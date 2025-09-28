package ru.tw1.euchekavelo.postservice.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tw1.euchekavelo.postservice.adapter.api.UserServiceApiAdapter;
import ru.tw1.euchekavelo.postservice.exception.PostNotFoundException;
import ru.tw1.euchekavelo.postservice.mapper.PostMapper;
import ru.tw1.euchekavelo.postservice.model.Photo;
import ru.tw1.euchekavelo.postservice.model.Post;
import ru.tw1.euchekavelo.postservice.repository.PostRepository;

import java.util.List;
import java.util.UUID;

import static ru.tw1.euchekavelo.postservice.exception.enums.ExceptionMessage.POST_NOT_FOUND_EXCEPTION_MESSAGE;

@Observed
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserServiceApiAdapter userServiceApiAdapter;
    private final AuthorizationService authorizationService;
    private final StorageService storageService;

    public Post createPost(Post post) {
        UUID userId = post.getUserId();
        authorizationService.checkAccess(userId);
        userServiceApiAdapter.getUserById(userId);

        return postRepository.save(post);
    }

    public Post getPostById(UUID uuid) {
        return postRepository.findById(uuid).orElseThrow(() -> {
            log.error(POST_NOT_FOUND_EXCEPTION_MESSAGE.getExceptionMessage());
            return new PostNotFoundException(POST_NOT_FOUND_EXCEPTION_MESSAGE.getExceptionMessage());
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deletePostById(UUID uuid) {
        Post post = getPostById(uuid);
        userServiceApiAdapter.getUserById(post.getUserId());
        authorizationService.checkAccess(post.getUserId());

        List<Photo> photos = post.getPhotos();
        postRepository.delete(post);

        for (Photo photo : photos) {
            storageService.deleteFileByName(photo.getName());
        }
    }


    public Post updatePostById(UUID uuid, Post post) {
        Post existingPost = getPostById(uuid);
        userServiceApiAdapter.getUserById(existingPost.getUserId());
        authorizationService.checkAccess(existingPost.getUserId());

        return postRepository.save(postMapper.postToPost(existingPost, post));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }
}
