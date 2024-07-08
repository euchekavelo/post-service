package ru.skillbox.postservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class S3Configuration {

    private static final LocalStackContainer LOCAL_STACK_CONTAINER;

    static {
        LOCAL_STACK_CONTAINER = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.14"))
                .withServices(LocalStackContainer.Service.S3)
                .withEnv("DEFAULT_REGION", "ru-center");

        LOCAL_STACK_CONTAINER.start();
    }

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                LOCAL_STACK_CONTAINER.getEndpoint().toString(),
                                LOCAL_STACK_CONTAINER.getRegion()
                        )
                )
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        LOCAL_STACK_CONTAINER.getAccessKey(),
                                        LOCAL_STACK_CONTAINER.getSecretKey()
                                )
                        )
                )
                .build();

        if (!amazonS3.doesBucketExistV2("posts")) {
            amazonS3.createBucket(new CreateBucketRequest("posts"));
        }

        return amazonS3;
    }
}
