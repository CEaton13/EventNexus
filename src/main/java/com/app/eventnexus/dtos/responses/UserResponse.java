package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.models.User;

import java.time.LocalDateTime;

/**
 * Response DTO carrying public-safe user information.
 * Never exposes password_hash or internal fields.
 */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code UserResponse} from a {@link User} entity.
     *
     * @param user the source entity
     * @return a populated response DTO
     */
    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public UserResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
