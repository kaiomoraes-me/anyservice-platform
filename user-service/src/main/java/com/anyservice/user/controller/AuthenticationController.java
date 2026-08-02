package com.anyservice.user.controller;

import com.anyservice.user.controller.dto.AuthenticationRequest;
import com.anyservice.user.controller.dto.AuthenticationResponse;
import com.anyservice.user.controller.dto.MessageResponse;
import com.anyservice.user.controller.dto.RegisterRequest;
import com.anyservice.user.controller.dto.VerifyAccountRequest;
import com.anyservice.user.controller.dto.ForgotPasswordRequest;
import com.anyservice.user.controller.dto.ResetPasswordRequest;
import com.anyservice.user.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes public endpoints for user registration, authentication, and password recovery. */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    /** Registers a new user and sends an email verification code. */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    /** Authenticates a user and returns a JWT token. */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    /** Verifies a user's account using the code sent to their email. */
    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verifyAccount(
            @RequestBody VerifyAccountRequest request
    ) {
        return ResponseEntity.ok(service.verifyAccount(request));
    }

    /** Initiates the password recovery flow by sending a reset code via email. */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.forgotPassword(request));
    }

    /** Completes the password recovery flow by validating the reset code and updating the password. */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(service.resetPassword(request));
    }
}
