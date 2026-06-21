package com.banking.policy;

import com.banking.exceptions.DuplicateBsnException;
import com.banking.exceptions.DuplicateEmailException;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class RegisterPolicy {

    private final UserRepository userRepository;

    public RegisterPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void enforceRegisterPolicy(RegisterRequestDTO request) {
        enforceEmailAvailable(request.getEmail());
        enforceBsnAvailable(request.getBsn());
    }

    public void enforceEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already in use");
        }
    }

    public void enforceBsnAvailable(String bsn) {
        if (userRepository.existsByBsn(bsn)) {
            throw new DuplicateBsnException("BSN already registered");
        }
    }
}
