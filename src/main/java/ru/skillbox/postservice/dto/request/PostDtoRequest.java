package ru.skillbox.postservice.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class PostDtoRequest {

    private String title;
    private UUID userId;
    private String description;
}
