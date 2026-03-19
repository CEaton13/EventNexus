package com.app.eventnexus.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link OrganizationMember}.
 * Combines {@code organization_id} and {@code user_id}.
 */
@Embeddable
public class OrganizationMemberId implements Serializable {

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "user_id")
    private Long userId;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public OrganizationMemberId() {
    }

    public OrganizationMemberId(Long organizationId, Long userId) {
        this.organizationId = organizationId;
        this.userId = userId;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // ─── equals / hashCode ────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationMemberId)) return false;
        OrganizationMemberId that = (OrganizationMemberId) o;
        return Objects.equals(organizationId, that.organizationId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, userId);
    }
}
