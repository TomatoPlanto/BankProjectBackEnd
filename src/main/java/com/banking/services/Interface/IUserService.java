package com.banking.services.Interface;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    UserResponseDTO register(RegisterRequestDTO request);

    UserResponseDTO getUserById(UUID userId);

    // Resolves JWT email to full profile — used by /api/users/me
    UserResponseDTO getUserByEmail(String email);

    List<UserResponseDTO> getAllUsers();

    List<UserResponseDTO> getUsersByStatus(UserStatus status);

    // Search by first or last name — employee user search feature
    List<UserResponseDTO> searchByName(String name);

    UserResponseDTO approveUser(UUID userId);

    UserResponseDTO closeUser(UUID userId);
}