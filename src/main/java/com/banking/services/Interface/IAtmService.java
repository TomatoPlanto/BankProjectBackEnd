package com.banking.services.Interface;

import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.LoginResponseDTO;

public interface IAtmService {

    public LoginResponseDTO atmLogin(AtmLoginRequestDTO request);
}
