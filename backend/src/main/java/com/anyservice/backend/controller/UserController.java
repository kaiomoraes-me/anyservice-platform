package com.anyservice.backend.controller;

import com.anyservice.backend.controller.dto.PhoneSendCodeRequest;
import com.anyservice.backend.controller.dto.PhoneVerifyRequest;
import com.anyservice.backend.controller.dto.UserProfileDto;
import com.anyservice.backend.controller.dto.UserUpdateDto;
import com.anyservice.backend.controller.dto.MessageResponse;
import com.anyservice.backend.model.User;
import com.anyservice.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.anyservice.backend.service.FileStorageService;
import com.anyservice.backend.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

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

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateDto updateDto) {
        return ResponseEntity.ok(userService.updateProfile(currentUser, updateDto));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file) {
        
        String fileUrl = fileStorageService.saveAvatar(file);
        
        // Atualiza a URL no usuário
        User user = userRepository.findById(currentUser.getId()).orElseThrow();
        user.setProfilePictureUrl(fileUrl);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("profilePictureUrl", fileUrl));
    }

    @PostMapping("/me/phone/send-code")
    public ResponseEntity<MessageResponse> sendPhoneCode(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PhoneSendCodeRequest request) {
        
        String fullPhone = request.getCountryCode() + " " + request.getPhone();
        userService.sendPhoneVerificationCode(currentUser, fullPhone);
        
        return ResponseEntity.ok(new MessageResponse("Código de verificação enviado por SMS."));
    }

    @PostMapping("/me/phone/verify")
    public ResponseEntity<MessageResponse> verifyPhoneCode(
            @AuthenticationPrincipal User currentUser,
            @RequestBody PhoneVerifyRequest request) {
        
        userService.verifyPhoneCode(currentUser, request.getCode());
        
        return ResponseEntity.ok(new MessageResponse("Número de telefone verificado com sucesso!"));
    }
}
