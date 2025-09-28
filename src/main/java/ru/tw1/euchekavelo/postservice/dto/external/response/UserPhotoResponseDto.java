package ru.tw1.euchekavelo.postservice.dto.external.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserPhotoResponseDto {

    private UUID id;
    private String link;
    private String name;
}
