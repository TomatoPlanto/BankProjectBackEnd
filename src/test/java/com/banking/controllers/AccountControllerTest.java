package com.banking.controllers;

import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.GlobalExceptionHandler;
import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.dto.response.IbanLookupResponseDTO;
import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import com.banking.services.Interface.IAccountService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock IAccountService accountService;
    @InjectMocks AccountController controller;

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

    private AccountResponseDTO sampleAccount(UUID id) {
        return AccountResponseDTO.builder()
                .accountId(id)
                .iban("NL00INHO0123456789")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("100.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .ownerName("John Doe")
                .build();
    }

    @Test
    void createAccount_returns201() throws Exception {
        CreateAccountRequestDTO req = new CreateAccountRequestDTO();
        req.setUserId(UUID.randomUUID());
        req.setAccountType(AccountType.CHECKING);
        req.setDailyLimit(new BigDecimal("1000.00"));
        req.setTransferLimit(new BigDecimal("500.00"));
        req.setAbsoluteMinimum(BigDecimal.ZERO);
        when(accountService.createAccount(any())).thenReturn(sampleAccount(UUID.randomUUID()));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban").value("NL00INHO0123456789"))
                .andExpect(jsonPath("$.ownerName").value("John Doe"));
    }

    @Test
    void createAccount_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // @NotNull fields missing
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccountById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.getAccountById(id)).thenReturn(sampleAccount(id));

        mockMvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(id.toString()));
    }

    @Test
    void getAccountById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.getAccountById(id)).thenThrow(new AccountNotFoundException("not found"));

        mockMvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAccounts_callsService() throws Exception {
        when(accountService.getAllAccounts(any()))
                .thenReturn(new PageImpl<>(List.of(sampleAccount(UUID.randomUUID()))));

        mockMvc.perform(get("/api/accounts?page=0&size=20"));

        verify(accountService).getAllAccounts(any()); // controller delegated to the service
    }

    @Test
    void getAccountsByUserId_returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(accountService.getAccountsByUserId(userId))
                .thenReturn(List.of(sampleAccount(UUID.randomUUID())));

        mockMvc.perform(get("/api/accounts/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL00INHO0123456789"));
    }

    @Test
    void getAccountByIban_returnsMinimalLookupWithoutBalance() throws Exception {
        when(accountService.getAccountByIban("NL00INHO0123456789"))
                .thenReturn(new IbanLookupResponseDTO(UUID.randomUUID(), "NL00INHO0123456789", "John Doe"));

        mockMvc.perform(get("/api/accounts/iban/{iban}", "NL00INHO0123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("John Doe"))
                .andExpect(jsonPath("$.balance").doesNotExist());
    }

    @Test
    void searchAccounts_returns200() throws Exception {
        when(accountService.searchAccountsByOwner("john"))
                .thenReturn(List.of(new IbanLookupResponseDTO(UUID.randomUUID(), "NL00INHO0123456789", "John Doe")));

        mockMvc.perform(get("/api/accounts/search").param("name", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerName").value("John Doe"));
    }

    @Test
    void updateAccount_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountRequestDTO req = new UpdateAccountRequestDTO();
        req.setStatus(AccountStatus.CLOSED);

        AccountResponseDTO updated = sampleAccount(id);
        updated.setStatus(AccountStatus.CLOSED);
        when(accountService.updateAccount(eq(id), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}