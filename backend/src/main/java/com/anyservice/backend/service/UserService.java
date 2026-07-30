package com.anyservice.backend.service;

import com.anyservice.backend.controller.dto.UserProfileDto;
import com.anyservice.backend.controller.dto.UserUpdateDto;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Handles user profile operations and phone number management. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SmsService smsService;

    public UserService(UserRepository userRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    /** Retrieves the user profile by ID and returns its DTO representation. */
    public UserProfileDto getProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return mapToDto(user);
    }

    /** Updates the user profile details (name, bio, username) ensuring business rules. */
    public UserProfileDto updateProfile(User currentUser, UserUpdateDto updateDto) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (updateDto.getName() != null) user.setName(updateDto.getName());
        if (updateDto.getBio() != null) user.setBio(updateDto.getBio());
        if (updateDto.getPhoneVisible() != null) user.setPhoneVisible(updateDto.getPhoneVisible());
        
        if (updateDto.getUsername() != null && !updateDto.getUsername().equals(user.getUsernameIdentifier())) {
            if (user.getUsernameLastChangedAt() != null) {
                long daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(user.getUsernameLastChangedAt(), LocalDateTime.now());
                if (daysSinceLastChange < 14) {
                    throw new RuntimeException("Você só pode alterar seu username a cada 14 dias.");
                }
            }
            if (userRepository.existsByUsernameIdentifier(updateDto.getUsername())) {
                throw new RuntimeException("Este username já está em uso.");
            }
            user.setUsernameIdentifier(updateDto.getUsername());
            user.setUsernameLastChangedAt(LocalDateTime.now());
        }

        userRepository.save(user);
        return mapToDto(user);
    }

    /** Validates phone uniqueness and sends a 6-digit SMS code for verification. */
    public void sendPhoneVerificationCode(User currentUser, String fullPhone) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (userRepository.existsByPhoneAndIdNot(fullPhone, user.getId())) {
            throw new RuntimeException("Este número de telefone já está associado a outra conta.");
        }

        String code = String.format("%06d", new java.util.Random().nextInt(999999));

        user.setPendingPhone(fullPhone);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        smsService.sendVerificationCode(fullPhone, code);
    }

    /** Validates the SMS code and permanently links the phone to the user's account. */
    public void verifyPhoneCode(User currentUser, String code) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getPhoneVerificationCode() == null || user.getPendingPhone() == null) {
            throw new RuntimeException("Nenhuma verificação de telefone pendente.");
        }

        if (user.getPhoneVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado. Solicite um novo código.");
        }

        if (!user.getPhoneVerificationCode().equals(code)) {
            throw new RuntimeException("Código inválido.");
        }

        user.setPhone(user.getPendingPhone());
        user.setPhoneVerified(true);
        user.setPendingPhone(null);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationCodeExpiresAt(null);

        userRepository.save(user);
    }

    /** Converts a User entity to its UserProfileDto representation. */
    private UserProfileDto mapToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setBio(user.getBio());
        dto.setPhone(user.getPhone());
        dto.setPhoneVisible(user.getPhoneVisible() != null ? user.getPhoneVisible() : false);
        dto.setPhoneVerified(user.getPhoneVerified() != null ? user.getPhoneVerified() : false);
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setUsername(user.getUsernameIdentifier());
        return dto;
    }
}
