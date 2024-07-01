package ru.skillbox.postservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Выходящий объект с информацией о посте пользователя.")
@Data
public class PostDtoResponse {

    @Schema(defaultValue = "1fa11f11-1111-1111-b1fc-1c111f11afa1", description = "Идентификатор поста.")
    private UUID id;

    @Schema(defaultValue = "Название поста", description = "Название поста.")
    private String title;

    @Schema(defaultValue = "2fa22f22-2222-2222-b2fc-2c222f22afa2", description = "Идентификатор пользователя.")
    private UUID userId;

    @Schema(defaultValue = "Описание поста", description = "Описание поста.")
    private String description;

    @Schema(description = "Список фотографий поста.")
    private List<PhotoDtoResponse> photos;

    @Schema(description = "Дата последней модификации")
    private LocalDateTime modificationDate;
}
