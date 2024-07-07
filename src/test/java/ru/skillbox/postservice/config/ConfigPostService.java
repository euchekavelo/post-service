package ru.skillbox.postservice.config;

import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.skillbox.postservice.config.properties.S3MinioProperties;
import ru.skillbox.postservice.mapper.PhotoMapper;
import ru.skillbox.postservice.mapper.PostMapper;
import ru.skillbox.postservice.repository.PhotoRepository;
import ru.skillbox.postservice.repository.PostRepository;
import ru.skillbox.postservice.repository.S3Repository;
import ru.skillbox.postservice.service.PostService;
import ru.skillbox.postservice.service.impl.PostServiceImpl;

@TestConfiguration
@EnableConfigurationProperties
public class ConfigPostService {

    @Bean
    public S3Repository s3Repository() {
        return Mockito.mock(S3Repository.class);
    }

    @Bean
    public S3MinioProperties s3MinioProperties() {
        return new S3MinioProperties();
    }

    @Bean
    public PostRepository postRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    public PostMapper postMapper() {
        return Mappers.getMapper(PostMapper.class);
    }

    @Bean
    public PhotoRepository photoRepository() {
        return Mockito.mock(PhotoRepository.class);
    }

    @Bean
    public PhotoMapper photoMapper() {
        return Mappers.getMapper(PhotoMapper.class);
    }

    @Bean
    public PostService postService() {
        return new PostServiceImpl(s3Repository(), s3MinioProperties(), postRepository(), postMapper(),
                photoRepository(), photoMapper());
    }
}
