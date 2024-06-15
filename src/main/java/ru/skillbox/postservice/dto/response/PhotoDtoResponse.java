package ru.skillbox.postservice.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PhotoDtoResponse {

    private UUID id;
    private String link;
    private String name;
}
