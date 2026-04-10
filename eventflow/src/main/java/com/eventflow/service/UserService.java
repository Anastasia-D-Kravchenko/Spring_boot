package com.eventflow.service;

import com.eventflow.dto.EventFlowDtos.*;
import com.eventflow.model.User;
import com.eventflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Registration ─────────────────────────────────────────────────────────

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        if (!dto.passwordsMatch()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        User user = new User(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                User.Role.USER
        );
        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getFullName(), saved.getEmail());
        return saved;
    }

    // ─── Admin: Data Adder Management ─────────────────────────────────────────

    @Transactional
    public User createDataAdder(CreateDataAdderDto dto, User createdByAdmin) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        User dataAdder = new User(
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                User.Role.DATA_ADDER
        );
        dataAdder.setPromotedAt(LocalDateTime.now());
        dataAdder.setPromotedBy(createdByAdmin.getFullName());
        User saved = userRepository.save(dataAdder);
        log.info("Data adder created: {} by admin {}", saved.getEmail(), createdByAdmin.getEmail());
        return saved;
    }

    @Transactional
    public User promoteToDataAdder(Long userId, User admin) {
        User user = findByIdOrThrow(userId);
        user.setRole(User.Role.DATA_ADDER);
        user.setPromotedAt(LocalDateTime.now());
        user.setPromotedBy(admin.getFullName());
        log.info("User {} promoted to DATA_ADDER by {}", user.getEmail(), admin.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User demoteToUser(Long userId) {
        User user = findByIdOrThrow(userId);
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Cannot demote an ADMIN");
        }
        user.setRole(User.Role.USER);
        log.info("User {} demoted to USER", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public void toggleActive(Long userId) {
        User user = findByIdOrThrow(userId);
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Cannot deactivate the ADMIN account");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
        log.info("User {} active status toggled to: {}", user.getEmail(), user.isActive());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findByIdOrThrow(userId);
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Cannot delete an ADMIN account");
        }
        userRepository.delete(user);
        log.info("User {} deleted", user.getEmail());
    }

    // ─── Profile ──────────────────────────────────────────────────────────────

    @Transactional
    public User updateProfile(Long userId, UserProfileDto dto) {
        User user = findByIdOrThrow(userId);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setBio(dto.getBio());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findByIdOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public Page<User> findAllUsers(int page, int size) {
        return userRepository.findByRoleIn(
                List.of(User.Role.USER, User.Role.DATA_ADDER),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    public List<User> findAllDataAdders() {
        return userRepository.findByRole(User.Role.DATA_ADDER);
    }

    public Page<User> searchUsers(String query, int page, int size) {
        return userRepository.searchUsers(query, PageRequest.of(page, size));
    }

    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    public long countTotal() {
        return userRepository.count();
    }
}
