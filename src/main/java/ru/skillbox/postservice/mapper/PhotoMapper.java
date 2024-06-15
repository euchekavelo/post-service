package ru.skillbox.postservice.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skillbox.postservice.config.properties.S3MinioProperties;
import ru.skillbox.postservice.dto.response.PostPhotoDtoResponse;
import ru.skillbox.postservice.model.Photo;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PhotoMapper {

    @Autowired
    protected S3MinioProperties s3MinioProperties;

    @Mapping(target = "postId", source = "photo.post.id")
    public abstract PostPhotoDtoResponse photoToPostPhotoDtoResponse(Photo photo);

    public abstract List<PostPhotoDtoResponse> photoListToPostPhotoDtoResponseList(List<Photo> photoList);

    @AfterMapping
    protected void setLink(@MappingTarget PostPhotoDtoResponse postPhotoDtoResponse) {
        String endpointValue = s3MinioProperties.getEndpoint();
        String endpointLink = endpointValue.endsWith("/") ? endpointValue : endpointValue.concat("/");
        postPhotoDtoResponse.setLink(endpointLink.concat(postPhotoDtoResponse.getLink()));
    }
}
