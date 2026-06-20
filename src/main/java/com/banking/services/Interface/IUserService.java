package com.banking.services.Interface;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    UserResponseDTO register(RegisterRequestDTO request);

    UserResponseDTO getUserById(UUID userId);

    // Resolves JWT email to full profile — used by /api/users/me
    UserResponseDTO getUserByEmail(String email);

    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    Page<UserResponseDTO> getUsersByStatus(UserStatus status, Pageable pageable);

    List<UserResponseDTO> searchUsers(String name);

    UserResponseDTO approveUser(UUID userId);

    UserResponseDTO closeUser(UUID userId);
}