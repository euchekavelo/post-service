package ru.skillbox.postservice.repository;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.Optional;

public interface S3Repository {

    void put(String name, InputStream inputStream, ObjectMetadata objectMetaData);

    void delete(String name);

    Optional<S3Object> get(String name);
}
