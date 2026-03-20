package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.Organization;

import java.time.LocalDateTime;

/**
 * Response DTO for {@link Organization} data.
 * Entities never leave the service layer — this DTO is the API surface.
 */
public class OrganizationResponse {

    private Long id;
    private String name;
    private String slug;
    private String contactEmail;
    private LocalDateTime createdAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public OrganizationResponse() {
    }

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Creates a response DTO from the given {@link Organization} entity.
     *
     * @param org the organization entity
     * @return a populated response DTO
     */
    public static OrganizationResponse from(Organization org) {
        OrganizationResponse dto = new OrganizationResponse();
        dto.id = org.getId();
        dto.name = org.getName();
        dto.slug = org.getSlug();
        dto.contactEmail = org.getContactEmail();
        dto.createdAt = org.getCreatedAt();
        return dto;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
