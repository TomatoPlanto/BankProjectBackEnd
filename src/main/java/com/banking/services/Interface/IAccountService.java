package com.banking.services.Interface;

import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.dto.response.IbanLookupResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAccountService {

    AccountResponseDTO createAccount(CreateAccountRequestDTO request);

    AccountResponseDTO getAccountById(UUID accountId);

    IbanLookupResponseDTO getAccountByIban(String iban);

    List<IbanLookupResponseDTO> searchAccountsByOwner(String name);

    List<AccountResponseDTO> getAccountsByUserId(UUID userId);

    Page<AccountResponseDTO> getAllAccounts(Pageable pageable);

    AccountResponseDTO updateAccount(UUID accountId, UpdateAccountRequestDTO request);
}