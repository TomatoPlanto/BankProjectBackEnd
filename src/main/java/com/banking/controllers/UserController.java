package com.banking.controllers;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.enums.UserStatus;
import com.banking.services.Interface.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    // Public — no token needed
    @PostMapping("/register")
    @Operation(summary = "Register as a new customer")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    /*
     * Customer calls this to get their own profile.
     * Avoids hitting GET /api/users which is employee-only.
     */
    @GetMapping("/me")
    @Operation(summary = "Get own profile from JWT")
    public ResponseEntity<UserResponseDTO> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getUserByEmail(userDetails.getUsername()));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get any user by ID — employee only")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // optional ?status=PENDING filter, otherwise returns everyone
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "List users, optionally filtered by status — employee only")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(status == null
                ? userService.getAllUsers(pageable)
                : userService.getUsersByStatus(status, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Search users by first or last name — employee only")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(@RequestParam String name) {
        return ResponseEntity.ok(userService.searchUsers(name));
    }

    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Approve pending customer, auto-creates checking + savings")
    public ResponseEntity<UserResponseDTO> approveUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.approveUser(userId));
    }

    @PutMapping("/{userId}/close")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Close a customer account")
    public ResponseEntity<UserResponseDTO> closeUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.closeUser(userId));
    }
}