package com.banking.services;

import com.banking.models.dto.response.AtmLoginResponseDTO;
import com.banking.models.entities.Account;
import com.banking.security.AccountDetailsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.repositories.AtmRepository;
import com.banking.security.JwtService;
import com.banking.services.Interface.IAtmService;

@Service
public class AtmService implements IAtmService {

    private final AtmRepository atmRepository;
    private final JwtService jwtService;

    public AtmService(AtmRepository atmRepository, JwtService jwtService) {
        this.atmRepository = atmRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AtmLoginResponseDTO atmLogin(AtmLoginRequestDTO request) {

        Account account = atmRepository.findByIbanIgnoreCase(request.getIban().trim())
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        if (!account.getPin().equals(request.getPin())) {
            throw new BadCredentialsException("Invalid PIN");
        }

        String token = jwtService.generateToken(new AccountDetailsService(account));

        return AtmLoginResponseDTO.builder()
                .token(token)
                .accountId(account.getAccountId())
                .iban(account.getIban())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .dailyLimit(account.getDailyLimit())
                .transferLimit(account.getTransferLimit())
                .absoluteMinimum(account.getAbsoluteMinimum())
                .status(account.getStatus())
                .build();
    }
}
