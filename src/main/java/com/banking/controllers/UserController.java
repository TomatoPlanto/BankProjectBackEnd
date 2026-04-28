package com.banking.controllers;
import com.banking.services.Interface.IUserService;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.enums.UserStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId) {
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByStatus(@PathVariable UserStatus status) {
        List<UserResponseDTO> response = userService.getUsersByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/approve")
    public ResponseEntity<UserResponseDTO> approveUser(@PathVariable UUID userId) {
        UserResponseDTO response = userService.approveUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/close")
    public ResponseEntity<UserResponseDTO> closeUser(@PathVariable UUID userId) {
        UserResponseDTO response = userService.closeUser(userId);
        return ResponseEntity.ok(response);
    }
}