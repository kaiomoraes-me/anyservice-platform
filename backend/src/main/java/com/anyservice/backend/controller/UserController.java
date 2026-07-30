package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.PhoneSendCodeRequest;
import com.anyservice.backend.controller.dto.PhoneVerifyRequest;
import com.anyservice.backend.controller.dto.UserProfileDto;
import com.anyservice.backend.controller.dto.UserUpdateDto;
import com.anyservice.backend.controller.dto.MessageResponse;
import com.anyservice.backend.model.User;
import com.anyservice.backend.service.UserService;
import com.anyservice.backend.service.FileStorageService;
import com.anyservice.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/** Manages user profiles, avatars, and phone verification endpoints. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public UserController(UserService userService, FileStorageService fileStorageService, UserRepository userRepository) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    /** Returns the authenticated user's profile details. */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser));
    }

    /** Updates the authenticated user's profile information. */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateDto updateDto) {
        return ResponseEntity.ok(userService.updateProfile(currentUser, updateDto));
    }

    /** Uploads and updates the user's avatar image. */
    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file) {
        
        String fileUrl = fileStorageService.saveAvatar(file);
        
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        user.setProfilePictureUrl(fileUrl);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("profilePictureUrl", fileUrl));
    }

    /** Triggers an SMS verification code to the user's phone number. */
    @PostMapping("/me/phone/send-code")
    public ResponseEntity<MessageResponse> sendPhoneCode(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PhoneSendCodeRequest request) {
        
        String fullPhone = request.getCountryCode() + " " + request.getPhone();
        userService.sendPhoneVerificationCode(currentUser, fullPhone);
        
        return ResponseEntity.ok(new MessageResponse("Código de verificação enviado por SMS."));
    }

    /** Verifies the SMS code and associates the phone number with the account. */
    @PostMapping("/me/phone/verify")
    public ResponseEntity<MessageResponse> verifyPhoneCode(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PhoneVerifyRequest request) {
        
        userService.verifyPhoneCode(currentUser, request.getCode());
        
        return ResponseEntity.ok(new MessageResponse("Número de telefone verificado com sucesso!"));
    }
}
