package com.banking.models.dto.response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {

    private String token;
    private String role;
    private String email;
    private UUID userId;
}