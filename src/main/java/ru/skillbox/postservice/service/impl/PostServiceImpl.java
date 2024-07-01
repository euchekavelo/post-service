package ru.skillbox.postservice.service.impl;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.postservice.config.properties.S3MinioProperties;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.exception.*;
import ru.skillbox.postservice.mapper.PhotoMapper;
import ru.skillbox.postservice.mapper.PostMapper;
import ru.skillbox.postservice.model.Photo;
import ru.skillbox.postservice.model.Post;
import ru.skillbox.postservice.repository.PhotoRepository;
import ru.skillbox.postservice.repository.PostRepository;
import ru.skillbox.postservice.repository.S3Repository;
import ru.skillbox.postservice.service.PostService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    private final S3Repository s3Repository;
    private final S3MinioProperties s3MinioProperties;
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final PostMapper postMapper;
    private final PhotoMapper photoMapper;
    private static final List<String> CORRECT_FILE_FORMATS = List.of("PNG", "JPEG", "JPG");

    @Autowired
    public PostServiceImpl(S3Repository s3Repository, S3MinioProperties s3MinioProperties, PostRepository postRepository,
                           PostMapper postMapper, PhotoRepository photoRepository, PhotoMapper photoMapper) {

        this.s3Repository = s3Repository;
        this.s3MinioProperties = s3MinioProperties;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.photoRepository = photoRepository;
        this.photoMapper = photoMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PostDtoResponse createPost(PostDtoRequest postDtoRequest, MultipartFile[] files)
            throws IncorrectFileFormatException, IOException {

        List<String> namesDownloadedFiles = new ArrayList<>();

        try {
            Post post = postMapper.postDtoRequestToPost(postDtoRequest);
            if (files != null) {
                List<Photo> photoList = getListPhotosToUploadToDatabase(post, files, namesDownloadedFiles);
                post.setPhotos(photoList);
            }
            Post savedPost = postRepository.saveAndFlush(post);

            return postMapper.postToPostDtoResponse(savedPost);
        } catch (Exception ex) {
            namesDownloadedFiles.forEach(s3Repository::delete);
            throw ex;
        }
    }

    @Override
    public PostDtoResponse getPostById(UUID uuid) throws PostNotFoundException {
        Optional<Post> optionalPost = postRepository.findById(uuid);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Пост с указанным идентификатором не найден.");
        }

        return postMapper.postToPostDtoResponse(optionalPost.get());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePostById(UUID uuid) throws PostNotFoundException, IOException {
        Optional<Post> optionalPost = postRepository.findById(uuid);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Пост с указанным идентификатором не найден.");
        }

        List<S3Object> s3ObjectList = new ArrayList<>();

        Post post = optionalPost.get();
        List<String> namesFiles = post.getPhotos().stream()
                .map(Photo::getName)
                .toList();

        try {
            fillListDeletedS3Objects(namesFiles, s3ObjectList);
            postRepository.delete(post);
        } catch (Exception ex) {
            uploadS3ObjectList(s3ObjectList);
            throw ex;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PostDtoResponse updatePostById(UUID uuid, PostDtoRequest postDtoRequest, MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException {

        Optional<Post> optionalPost = postRepository.findById(uuid);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Пост с указанным идентификатором не найден.");
        }

        List<String> namesDownloadedFiles = new ArrayList<>();

        try {
            Post post = optionalPost.get();
            if (files != null) {
                List<Photo> photoList = getListPhotosToUploadToDatabase(post, files, namesDownloadedFiles);
                post.addPhotos(photoList);
            }
            Post updatedPost = postMapper.updatePostFromPostDtoRequest(post, postDtoRequest);
            Post savedPost = postRepository.saveAndFlush(updatedPost);

            return postMapper.postToPostDtoResponse(savedPost);
        } catch (Exception ex) {
            namesDownloadedFiles.forEach(s3Repository::delete);
            throw ex;
        }
    }

    @Override
    public List<PostDtoResponse> getAllPosts() {
        return postMapper.postListToPostDtoResponseList(postRepository.findAll());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<PostPhotoDtoResponse> addPhotosToPost(UUID postId, MultipartFile[] files) throws PostNotFoundException,
            IncorrectFileFormatException, IOException, IncorrectFileContentException {

        if (areAllFilesEmpty(files)) {
            throw new IncorrectFileContentException("В списке файлов обнаружены пустые файлы.");
        }

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Пост с указанным идентификатором не найден.");
        }

        List<String> namesDownloadedFiles = new ArrayList<>();

        try {
            Post post = optionalPost.get();
            List<Photo> photoList = getListUploadedPhoto(post, files, namesDownloadedFiles);
            List<Photo> savedPhotos = photoRepository.saveAll(photoList);

            return photoMapper.photoListToPostPhotoDtoResponseList(savedPhotos);
        } catch (Exception ex) {
            namesDownloadedFiles.forEach(s3Repository::delete);
            throw ex;
        }
    }

    @Override
    public PostPhotoDtoResponse getPostPhotoById(UUID postId, UUID photoId) throws PhotoNotFoundException {
        Optional<Photo> optionalPhoto = photoRepository.findByIdAndPostId(photoId, postId);
        if (optionalPhoto.isEmpty()) {
            throw new PhotoNotFoundException("Не удалось найти фото поста по указанным критериям.");
        }

        return photoMapper.photoToPostPhotoDtoResponse(optionalPhoto.get());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePostPhotoById(UUID postId, UUID photoId) throws PhotoNotFoundException, IOException {
        Optional<Photo> optionalPhoto = photoRepository.findByIdAndPostId(photoId, postId);
        if (optionalPhoto.isEmpty()) {
            throw new PhotoNotFoundException("Не удалось найти фото поста по указанным критериям.");
        }

        List<S3Object> s3ObjectList = new ArrayList<>();

        try {
            photoRepository.delete(optionalPhoto.get());
            fillListDeletedS3Objects(List.of(optionalPhoto.get().getName()), s3ObjectList);
        } catch (Exception ex) {
            uploadS3ObjectList(s3ObjectList);
            throw ex;
        }
    }

    @Override
    public List<PostPhotoDtoResponse> getPostPhotos(UUID postId) throws PostNotFoundException {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException("Пост с указанным идентификатором не найден.");
        }

        return photoMapper.photoListToPostPhotoDtoResponseList(optionalPost.get().getPhotos());
    }

    private List<Photo> getListPhotosToUploadToDatabase(Post post, MultipartFile[] files, List<String> namesDownloadedFiles)
            throws IncorrectFileFormatException, IOException {

        if ((files.length == 1) && areAllFilesEmpty(files)
                && Objects.requireNonNull(files[0].getOriginalFilename()).isEmpty()) {

            return new ArrayList<>();
        }

        return getListUploadedPhoto(post, files, namesDownloadedFiles);
    }

    private List<Photo> getListUploadedPhoto(Post post, MultipartFile[] files, List<String> namesDownloadedFiles)
            throws IncorrectFileFormatException, IOException {

        List<Photo> photoList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!isValidFormatFile(file)) {
                String enumerationFileFormats = String.join(", ", CORRECT_FILE_FORMATS);
                throw new IncorrectFileFormatException("Неверный формат файла. Рекомендуемые форматы: "
                        + enumerationFileFormats + ".");
            }

            String uniqueFileName = generateUniqueFileNameForUser(post, file);
            try (InputStream inputStream = file.getInputStream()) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(file.getContentType());
                objectMetadata.setContentLength(file.getSize());

                s3Repository.put(uniqueFileName, inputStream, objectMetadata);
            }

            namesDownloadedFiles.add(uniqueFileName);

            Photo photo = new Photo();
            photo.setPost(post);
            photo.setLink(generateShortLinkForFile(generateShortLinkForFile(uniqueFileName)));
            photo.setName(uniqueFileName);
            photoList.add(photo);
        }

        return photoList;
    }

    private boolean areAllFilesEmpty(MultipartFile[] files) {
        return Arrays.stream(files).anyMatch(MultipartFile::isEmpty);
    }

    private String generateUniqueFileNameForUser(Post post, MultipartFile file) {
        return post.getUserId() + "_" + LocalDateTime.now() + "_" + file.getOriginalFilename();
    }

    private boolean isValidFormatFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String fileExtension = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf(".") + 1);

        return CORRECT_FILE_FORMATS.stream()
                .anyMatch(format -> format.equals(fileExtension.toUpperCase()));
    }

    private String generateShortLinkForFile(String fileName) {
        return s3MinioProperties.getBucketPosts() + "/" + fileName;
    }

    private void uploadS3ObjectList(List<S3Object> s3ObjectList) throws IOException {
        for (S3Object s3Object : s3ObjectList) {
            try (InputStream inputStream = s3Object.getObjectContent()) {
                s3Repository.put(s3Object.getKey(), inputStream, s3Object.getObjectMetadata());
            }
        }
    }

    private void fillListDeletedS3Objects(List<String> namesFiles, List<S3Object> s3ObjectList) {
        for (String fileName : namesFiles) {
            Optional<S3Object> optionalS3Object = s3Repository.get(fileName);
            if (optionalS3Object.isEmpty()) {
                continue;
            }

            s3Repository.delete(fileName);
            s3ObjectList.add(optionalS3Object.get());
        }
    }
}