package com.banking.models.dto.response;

import com.banking.models.enums.UserRole;
import com.banking.models.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponseDTO {

    private UUID userId;
    private String email;
    private String firstName;
    private String infix;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private UserRole role;
    private LocalDateTime createdAt;
}