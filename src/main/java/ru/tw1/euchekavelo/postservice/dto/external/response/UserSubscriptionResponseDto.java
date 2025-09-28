package ru.tw1.euchekavelo.postservice.dto.external.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserSubscriptionResponseDto {

    private UUID userId;
    private LocalDateTime creationTime;
}
