package com.banking.controllers;

import com.banking.models.dto.request.UpdateBalanceRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banking.models.dto.request.AtmLoginRequestDTO;
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
    public ResponseEntity<LoginResponseDTO> atmLogin(@Valid @RequestBody AtmLoginRequestDTO request) {
        LoginResponseDTO response = atmService.atmLogin(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateBalance")
    public ResponseEntity<LoginResponseDTO> updateBalance(@Valid @RequestBody UpdateBalanceRequestDTO request) { // need to fix the @ on this one
        LoginResponseDTO response = atmService.updateBalance(request);
        return ResponseEntity.ok(response);
    }
}
