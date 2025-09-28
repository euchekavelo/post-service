package ru.tw1.euchekavelo.postservice.exception;

public class PhotoNotFoundException extends RuntimeException {

    public PhotoNotFoundException(String message) {
        super(message);
    }
}
