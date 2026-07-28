package com.anyservice.backend.controller.dto;

public class UserUpdateDto {
    private String name;
    private String bio;
    private String phone;
    private Boolean phoneVisible;
    private String username;

    public UserUpdateDto() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getPhoneVisible() {
        return phoneVisible;
    }

    public void setPhoneVisible(Boolean phoneVisible) {
        this.phoneVisible = phoneVisible;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
