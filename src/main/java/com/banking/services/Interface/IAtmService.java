package com.banking.services.Interface;

import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.AtmLoginResponseDTO;

public interface IAtmService {

    AtmLoginResponseDTO atmLogin(AtmLoginRequestDTO request);
}
