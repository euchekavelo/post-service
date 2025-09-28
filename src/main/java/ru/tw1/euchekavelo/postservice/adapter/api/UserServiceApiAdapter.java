package ru.tw1.euchekavelo.postservice.adapter.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.tw1.euchekavelo.postservice.dto.external.response.UserResponseDto;
import ru.tw1.euchekavelo.postservice.exception.ExternalServiceException;
import ru.tw1.euchekavelo.postservice.mapper.UserMapper;
import ru.tw1.euchekavelo.postservice.model.external.ExternalUser;

import java.util.UUID;

@Component
public class UserServiceApiAdapter {

    private final RestClient restClient;
    private final UserMapper userMapper;

    public UserServiceApiAdapter(RestClient.Builder restClientBuilder, UserMapper userMapper,
                                 @Value("${user-service.api.root-url}") String baseUrl) {

        this.userMapper = userMapper;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public ExternalUser getUserById(UUID userId) {
        try {
            UserResponseDto userResponseDto = restClient.get()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .body(UserResponseDto.class);

            return userMapper.UserResponseDtoToExternalUser(userResponseDto);
        } catch (Exception e) {
            throw new ExternalServiceException("Произошла ошибка при попытке получить внешнего пользователя: "
                    + e.getMessage());
        }
    }
}
