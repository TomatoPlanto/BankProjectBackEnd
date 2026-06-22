package com.banking.controllers;

import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.AtmLoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banking.services.Interface.IAtmService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/atm")
public class AtmController {

    private final IAtmService atmService;

    public AtmController(IAtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping("/login")
    public ResponseEntity<AtmLoginResponseDTO> atmLogin(@Valid @RequestBody AtmLoginRequestDTO request) {
        AtmLoginResponseDTO response = atmService.atmLogin(request);
        return ResponseEntity.ok(response);
    }

}
