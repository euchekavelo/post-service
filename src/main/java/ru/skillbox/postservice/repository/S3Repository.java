package ru.skillbox.postservice.repository;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;
import java.util.List;

public interface S3Repository {

    void put(String name, InputStream inputStream, ObjectMetadata objectMetaData);

    void delete(String name);

    void restore(String name);

    void deleteAllByNames(List<String> names);
}
