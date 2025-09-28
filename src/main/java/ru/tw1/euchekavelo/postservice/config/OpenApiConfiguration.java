package ru.tw1.euchekavelo.postservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Микросервис по работе с постами пользователей",
                description = "Описание спецификации API микросервиса по работе с постами пользователей.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Suslov Kirill",
                        url = "https://t.me/euchekavelo"
                )
        ),
        security = @SecurityRequirement(name = "Bearer Token")
)
@SecurityScheme(
        name = "Bearer Token",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfiguration {

    public OpenApiConfiguration(MappingJackson2HttpMessageConverter converter) {
        List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }
}
