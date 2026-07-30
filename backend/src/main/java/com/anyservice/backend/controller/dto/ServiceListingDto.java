package com.anyservice.backend.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceListingDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private ProviderDto provider;
    private LocalDateTime createdAt;

    // Classe aninhada para expor apenas os dados públicos do prestador
    public static class ProviderDto {
        private Long id;
        private String name;
        private String profilePictureUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ProviderDto getProvider() { return provider; }
    public void setProvider(ProviderDto provider) { this.provider = provider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
