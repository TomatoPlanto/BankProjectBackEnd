package com.banking.services;

import com.banking.exceptions.DuplicateBsnException;
import com.banking.exceptions.DuplicateEmailException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.mappers.UserMapper;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountType;
import com.banking.models.enums.UserStatus;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.Interface.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));
        return UserMapper.toDTO(user);
    }

    /*
     * Called by GET /api/users/me — avoids customer calling getAllUsers()
     * which is employee-only and would 403.
     */
    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));
        return UserMapper.toDTO(user);
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDTO);
    }

    @Override
    public Page<UserResponseDTO> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable).map(UserMapper::toDTO);
    }

    /*
     * Approving auto-creates both account types with sane defaults.
     * pin=0 is unset — employee sets real limits/PIN separately.
     */
    @Override
    public UserResponseDTO approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));

        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);

        accountRepository.save(Account.builder()
                .user(saved)
                .iban(accountService.generateIban())
                .accountType(AccountType.CHECKING)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .pin("0")
                .build());

        accountRepository.save(Account.builder()
                .user(saved)
                .iban(accountService.generateIban())
                .accountType(AccountType.SAVINGS)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .pin("0")
                .build());

        return UserMapper.toDTO(saved);
    }

    @Override
    public UserResponseDTO closeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));
        user.setStatus(UserStatus.CLOSED);
        return UserMapper.toDTO(userRepository.save(user));
    }
}