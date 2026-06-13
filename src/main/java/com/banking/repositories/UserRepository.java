package com.banking.repositories;

import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;
import com.banking.models.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByBsn(String bsn);

    List<User> findByStatus(UserStatus status);

    List<User> findByRole(UserRole role);

    List<User> findByStatusAndRole(UserStatus status, UserRole role);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName
    );
}