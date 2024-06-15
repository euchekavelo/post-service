package ru.skillbox.postservice.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PostPhotoDtoResponse {

    private UUID id;
    private String link;
    private String name;
    private UUID postId;
}
