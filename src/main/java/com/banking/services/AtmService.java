package com.banking.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.LoginResponseDTO;
import com.banking.repositories.AtmRepository;
import com.banking.repositories.UserRepository;
import com.banking.security.JwtService;
import com.banking.services.Interface.IAtmService;

@Service
public class AtmService implements IAtmService {

    private final AuthenticationManager authenticationManager;
    private final AtmRepository atmRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AtmService(AtmRepository atmRepository, UserRepository userRepository, JwtService jwtService, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.atmRepository = atmRepository;
        this.userRepository = userRepository;
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

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String token = jwtService.generateToken(userDetails);

        String role = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getRole()
                .name();

        return LoginResponseDTO.builder()
                .token(token)
                .role(role)
                .build();
    }
}
