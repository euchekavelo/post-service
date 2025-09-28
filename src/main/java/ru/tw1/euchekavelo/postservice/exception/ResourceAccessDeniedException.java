package ru.tw1.euchekavelo.postservice.exception;

public class ResourceAccessDeniedException extends RuntimeException{

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}
