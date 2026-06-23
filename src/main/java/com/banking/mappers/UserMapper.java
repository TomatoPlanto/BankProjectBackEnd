package com.banking.mappers;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .infix(user.getInfix())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static User toEntity(RegisterRequestDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .infix(dto.getInfix())
                .lastName(dto.getLastName())
                .bsn(dto.getBsn())
                .phoneNumber(dto.getPhoneNumber())
                .role(UserRole.CUSTOMER)
                .build();
    }
}