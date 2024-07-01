package ru.skillbox.postservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Входящий объект с информацией о новом посте пользователя.")
@Data
public class PostDtoRequest {

    @Schema(description = "Post title")
    @NotBlank
    private String title;

    @Schema(description = "Post description")
    @NotBlank
    private String description;
}
