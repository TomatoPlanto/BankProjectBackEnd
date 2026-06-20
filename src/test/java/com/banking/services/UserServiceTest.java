package com.banking.services;

import com.banking.exceptions.DuplicateBsnException;
import com.banking.exceptions.DuplicateEmailException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;
import com.banking.models.enums.UserStatus;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock AccountRepository accountRepository;
    @Mock AccountService accountService;
    @InjectMocks UserService userService;

    private RegisterRequestDTO registerRequest() {
        RegisterRequestDTO r = new RegisterRequestDTO();
        r.setEmail("new@email.com");
        r.setPassword("pw");
        r.setFirstName("New");
        r.setLastName("User");
        r.setBsn("123456789");
        r.setPhoneNumber("0600000000");
        return r;
    }

    @Test
    void register_success_hashesPasswordAndSavesCustomer() {
        RegisterRequestDTO req = registerRequest();
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.existsByBsn("123456789")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO dto = userService.register(req);

        assertEquals("new@email.com", dto.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getPasswordHash());
        assertEquals(UserRole.CUSTOMER, captor.getValue().getRole());
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(DuplicateEmailException.class, () -> userService.register(registerRequest()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateBsn_throws() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByBsn(any())).thenReturn(true);
        assertThrows(DuplicateBsnException.class, () -> userService.register(registerRequest()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void approveUser_setsActiveAndCreatesCheckingAndSavings() {
        UUID id = UUID.randomUUID();
        User pending = User.builder()
                .userId(id).email("p@e.com").firstName("Pending").lastName("User")
                .role(UserRole.CUSTOMER).status(UserStatus.PENDING).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(pending));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountService.generateIban()).thenReturn("NL00INHO0000000001", "NL00INHO0000000002");

        UserResponseDTO dto = userService.approveUser(id);

        assertEquals(UserStatus.ACTIVE, dto.getStatus());
        verify(accountRepository, times(2)).save(any(Account.class)); // checking + savings
    }

    @Test
    void approveUser_missing_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.approveUser(id));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void closeUser_setsClosed() {
        UUID id = UUID.randomUUID();
        User active = User.builder()
                .userId(id).email("a@b.com").firstName("A").lastName("B").status(UserStatus.ACTIVE).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(active));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO dto = userService.closeUser(id);
        assertEquals(UserStatus.CLOSED, dto.getStatus());
    }

    @Test
    void getUserById_missing_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    void getUserByEmail_missing_throws() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("x@y.com"));
    }

    @Test
    void searchUsers_mapsResults() {
        User jane = User.builder()
                .userId(UUID.randomUUID()).email("j@e.com").firstName("Jane").lastName("Doe").build();
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jane", "jane"))
                .thenReturn(List.of(jane));

        List<UserResponseDTO> result = userService.searchUsers("jane");

        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }
}