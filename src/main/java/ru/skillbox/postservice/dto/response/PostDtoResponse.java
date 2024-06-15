package ru.skillbox.postservice.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PostDtoResponse {

    private UUID id;
    private String title;
    private UUID userId;
    private String description;
    private List<PhotoDtoResponse> photos;
    private LocalDateTime modificationDate;
}
