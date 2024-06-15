package ru.skillbox.postservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.exception.IncorrectFileContentException;
import ru.skillbox.postservice.exception.IncorrectFileFormatException;
import ru.skillbox.postservice.exception.PhotoNotFoundException;
import ru.skillbox.postservice.exception.PostNotFoundException;
import ru.skillbox.postservice.service.PostService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDtoResponse> createPost(@RequestPart(name = "data") PostDtoRequest postDtoRequest,
                                                      @RequestPart MultipartFile[] files)
            throws IOException, IncorrectFileFormatException {

        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(postDtoRequest, files));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PostDtoResponse> getPostById(@PathVariable UUID uuid) throws PostNotFoundException {
        return ResponseEntity.ok(postService.getPostById(uuid));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deletePostByID(@PathVariable UUID uuid) throws PostNotFoundException {
        postService.deletePostById(uuid);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{uuid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDtoResponse> updatePostById(@PathVariable UUID uuid,
                                                          @RequestPart(name = "data") PostDtoRequest postDtoRequest,
                                                          @RequestPart MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException {

        return ResponseEntity.ok(postService.updatePostById(uuid, postDtoRequest, files));
    }

    @GetMapping
    public ResponseEntity<List<PostDtoResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping(path = "/{postId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PostPhotoDtoResponse>> addPhotosToPost(@PathVariable UUID postId,
                                                                      @RequestPart MultipartFile[] files)
            throws PostNotFoundException, IncorrectFileFormatException, IOException, IncorrectFileContentException {

        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addPhotosToPost(postId, files));
    }

    @GetMapping("/{postId}/photos/{photoId}")
    public ResponseEntity<PostPhotoDtoResponse> getPostPhotoById(@PathVariable UUID postId, @PathVariable UUID photoId)
            throws PostNotFoundException, PhotoNotFoundException {

        return ResponseEntity.ok(postService.getPostPhotoById(postId, photoId));
    }

    @DeleteMapping("/{postId}/photos/{photoId}")
    public ResponseEntity<Void> deletePostPhotoById(@PathVariable UUID postId, @PathVariable UUID photoId)
            throws PhotoNotFoundException {

        postService.deletePostPhotoById(postId, photoId);

        return ResponseEntity.noContent().build();
    }
}
