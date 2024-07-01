package ru.skillbox.postservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Выходящий объект с информацией об ошибке.")
@Data
@Builder
public class ErrorDtoResponse {

    @Schema(defaultValue = "Текст ошибки")
    private String message;

    @Schema(defaultValue = "false")
    private boolean result;
}
