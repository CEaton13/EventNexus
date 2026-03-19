package com.app.eventnexus.models;

import com.app.eventnexus.enums.OrgRole;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * JPA entity representing a user's membership in an organization.
 * Maps to the {@code organization_members} table.
 *
 * <p>A user may be a member of multiple organizations, each with a different
 * {@link OrgRole}. This join table is the source of truth for per-org
 * authorization decisions made in {@code TenantFilter}.
 */
@Entity
@Table(name = "organization_members")
public class OrganizationMember {

    @EmbeddedId
    private OrganizationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "org_role", nullable = false, columnDefinition = "org_role")
    private OrgRole orgRole;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public OrganizationMember() {
    }

    public OrganizationMember(Organization organization, User user, OrgRole orgRole) {
        this.id = new OrganizationMemberId(organization.getId(), user.getId());
        this.organization = organization;
        this.user = user;
        this.orgRole = orgRole;
        this.joinedAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public OrganizationMemberId getId() {
        return id;
    }

    public void setId(OrganizationMemberId id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrgRole getOrgRole() {
        return orgRole;
    }

    public void setOrgRole(OrgRole orgRole) {
        this.orgRole = orgRole;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
