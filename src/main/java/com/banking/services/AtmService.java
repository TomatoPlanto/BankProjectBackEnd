package com.banking.services;

import com.banking.models.dto.request.UpdateBalanceRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.entities.Account;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.LoginResponseDTO;
import com.banking.repositories.AtmRepository;
import com.banking.security.JwtService;
import com.banking.services.Interface.IAtmService;

@Service
public class AtmService implements IAtmService {

    private final AuthenticationManager authenticationManager;
    private final AtmRepository atmRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AtmService(AtmRepository atmRepository, JwtService jwtService, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.atmRepository = atmRepository;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AccountResponseDTO atmLogin(AtmLoginRequestDTO request) {

        System.out.println("Step 1");

        Account account = atmRepository.findByIban(request.getIban())
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        if (!account.getPin().equals(request.getPin())) {
            throw new BadCredentialsException("Invalid PIN");
        }

        System.out.println("Step 2");

//        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getIban());
//
//        System.out.println("Step 3");
//
//        String token = jwtService.generateToken(userDetails);

        System.out.println("Step 4");

        return AccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .iban(account.getIban())
                .balance(account.getBalance())
                .build();
    }

    @Override
    public LoginResponseDTO updateBalance(UpdateBalanceRequestDTO request) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
//
//        user.setStatus(UserStatus.ACTIVE);
//        User savedUser = userRepository.save(user);
//        return UserMapper.toDTO(savedUser);
        return null;
    }
}
