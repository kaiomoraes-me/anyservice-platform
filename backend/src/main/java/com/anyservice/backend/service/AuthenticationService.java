package com.anyservice.backend.service;

import com.anyservice.backend.controller.dto.AuthenticationRequest;
import com.anyservice.backend.controller.dto.AuthenticationResponse;
import com.anyservice.backend.controller.dto.RegisterRequest;
import com.anyservice.backend.model.Role;
import com.anyservice.backend.model.User;
import com.anyservice.backend.repository.UserRepository;
import com.anyservice.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    private String generateRandomCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }

    public com.anyservice.backend.controller.dto.MessageResponse register(RegisterRequest request) {
        // Verifica se o email já existe
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Email já cadastrado"
            );
        }

        String username = request.getUsername();
        if (username == null || !username.matches("^[a-z0-9_]+$")) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "O nome de usuário (@) deve conter apenas letras minúsculas, números e underline (_)."
            );
        }

        if (repository.existsByUsernameIdentifier(username)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Este nome de usuário (@) já está em uso."
            );
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsernameIdentifier(username);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setEnabled(false); // Inativo até verificar e-mail

        String code = generateRandomCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(15));
        
        repository.save(user);

        // Dispara o e-mail
        emailService.sendVerificationCode(user.getEmail(), code);

        return new com.anyservice.backend.controller.dto.MessageResponse("Conta criada com sucesso. Verifique seu e-mail para ativar.");
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Conta não ativada. Verifique seu e-mail.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public com.anyservice.backend.controller.dto.MessageResponse verifyAccount(com.anyservice.backend.controller.dto.VerifyAccountRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.isEnabled()) {
            return new com.anyservice.backend.controller.dto.MessageResponse("Conta já está ativada.");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Código inválido");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        repository.save(user);

        return new com.anyservice.backend.controller.dto.MessageResponse("Conta ativada com sucesso!");
    }

    public com.anyservice.backend.controller.dto.MessageResponse forgotPassword(com.anyservice.backend.controller.dto.ForgotPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String code = generateRandomCode();
        user.setResetPasswordCode(code);
        user.setResetPasswordCodeExpiresAt(java.time.LocalDateTime.now().plusMinutes(15));
        repository.save(user);

        emailService.sendPasswordResetCode(user.getEmail(), code);

        return new com.anyservice.backend.controller.dto.MessageResponse("Código de recuperação enviado para o e-mail.");
    }

    public com.anyservice.backend.controller.dto.MessageResponse resetPassword(com.anyservice.backend.controller.dto.ResetPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getResetPasswordCode() == null || !user.getResetPasswordCode().equals(request.getCode())) {
            throw new RuntimeException("Código inválido");
        }

        if (user.getResetPasswordCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiresAt(null);
        repository.save(user);

        return new com.anyservice.backend.controller.dto.MessageResponse("Senha redefinida com sucesso!");
    }
}
