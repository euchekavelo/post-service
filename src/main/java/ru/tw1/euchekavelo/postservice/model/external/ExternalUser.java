package ru.tw1.euchekavelo.postservice.model.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalUser {

    private UUID id;
    private String email;
}
