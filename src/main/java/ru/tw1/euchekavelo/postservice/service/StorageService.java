package ru.tw1.euchekavelo.postservice.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.tw1.euchekavelo.postservice.exception.IncorrectFileFormatException;
import ru.tw1.euchekavelo.postservice.repository.S3Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static ru.tw1.euchekavelo.postservice.exception.enums.ExceptionMessage.INCORRECT_FILE_FORMAT_EXCEPTION_MESSAGE;

@Observed
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Repository s3Repository;
    private static final List<String> CORRECT_FILE_FORMATS = List.of("PNG", "JPEG", "JPG");
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    public void uploadFile(MultipartFile file, String fileName) {
        if (!isValidFormatFile(file)) {
            String enumerationFileFormats = String.join(", ", CORRECT_FILE_FORMATS);
            String errorMessage = INCORRECT_FILE_FORMAT_EXCEPTION_MESSAGE.getExceptionMessage() + " Рекомендуемые форматы: "
                    + enumerationFileFormats + ".";

            LOGGER.error(errorMessage);
            throw new IncorrectFileFormatException(errorMessage);
        }

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());

            s3Repository.put(fileName, inputStream, objectMetadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFileByName(String fileName) {
        s3Repository.delete(fileName);
    }

    private boolean isValidFormatFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String fileExtension = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf(".") + 1);

        return CORRECT_FILE_FORMATS.stream()
                .anyMatch(format -> format.equals(fileExtension.toUpperCase()));
    }
}
