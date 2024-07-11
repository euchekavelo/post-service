package ru.skillbox.postservice.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skillbox.postservice.config.properties.S3MinioProperties;

@Configuration
@RequiredArgsConstructor
public class S3Configuration {

    @Autowired
    private final S3MinioProperties s3MinioProperties;

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(s3MinioProperties.getAccessKey(), s3MinioProperties.getSecretKey());
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider(AWSCredentials credentials) {
        return new AWSStaticCredentialsProvider(credentials);
    }

    @Bean
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride(s3MinioProperties.getSigner());
        clientConfiguration.withProtocol(Protocol.HTTP);
        return clientConfiguration;
    }

    @Bean
    public AmazonS3 amazonS3(AWSCredentialsProvider credentialsProvider, ClientConfiguration configuration) {
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3MinioProperties.getEndpoint(),
                        s3MinioProperties.getRegion()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(configuration)
                .withCredentials(credentialsProvider)
                .build();
    }
}
