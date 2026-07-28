package com.anyservice.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users") // "user" é uma palavra reservada no PostgreSQL, por isso usamos "users"
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "reset_password_code")
    private String resetPasswordCode;

    @Column(name = "reset_password_code_expires_at")
    private LocalDateTime resetPasswordCodeExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(unique = true)
    private String phone;
    
    @Column(name = "phone_visible")
    private Boolean phoneVisible = false;

    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    @Column(name = "pending_phone")
    private String pendingPhone;

    @Column(name = "phone_verification_code")
    private String phoneVerificationCode;

    @Column(name = "phone_verification_code_expires_at")
    private LocalDateTime phoneVerificationCodeExpiresAt;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // Usaremos "usernameIdentifier" para não conflitar com o getUsername() do Spring Security
    @Column(name = "username_identifier", unique = true)
    private String usernameIdentifier;

    @Column(name = "username_last_changed_at")
    private LocalDateTime usernameLastChangedAt;

    // Construtor vazio exigido pelo Hibernate (JPA)
    public User() {
    }

    // Este método é executado automaticamente antes de salvar no banco pela primeira vez
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiresAt() {
        return verificationCodeExpiresAt;
    }

    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    public String getResetPasswordCode() {
        return resetPasswordCode;
    }

    public void setResetPasswordCode(String resetPasswordCode) {
        this.resetPasswordCode = resetPasswordCode;
    }

    public LocalDateTime getResetPasswordCodeExpiresAt() {
        return resetPasswordCodeExpiresAt;
    }

    public void setResetPasswordCodeExpiresAt(LocalDateTime resetPasswordCodeExpiresAt) {
        this.resetPasswordCodeExpiresAt = resetPasswordCodeExpiresAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean getPhoneVisible() {
        return phoneVisible;
    }

    public void setPhoneVisible(Boolean phoneVisible) {
        this.phoneVisible = phoneVisible;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public String getPendingPhone() {
        return pendingPhone;
    }

    public void setPendingPhone(String pendingPhone) {
        this.pendingPhone = pendingPhone;
    }

    public String getPhoneVerificationCode() {
        return phoneVerificationCode;
    }

    public void setPhoneVerificationCode(String phoneVerificationCode) {
        this.phoneVerificationCode = phoneVerificationCode;
    }

    public LocalDateTime getPhoneVerificationCodeExpiresAt() {
        return phoneVerificationCodeExpiresAt;
    }

    public void setPhoneVerificationCodeExpiresAt(LocalDateTime phoneVerificationCodeExpiresAt) {
        this.phoneVerificationCodeExpiresAt = phoneVerificationCodeExpiresAt;
    }

    public String getUsernameIdentifier() {
        return usernameIdentifier;
    }

    public void setUsernameIdentifier(String usernameIdentifier) {
        this.usernameIdentifier = usernameIdentifier;
    }

    public LocalDateTime getUsernameLastChangedAt() {
        return usernameLastChangedAt;
    }

    public void setUsernameLastChangedAt(LocalDateTime usernameLastChangedAt) {
        this.usernameLastChangedAt = usernameLastChangedAt;
    }

    // --- MÉTODOS DO SPRING SECURITY (UserDetails) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converte nosso Enum Role em uma permissão que o Spring entenda
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // O email será o nosso "usuário" de login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
