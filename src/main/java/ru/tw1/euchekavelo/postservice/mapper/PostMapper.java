package ru.tw1.euchekavelo.postservice.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tw1.euchekavelo.postservice.config.properties.S3MinioProperties;
import ru.tw1.euchekavelo.postservice.dto.request.PostDtoRequest;
import ru.tw1.euchekavelo.postservice.dto.response.PostDtoResponse;
import ru.tw1.euchekavelo.postservice.model.Post;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PostMapper {

    @Autowired
    protected S3MinioProperties s3MinioProperties;

    public abstract Post postDtoRequestToPost(PostDtoRequest postDtoRequest);

    public abstract PostDtoResponse postToPostDtoResponse(Post post);

    public abstract List<PostDtoResponse> postListToPostDtoResponseList(List<Post> postList);

    @AfterMapping
    protected void setLink(@MappingTarget PostDtoResponse postDtoResponse) {
        postDtoResponse.getPhotos().forEach(photoDtoResponse ->
                photoDtoResponse.setLink(getActualEndpointLink().concat(photoDtoResponse.getLink())));
    }

    private String getActualEndpointLink() {
        String endpointValue = s3MinioProperties.getEndpoint();
        return endpointValue.endsWith("/") ? endpointValue : endpointValue.concat("/");
    }

    public abstract Post postToPost(@MappingTarget Post toPost, Post fromPost);
}
