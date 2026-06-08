package com.banking.services.Interface;

import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountLimitsRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IAccountService {

    AccountResponseDTO createAccount(CreateAccountRequestDTO request);

    AccountResponseDTO getAccountById(UUID accountId);

    AccountResponseDTO getAccountByIban(String iban);

    List<AccountResponseDTO> getAccountsByUserId(UUID userId);

    List<AccountResponseDTO> getAllAccounts();

    AccountResponseDTO updateLimits(UUID accountId, UpdateAccountLimitsRequestDTO request);

    AccountResponseDTO closeAccount(UUID accountId);

    AccountResponseDTO reactivateAccount(UUID accountId);
}