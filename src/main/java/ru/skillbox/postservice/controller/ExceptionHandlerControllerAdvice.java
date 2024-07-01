package ru.skillbox.postservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.skillbox.postservice.dto.response.ErrorDtoResponse;
import ru.skillbox.postservice.exception.IncorrectFileContentException;
import ru.skillbox.postservice.exception.IncorrectFileFormatException;
import ru.skillbox.postservice.exception.PhotoNotFoundException;
import ru.skillbox.postservice.exception.PostNotFoundException;

import java.io.IOException;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

    @ExceptionHandler({PhotoNotFoundException.class, PostNotFoundException.class})
    public ResponseEntity<ErrorDtoResponse> handleNotFoundException(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getResponseDto(ex.getMessage()));
    }

    @ExceptionHandler({IncorrectFileContentException.class, IncorrectFileFormatException.class, IOException.class})
    public ResponseEntity<ErrorDtoResponse> handleBadRequestException(Exception ex) {
        return ResponseEntity.badRequest().body(getResponseDto(ex.getMessage()));
    }

    private ErrorDtoResponse getResponseDto(String message) {
        return ErrorDtoResponse.builder()
                .message(message)
                .result(false)
                .build();
    }
}
