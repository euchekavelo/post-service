package ru.skillbox.postservice.exception.enums;

public enum ExceptionMessage {

    POST_NOT_FOUND_EXCEPTION_MESSAGE("Пост с указанным идентификатором не найден."),
    PHOTO_NOT_FOUND_EXCEPTION_MESSAGE("Не удалось найти фото поста по указанным критериям."),
    INCORRECT_FILE_CONTENT_EXCEPTION_MESSAGE("В списке файлов обнаружены пустые файлы."),
    INCORRECT_FILE_FORMAT_EXCEPTION_MESSAGE("Неверный формат файла.");

    private final String exceptionText;

    ExceptionMessage(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public String getExceptionMessage() {
        return exceptionText;
    }
}
