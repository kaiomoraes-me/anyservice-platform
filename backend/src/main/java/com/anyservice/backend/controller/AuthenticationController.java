package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.AuthenticationRequest;
import com.anyservice.backend.controller.dto.AuthenticationResponse;
import com.anyservice.backend.controller.dto.RegisterRequest;
import com.anyservice.backend.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<com.anyservice.backend.controller.dto.MessageResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<com.anyservice.backend.controller.dto.MessageResponse> verifyAccount(
            @RequestBody com.anyservice.backend.controller.dto.VerifyAccountRequest request
    ) {
        return ResponseEntity.ok(service.verifyAccount(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<com.anyservice.backend.controller.dto.MessageResponse> forgotPassword(
            @RequestBody com.anyservice.backend.controller.dto.ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<com.anyservice.backend.controller.dto.MessageResponse> resetPassword(
            @RequestBody com.anyservice.backend.controller.dto.ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(service.resetPassword(request));
    }
}
