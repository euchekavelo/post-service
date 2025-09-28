package ru.tw1.euchekavelo.postservice.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tw1.euchekavelo.postservice.dto.response.ErrorDtoResponse;
import ru.tw1.euchekavelo.postservice.exception.*;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PhotoNotFoundException.class, PostNotFoundException.class})
    public ResponseEntity<ErrorDtoResponse> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getResponseDto(ex.getMessage()));
    }

    @ExceptionHandler({IncorrectFileContentException.class, IncorrectFileFormatException.class, IOException.class})
    public ResponseEntity<ErrorDtoResponse> handleBadRequestException(Exception ex) {
        return ResponseEntity.badRequest().body(getResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<ErrorDtoResponse> handeResourceAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(getResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorDtoResponse> handleExternalServiceException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getResponseDto(ex.getMessage()));
    }

    private ErrorDtoResponse getResponseDto(String message) {
        return ErrorDtoResponse.builder()
                .message(message)
                .result(false)
                .build();
    }
}
