package com.anyservice.backend.service;

import com.anyservice.backend.controller.dto.UserProfileDto;
import com.anyservice.backend.controller.dto.UserUpdateDto;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SmsService smsService;

    public UserService(UserRepository userRepository, SmsService smsService) {
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    public UserProfileDto getProfile(User currentUser) {
        // Garantir que temos os dados mais atualizados do banco
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return mapToDto(user);
    }

    public UserProfileDto updateProfile(User currentUser, UserUpdateDto updateDto) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (updateDto.getName() != null) user.setName(updateDto.getName());
        if (updateDto.getBio() != null) user.setBio(updateDto.getBio());
        if (updateDto.getPhoneVisible() != null) user.setPhoneVisible(updateDto.getPhoneVisible());
        // NOTA: O telefone NÃO é alterado aqui. Só pode ser alterado via verificação SMS.
        
        // Lógica do Username
        if (updateDto.getUsername() != null && !updateDto.getUsername().equals(user.getUsernameIdentifier())) {
            // Verificar regra dos 14 dias
            if (user.getUsernameLastChangedAt() != null) {
                long daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(user.getUsernameLastChangedAt(), LocalDateTime.now());
                if (daysSinceLastChange < 14) {
                    throw new RuntimeException("Você só pode alterar seu username a cada 14 dias.");
                }
            }
            // Verificar unicidade
            if (userRepository.existsByUsernameIdentifier(updateDto.getUsername())) {
                throw new RuntimeException("Este username já está em uso.");
            }
            user.setUsernameIdentifier(updateDto.getUsername());
            user.setUsernameLastChangedAt(LocalDateTime.now());
        }

        userRepository.save(user);

        return mapToDto(user);
    }

    /**
     * Envia código de verificação por SMS para o número informado.
     * Valida unicidade do número antes de enviar.
     */
    public void sendPhoneVerificationCode(User currentUser, String fullPhone) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se o número já está em uso por OUTRA conta
        if (userRepository.existsByPhoneAndIdNot(fullPhone, user.getId())) {
            throw new RuntimeException("Este número de telefone já está associado a outra conta.");
        }

        // Gerar código de 6 dígitos
        String code = String.format("%06d", new java.util.Random().nextInt(999999));

        // Armazenar os dados temporários
        user.setPendingPhone(fullPhone);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        // "Enviar" SMS (simulado — log no console)
        smsService.sendVerificationCode(fullPhone, code);
    }

    /**
     * Verifica o código SMS e, se correto, associa o número à conta.
     */
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

        // Código correto! Associar o número à conta.
        user.setPhone(user.getPendingPhone());
        user.setPhoneVerified(true);

        // Limpar campos temporários
        user.setPendingPhone(null);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationCodeExpiresAt(null);

        userRepository.save(user);
    }

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
