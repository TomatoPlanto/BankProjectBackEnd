package com.banking.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String infix;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "BSN is required")
    @Size(min = 9, max = 9, message = "BSN must be exactly 9 digits")
    @Pattern(regexp = "\\d{9}", message = "BSN must contain only digits")
    private String bsn;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}