package com.banking.services;

import com.banking.models.dto.request.UpdateBalanceRequestDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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
    public LoginResponseDTO atmLogin(AtmLoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIban(),
                        request.getPin()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getIban());

        String token = jwtService.generateToken(userDetails);

        String role = atmRepository.findByIban(request.getIban())
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getRole()
                .name();

        return LoginResponseDTO.builder()
                .token(token)
                .role(role)
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
