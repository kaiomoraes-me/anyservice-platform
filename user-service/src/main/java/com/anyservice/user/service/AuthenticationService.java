package com.anyservice.user.service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.anyservice.user.controller.dto.*;
import com.anyservice.user.model.Role;
import com.anyservice.user.model.User;
import com.anyservice.user.repository.UserRepository;
import com.anyservice.user.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Handles user registration, login, email verification, and password reset flows. */
@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder,
                                  JwtService jwtService, AuthenticationManager authenticationManager,
                                  EmailService emailService, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Generates a random 6-digit verification code. */
    private String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /** Registers a new user, sends email verification code, and returns a confirmation message. */
    public MessageResponse register(RegisterRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        String username = request.getUsername();
        if (username == null || !username.matches("^[a-z0-9_]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O nome de usuário (@) deve conter apenas letras minúsculas, números e underline (_).");
        }

        if (repository.existsByUsernameIdentifier(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este nome de usuário (@) já está em uso.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsernameIdentifier(username);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        String requestedRole = request.getRole().toUpperCase();
        if ("CLIENT".equals(requestedRole)) {
            requestedRole = "USER";
        }
        user.setRole(Role.valueOf(requestedRole));
        user.setEnabled(false);

        String code = generateRandomCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        repository.save(user);
        rabbitTemplate.convertAndSend("anyservice_events", "user.created", user.getId().toString());
        emailService.sendVerificationCode(user.getEmail(), code);

        return new MessageResponse("Conta criada com sucesso. Verifique seu e-mail para ativar.");
    }

    /** Authenticates the user by email/password and returns a JWT token with role claims. */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta não ativada. Verifique seu e-mail.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, user);
        return new AuthenticationResponse(jwtToken);
    }

    /** Activates a user account by validating the email verification code. */
    public MessageResponse verifyAccount(VerifyAccountRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (user.isEnabled()) {
            return new MessageResponse("Conta já está ativada.");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        repository.save(user);
        rabbitTemplate.convertAndSend("anyservice_events", "user.created", user.getId().toString());

        return new MessageResponse("Conta ativada com sucesso!");
    }

    /** Sends a password reset code to the user's email. */
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String code = generateRandomCode();
        user.setResetPasswordCode(code);
        user.setResetPasswordCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        repository.save(user);
        rabbitTemplate.convertAndSend("anyservice_events", "user.created", user.getId().toString());

        emailService.sendPasswordResetCode(user.getEmail(), code);

        return new MessageResponse("Código de recuperação enviado para o e-mail.");
    }

    /** Validates the reset code and updates the user's password. */
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (user.getResetPasswordCode() == null || !user.getResetPasswordCode().equals(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }

        if (user.getResetPasswordCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiresAt(null);
        repository.save(user);
        rabbitTemplate.convertAndSend("anyservice_events", "user.created", user.getId().toString());

        return new MessageResponse("Senha redefinida com sucesso!");
    }
}
