package ru.skillbox.postservice.exception;

public class PostNotFoundException extends Exception {

    public PostNotFoundException(String message) {
        super(message);
    }
}
