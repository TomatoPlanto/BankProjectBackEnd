package com.banking.services;
import com.banking.models.entities.Account;
import com.banking.models.enums.AccountType;
import com.banking.services.Interface.IUserService;
import com.banking.exceptions.DuplicateBsnException;
import com.banking.exceptions.DuplicateEmailException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.mappers.UserMapper;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.User;
import com.banking.models.enums.UserStatus;
import com.banking.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.banking.models.entities.Account;
import com.banking.models.enums.AccountType;
import com.banking.repositories.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       AccountRepository accountRepository,
                       AccountService accountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }
        if (userRepository.existsByBsn(request.getBsn())) {
            throw new DuplicateBsnException("BSN already registered");
        }

        User user = UserMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return UserMapper.toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        // Auto-create CHECKING account
        Account checkingAccount = Account.builder()
                .user(savedUser)
                .iban(accountService.generateIban())
                .accountType(AccountType.CHECKING)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .build();
        accountRepository.save(checkingAccount);

        // Auto-create SAVINGS account
        Account savingsAccount = Account.builder()
                .user(savedUser)
                .iban(accountService.generateIban())
                .accountType(AccountType.SAVINGS)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .build();
        accountRepository.save(savingsAccount);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public UserResponseDTO closeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setStatus(UserStatus.CLOSED);
        User savedUser = userRepository.save(user);
        return UserMapper.toDTO(savedUser);
    }
}