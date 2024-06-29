package ru.skillbox.postservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.exception.IncorrectFileContentException;
import ru.skillbox.postservice.exception.IncorrectFileFormatException;
import ru.skillbox.postservice.exception.PhotoNotFoundException;
import ru.skillbox.postservice.exception.PostNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PostService {

    PostDtoResponse createPost(PostDtoRequest postDtoRequest, MultipartFile[] files) throws IOException,
            IncorrectFileFormatException;

    PostDtoResponse getPostById(UUID uuid) throws PostNotFoundException;

    void deletePostById(UUID uuid) throws PostNotFoundException, IOException;

    PostDtoResponse updatePostById(UUID uuid, PostDtoRequest postDtoRequest, MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException;

    List<PostDtoResponse> getAllPosts();

    List<PostPhotoDtoResponse> addPhotosToPost(UUID postId, MultipartFile[] files) throws PostNotFoundException,
            IncorrectFileFormatException, IOException, IncorrectFileContentException;

    PostPhotoDtoResponse getPostPhotoById(UUID postId, UUID photoId) throws PostNotFoundException, PhotoNotFoundException;

    void deletePostPhotoById(UUID postId, UUID photoId) throws PhotoNotFoundException, IOException;
}
