package com.anyservice.user.dto;

import java.util.Objects;

public class UserSearchDto {
    private Long id;
    private String name;
    private String username;
    private String profilePictureUrl;

    public UserSearchDto(Long id, String name, String username, String profilePictureUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getProfilePictureUrl() { return profilePictureUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSearchDto that = (UserSearchDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
