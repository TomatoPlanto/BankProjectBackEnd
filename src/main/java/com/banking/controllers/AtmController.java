package com.banking.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.models.dto.request.LoginRequestDTO;
import com.banking.models.dto.response.LoginResponseDTO;
import com.banking.services.Interface.IAtmService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/atm")
public class AtmController {

    private final IAtmService atmService;

    public AtmController(IAtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> atmLogin(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = atmService.atmLogin(request);
        return ResponseEntity.ok(response);
    }
}
