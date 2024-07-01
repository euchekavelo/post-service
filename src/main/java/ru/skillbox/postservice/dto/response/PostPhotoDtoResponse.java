package ru.skillbox.postservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Schema(description = "Выходящий объект с расширенной информацией о фотографии поста.")
@Data
public class PostPhotoDtoResponse {

    @Schema(defaultValue = "3fa33f33-3333-3333-b2fc-3c333f33afa3", description = "Идентификатор фотографии поста.")
    private UUID id;

    @Schema(defaultValue = "Ссылка на фотографию", description = "Ссылка на фотографию поста.")
    private String link;

    @Schema(defaultValue = "Имя файла фотографии поста", description = "Имя файла фотографии поста.")
    private String name;

    @Schema(defaultValue = "ID поста", description = "Идентификатор поста.")
    private UUID postId;
}
