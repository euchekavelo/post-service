package ru.skillbox.postservice.repository.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.skillbox.postservice.config.properties.S3MinioProperties;
import ru.skillbox.postservice.repository.S3Repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class S3RepositoryImpl implements S3Repository {

    private final AmazonS3 amazonS3Client;
    private final S3MinioProperties s3MinioProperties;

    @Autowired
    public S3RepositoryImpl(AmazonS3 amazonS3Client, S3MinioProperties s3MinioProperties) {
        this.amazonS3Client = amazonS3Client;
        this.s3MinioProperties = s3MinioProperties;
    }

    @Override
    public void put(String name, InputStream inputStream, ObjectMetadata objectMetaData) {
        amazonS3Client.putObject(s3MinioProperties.getBucketPosts(), name, inputStream, objectMetaData);
    }

    @Override
    public void delete(String name) {
        amazonS3Client.deleteObject(s3MinioProperties.getBucketPosts(), name);
    }

    @Override
    public void restore(String name) {
        RestoreObjectRequest restoreObjectRequest = new RestoreObjectRequest(s3MinioProperties.getBucketPosts(), name);
        amazonS3Client.restoreObjectV2(restoreObjectRequest);
    }

    @Override
    public void deleteAllByNames(List<String> names) {
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        names.forEach(name -> keys.add(new DeleteObjectsRequest.KeyVersion(name)));

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(s3MinioProperties.getBucketPosts());
        deleteObjectsRequest.setKeys(keys);

        amazonS3Client.deleteObjects(deleteObjectsRequest);
    }
}
