package com.banking.controllers;

import com.banking.exceptions.DuplicateEmailException;
import com.banking.exceptions.GlobalExceptionHandler;
import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.enums.UserRole;
import com.banking.models.enums.UserStatus;
import com.banking.services.Interface.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock IUserService userService;
    @InjectMocks UserController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // configured converter so PageImpl serializes cleanly in standalone setup
        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();
        jackson.getObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(jackson)
                .build();
    }

    private UserResponseDTO sampleUser(UUID id) {
        return UserResponseDTO.builder()
                .userId(id)
                .email("john@email.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build(); // createdAt left null on purpose
    }

    private RegisterRequestDTO validRegisterRequest() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setEmail("new@email.com");
        req.setPassword("password123");   // >= 8 chars
        req.setFirstName("New");
        req.setLastName("User");
        req.setBsn("123456789");           // exactly 9
        req.setPhoneNumber("0612345678");
        return req;
    }

    @Test
    void register_returns201() throws Exception {
        when(userService.register(any())).thenReturn(sampleUser(UUID.randomUUID()));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@email.com"));
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        RegisterRequestDTO bad = validRegisterRequest();
        bad.setEmail("not-an-email");
        bad.setPassword("short"); // < 8

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(userService.register(any())).thenThrow(new DuplicateEmailException("Email already in use"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenReturn(sampleUser(id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(id.toString()));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("not found"));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_noStatus_callsGetAllUsers() throws Exception {
        when(userService.getAllUsers(any())).thenReturn(new PageImpl<>(List.of(sampleUser(UUID.randomUUID()))));

        mockMvc.perform(get("/api/users?page=0&size=20"));

        verify(userService).getAllUsers(any());
        verify(userService, never()).getUsersByStatus(any(), any());
    }

    @Test
    void getUsers_withStatus_callsGetUsersByStatus() throws Exception {
        when(userService.getUsersByStatus(eq(UserStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(sampleUser(UUID.randomUUID()))));

        mockMvc.perform(get("/api/users").param("status", "PENDING"));

        verify(userService).getUsersByStatus(eq(UserStatus.PENDING), any());
        verify(userService, never()).getAllUsers(any());
    }

    @Test
    void searchUsers_returns200() throws Exception {
        when(userService.searchUsers("jane")).thenReturn(List.of(sampleUser(UUID.randomUUID())));

        mockMvc.perform(get("/api/users/search").param("name", "jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void approveUser_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.approveUser(id)).thenReturn(sampleUser(id));

        mockMvc.perform(put("/api/users/{id}/approve", id))
                .andExpect(status().isOk());
        verify(userService).approveUser(id);
    }

    @Test
    void closeUser_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.closeUser(id)).thenReturn(sampleUser(id));

        mockMvc.perform(put("/api/users/{id}/close", id))
                .andExpect(status().isOk());
        verify(userService).closeUser(id);
    }
}