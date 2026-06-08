package com.banking.services.Interface;

import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.request.UpdateBalanceRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.dto.response.LoginResponseDTO;

public interface IAtmService {

    AccountResponseDTO atmLogin(AtmLoginRequestDTO request);

    LoginResponseDTO updateBalance(UpdateBalanceRequestDTO request);
}
