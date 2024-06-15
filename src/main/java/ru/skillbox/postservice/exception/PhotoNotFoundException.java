package ru.skillbox.postservice.exception;

public class PhotoNotFoundException extends Exception {

    public PhotoNotFoundException(String message) {
        super(message);
    }
}
