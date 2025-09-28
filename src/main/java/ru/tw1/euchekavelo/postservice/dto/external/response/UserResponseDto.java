package ru.tw1.euchekavelo.postservice.dto.external.response;

import lombok.Data;
import ru.tw1.euchekavelo.postservice.dto.external.response.enums.Sex;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponseDto {

    private UUID id;
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private Sex sex;
    private List<UserSubscriptionResponseDto> subscriptions;
    private List<UserSubscriptionResponseDto> subscribers;
    private UserPhotoResponseDto photo;
}
