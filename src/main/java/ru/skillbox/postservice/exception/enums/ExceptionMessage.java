package ru.skillbox.postservice.exception.enums;

public enum ExceptionMessage {

    POST_NOT_FOUND_EXCEPTION_MESSAGE("Пост с указанным идентификатором не найден.");

    private final String exceptionText;

    ExceptionMessage(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public String getExceptionMessage() {
        return exceptionText;
    }
}
