package ru.skillbox.postservice.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skillbox.postservice.config.properties.S3MinioProperties;
import ru.skillbox.postservice.dto.request.PostDtoRequest;
import ru.skillbox.postservice.dto.response.PostDtoResponse;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.model.Post;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PostMapper {

    @Autowired
    protected S3MinioProperties s3MinioProperties;

    public abstract Post postDtoRequestToPost(PostDtoRequest postDtoRequest);

    public abstract PostDtoResponse postToPostDtoResponse(Post post);

    public abstract Post updatePostFromPostDtoRequest(@MappingTarget Post post, PostDtoRequest postDtoRequest);

    public abstract List<PostDtoResponse> postListToPostDtoResponseList(List<Post> postList);

    public List<PostPhotoDtoResponse> postToPostPhotoDtoResponseList(Post post) {
        return post.getPhotos().stream()
                .map(photo -> {
                    PostPhotoDtoResponse postPhotoDtoResponse = new PostPhotoDtoResponse();
                    postPhotoDtoResponse.setId(photo.getId());
                    postPhotoDtoResponse.setName(photo.getName());
                    postPhotoDtoResponse.setPostId(post.getId());
                    postPhotoDtoResponse.setLink(getActualEndpointLink().concat(photo.getLink()));

                    return postPhotoDtoResponse;
                })
                .toList();
    }

    @AfterMapping
    protected void setLink(@MappingTarget PostDtoResponse postDtoResponse) {
        postDtoResponse.getPhotos().forEach(photoDtoResponse ->
                photoDtoResponse.setLink(getActualEndpointLink().concat(photoDtoResponse.getLink())));
    }

    private String getActualEndpointLink() {
        String endpointValue = s3MinioProperties.getEndpoint();
        return endpointValue.endsWith("/") ? endpointValue : endpointValue.concat("/");
    }
}
