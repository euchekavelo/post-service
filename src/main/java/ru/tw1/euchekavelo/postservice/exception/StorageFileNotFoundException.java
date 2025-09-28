package ru.tw1.euchekavelo.postservice.exception;

public class StorageFileNotFoundException extends RuntimeException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }
}
