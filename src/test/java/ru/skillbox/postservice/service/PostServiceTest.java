package ru.skillbox.postservice.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.http.MediaTypeFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.postservice.config.ConfigPostService;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.exception.IncorrectFileContentException;
import ru.skillbox.postservice.exception.IncorrectFileFormatException;
import ru.skillbox.postservice.exception.PhotoNotFoundException;
import ru.skillbox.postservice.exception.PostNotFoundException;
import ru.skillbox.postservice.model.Photo;
import ru.skillbox.postservice.model.Post;
import ru.skillbox.postservice.repository.PhotoRepository;
import ru.skillbox.postservice.repository.PostRepository;
import ru.skillbox.postservice.repository.S3Repository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ConfigPostService.class, initializers = ConfigDataApplicationContextInitializer.class)
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private S3Repository s3Repository;

    private static List<MultipartFile> mockMultipartFileList;
    private static PostDtoRequest postDtoRequest;
    private static Post post;
    private static Photo photo;
    private static File correctFile;
    private static File incorrectFile;
    private static UUID postId;
    private static UUID photoId;

    @BeforeAll
    static void beforeAll() {
        postId = UUID.fromString("1fa11f11-1111-1111-b1fc-1c111f11afa1");
        photoId = UUID.fromString("2fa22f22-2222-2222-b1fc-2c222f22afa2");
        postDtoRequest = new PostDtoRequest();
        postDtoRequest.setTitle("Test post title");
        postDtoRequest.setDescription("Test post description");

        LocalDateTime currentTime = LocalDateTime.now();

        post = new Post();
        post.setId(postId);
        post.setCreationDate(currentTime);
        post.setTitle(postDtoRequest.getTitle());
        post.setDescription(postDtoRequest.getDescription());
        post.setModificationDate(currentTime);
        post.setUserId(null);

        correctFile = new File("src/test/resources/files/correct_file.png");
        incorrectFile = new File("src/test/resources/files/incorrect_file.txt");

        mockMultipartFileList = new ArrayList<>();

        photo = new Photo();
        photo.setId(photoId);
        photo.setCreationDate(currentTime);
        photo.setModificationDate(currentTime);
    }

    @AfterEach
    void tearDown() {
        mockMultipartFileList.clear();
    }

    @Test
    void createPostSuccessfulWithoutMultipartFilesTest() throws IOException, IncorrectFileFormatException {
        Mockito.when(postRepository.saveAndFlush(Mockito.any())).thenReturn(post);
        PostDtoResponse postDtoResponse = postService.createPost(postDtoRequest, null);

        assertThat(postDtoResponse.getDescription()).isEqualTo("Test post description");
        assertThat(postDtoResponse.getTitle()).isEqualTo("Test post title");
    }

    @Test
    void createPostSuccessfulWithNotEmptyMultipartFilesTest() throws IOException, IncorrectFileFormatException {
        photo.setName(correctFile.getName());
        photo.setLink("./testLink/" + correctFile.getName());
        post.setPhotos(List.of(photo));
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(correctFile)));

        Mockito.when(postRepository.saveAndFlush(Mockito.any())).thenReturn(post);
        PostDtoResponse postDtoResponse
                = postService.createPost(postDtoRequest, mockMultipartFileList.toArray(MultipartFile[]::new));

        assertThat(postDtoResponse.getDescription()).isEqualTo("Test post description");
        assertThat(postDtoResponse.getPhotos()).hasSizeGreaterThan(0);
    }

    @Test
    void createPostSuccessfulWithEmptyMultipartFilesTest() throws IOException, IncorrectFileFormatException {
        Mockito.when(postRepository.saveAndFlush(Mockito.any())).thenReturn(post);
        MultipartFile multipartFile
                = new MockMultipartFile(" ", "", "", new byte[0]);
        post.getPhotos().clear();
        mockMultipartFileList.add(multipartFile);

        PostDtoResponse postDtoResponse
                = postService.createPost(postDtoRequest, mockMultipartFileList.toArray(MultipartFile[]::new));

        assertThat(postDtoResponse.getDescription()).isEqualTo("Test post description");
        assertThat(postDtoResponse.getTitle()).isEqualTo("Test post title");
        assertTrue(postDtoResponse.getPhotos().isEmpty());
    }

    @Test
    void createPostThrowIncorrectFileFormatExceptionTest() throws IOException {
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(incorrectFile)));

        assertThrows(IncorrectFileFormatException.class,
                () -> postService.createPost(postDtoRequest, mockMultipartFileList.toArray(MultipartFile[]::new)));
    }

    @Test
    void getPostByIdSuccessfulTest() throws PostNotFoundException {
        Mockito.when(postRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(post));

        assertThat(postService.getPostById(postId).getId().toString())
                .hasToString("1fa11f11-1111-1111-b1fc-1c111f11afa1");
    }

    @Test
    void getPostByIdThrowPostNotFoundExceptionTest(){
        Mockito.when(postRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPostById(postId));
    }

    @Test
    void deletePostByIdSuccessfulTest() {
        Mockito.when(postRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> postService.deletePostById(postId));
    }

    @Test
    void deletePostByIdThrowPostNotFoundExceptionTest() {
        Mockito.when(postRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.deletePostById(postId));
    }

    @Test
    void updatePostByIdSuccessfulTest() throws IOException, IncorrectFileFormatException, PostNotFoundException {
        post.setPhotos(new ArrayList<>());
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(correctFile)));

        Mockito.when(postRepository.saveAndFlush(Mockito.any())).thenReturn(post);
        PostDtoResponse postDtoResponse
                = postService.updatePostById(postId, postDtoRequest, mockMultipartFileList.toArray(MultipartFile[]::new));

        assertThat(postDtoResponse.getDescription()).isEqualTo("Test post description");
        assertThat(postDtoResponse.getId()).isEqualTo(postId);
        assertThat(postDtoResponse.getPhotos()).hasSize(1);
    }

    @Test
    void updatePostByIdThrowPostNotFoundExceptionTest() throws IOException {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.empty());
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(correctFile)));

        assertThrows(PostNotFoundException.class,
                () -> postService.updatePostById(postId, postDtoRequest, mockMultipartFileList.toArray(MultipartFile[]::new)));
    }

    @Test
    void updatePostByIdWithoutMultipartFilesTest() throws IOException {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> postService.updatePostById(postId, postDtoRequest, null));
    }

    @Test
    void getAllPostsTest() {
        Mockito.when(postRepository.findAll()).thenReturn(List.of(post));

        List<PostDtoResponse> postDtoResponseList = postService.getAllPosts();
        assertFalse(postDtoResponseList.isEmpty());
    }

    @Test
    void addPhotosToPostSuccessfulTest() throws IOException, PostNotFoundException, IncorrectFileFormatException,
            IncorrectFileContentException {

        photo.setPost(post);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(photoRepository.saveAll(Mockito.anyCollection())).thenReturn(List.of(photo));
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(correctFile)));

        List<PostPhotoDtoResponse> postPhotoDtoResponseList
                = postService.addPhotosToPost(postId, mockMultipartFileList.toArray(MultipartFile[]::new));

        assertFalse(postPhotoDtoResponseList.isEmpty());
        assertEquals(postPhotoDtoResponseList.get(0).getPostId(), postId);
    }

    @Test
    void addPhotosToPostThrowPostNotFoundExceptionTest() throws IOException {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.empty());
        Mockito.when(photoRepository.saveAll(Mockito.anyCollection())).thenReturn(List.of(photo));
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(correctFile)));

        assertThrows(PostNotFoundException.class,
                () -> postService.addPhotosToPost(postId, mockMultipartFileList.toArray(MultipartFile[]::new)));
    }

    @Test
    void addPhotosToPostThrowIncorrectFileFormatExceptionTest() throws IOException {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(photoRepository.saveAll(Mockito.anyCollection())).thenReturn(List.of(photo));
        mockMultipartFileList.addAll(getMockMultipartFileList(List.of(incorrectFile)));

        assertThrows(IncorrectFileFormatException.class,
                () -> postService.addPhotosToPost(postId, mockMultipartFileList.toArray(MultipartFile[]::new)));
    }

    @Test
    void addPhotosToPostThrowIncorrectFileContentException() {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Mockito.when(photoRepository.saveAll(Mockito.anyCollection())).thenReturn(List.of(photo));

        MultipartFile multipartFile = new MockMultipartFile("empty_file.png", new byte[0]);
        mockMultipartFileList.add(multipartFile);

        assertThrows(IncorrectFileContentException.class,
                () -> postService.addPhotosToPost(postId, mockMultipartFileList.toArray(MultipartFile[]::new)));
    }

    @Test
    void getPostPhotoByIdAndPostIdSuccessfulTest() throws PhotoNotFoundException {
        photo.setName(correctFile.getName());
        photo.setLink("./testLink/" + correctFile.getName());
        photo.setPost(post);
        Mockito.when(photoRepository.findByIdAndPostId(photoId, postId)).thenReturn(Optional.of(photo));

        PostPhotoDtoResponse postPhotoDtoResponse = postService.getPostPhotoByIdAndPostId(postId, photoId);
        assertThat(postPhotoDtoResponse.getId()).isEqualTo(photoId);
        assertThat(postPhotoDtoResponse.getPostId()).isEqualTo(postId);
    }

    @Test
    void getPostPhotoByIdAndPostIdThrowPhotoNotFoundExceptionTest() throws PhotoNotFoundException {
        Mockito.when(photoRepository.findByIdAndPostId(photoId, postId)).thenReturn(Optional.empty());

        assertThrows(PhotoNotFoundException.class, () -> postService.getPostPhotoByIdAndPostId(postId, photoId));
    }

    @Test
    void deletePostPhotoByIdSuccessfulTest() {
        photo.setName(correctFile.getName());
        photo.setLink("./testLink/" + correctFile.getName());
        photo.setPost(post);
        Mockito.when(photoRepository.findByIdAndPostId(photoId, postId)).thenReturn(Optional.of(photo));

        assertDoesNotThrow(() -> postService.deletePostPhotoById(postId, photoId));
    }

    @Test
    void deletePostPhotoByIdThrowPhotoNotFoundExceptionTest() {
        Mockito.when(photoRepository.findByIdAndPostId(photoId, postId)).thenReturn(Optional.empty());

        assertThrows(PhotoNotFoundException.class, () -> postService.deletePostPhotoById(postId, photoId));
    }

    @Test
    void getPostPhotosSuccessfulTest() throws PostNotFoundException {
        photo.setName(correctFile.getName());
        photo.setLink("./testLink/" + correctFile.getName());
        photo.setPost(post);
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        List<PostPhotoDtoResponse> postPhotoDtoResponse = postService.getPostPhotos(postId);

        assertFalse(postPhotoDtoResponse.isEmpty());
    }

    @Test
    void getPostPhotosThrowPostNotFoundExceptionTest() {
        Mockito.when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.getPostPhotos(postId));
    }

    private static List<MultipartFile> getMockMultipartFileList(List<File> fileList) throws IOException {
        List<MultipartFile> mockMultipartFileList = new ArrayList<>();

        for (File file : fileList) {
            try (InputStream  inputStream = new FileInputStream(file)) {
                String contentType = MediaTypeFactory.getMediaType(file.getName()).toString();
                MultipartFile multipartFile
                        = new MockMultipartFile(file.getName(), file.getName(), contentType, inputStream);

                mockMultipartFileList.add(multipartFile);
            }
        }

        return mockMultipartFileList;
    }
}
