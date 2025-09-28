package ru.tw1.euchekavelo.postservice.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.tw1.euchekavelo.postservice.config.properties.S3MinioProperties;
import ru.tw1.euchekavelo.postservice.exception.IncorrectFileContentException;
import ru.tw1.euchekavelo.postservice.exception.PhotoNotFoundException;
import ru.tw1.euchekavelo.postservice.model.Photo;
import ru.tw1.euchekavelo.postservice.model.Post;
import ru.tw1.euchekavelo.postservice.repository.PhotoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ru.tw1.euchekavelo.postservice.exception.enums.ExceptionMessage.INCORRECT_FILE_CONTENT_EXCEPTION_MESSAGE;
import static ru.tw1.euchekavelo.postservice.exception.enums.ExceptionMessage.PHOTO_NOT_FOUND_EXCEPTION_MESSAGE;

@Observed
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PostService postService;
    private final StorageService storageService;
    private final S3MinioProperties s3MinioProperties;

    @Transactional(rollbackFor = Throwable.class)
    public List<Photo> addPhotosToPost(UUID postId, MultipartFile[] files) {
        if (emptyFileExistsInArray(files)) {
            log.error(INCORRECT_FILE_CONTENT_EXCEPTION_MESSAGE.getExceptionMessage());
            throw new IncorrectFileContentException(INCORRECT_FILE_CONTENT_EXCEPTION_MESSAGE.getExceptionMessage());
        }

        Post post = postService.getPostById(postId);
        List<String> namesDownloadedFiles = new ArrayList<>();
        List<Photo> photos = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String filename = generateUniqueFileNameForPost(post, file);
                storageService.uploadFile(file, filename);
                Photo photo = new Photo();
                photo.setPost(post);
                photo.setName(filename);
                photo.setLink(generateShortLinkForFile(filename));
                photos.add(photo);
                namesDownloadedFiles.add(filename);
            }

            List<Photo> savedPhotos = photoRepository.saveAll(photos);
            List<Photo> existingPhotos = post.getPhotos();
            existingPhotos.addAll(savedPhotos);
            post.setPhotos(existingPhotos);
            postService.savePost(post);

            return savedPhotos;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            namesDownloadedFiles.forEach(storageService::deleteFileByName);
            throw ex;
        }
    }

    public List<Photo> getPostPhotos(UUID postId) {
        Post post = postService.getPostById(postId);

        return post.getPhotos();
    }

    public Photo getPostPhotoByIdAndPostId(UUID postId, UUID photoId) {
        return photoRepository.findByIdAndPostId(photoId, postId).orElseThrow(() -> {
            log.error(PHOTO_NOT_FOUND_EXCEPTION_MESSAGE.getExceptionMessage());
            return new PhotoNotFoundException(PHOTO_NOT_FOUND_EXCEPTION_MESSAGE.getExceptionMessage());
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deletePostPhotoById(UUID postId, UUID photoId) {
        Photo photo = getPostPhotoByIdAndPostId(postId, photoId);

        photoRepository.delete(photo);
        storageService.deleteFileByName(photo.getName());
    }

    private boolean emptyFileExistsInArray(MultipartFile[] files) {
        return Arrays.stream(files).anyMatch(MultipartFile::isEmpty);
    }

    private String generateUniqueFileNameForPost(Post post, MultipartFile file) {
        return new StringBuilder(post.getId().toString())
                .append("_")
                .append(post.getUserId())
                .append("_")
                .append(LocalDateTime.now())
                .append("_")
                .append(file.getOriginalFilename())
                .toString();
    }

    private String generateShortLinkForFile(String fileName) {
        return s3MinioProperties.getBucketPosts() + "/" + fileName;
    }
}
