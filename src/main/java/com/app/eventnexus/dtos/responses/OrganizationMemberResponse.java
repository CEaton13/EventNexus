package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.enums.OrgRole;
import com.app.eventnexus.models.OrganizationMember;

import java.time.LocalDateTime;

/**
 * Response DTO representing a user's membership in an organization.
 * Returned in the login response and member-list endpoints.
 */
public class OrganizationMemberResponse {

    private Long organizationId;
    private String organizationName;
    private String organizationSlug;
    private Long userId;
    private String username;
    private OrgRole orgRole;
    private LocalDateTime joinedAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public OrganizationMemberResponse() {
    }

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Creates a response DTO from the given {@link OrganizationMember} entity.
     *
     * @param member the membership entity
     * @return a populated response DTO
     */
    public static OrganizationMemberResponse from(OrganizationMember member) {
        OrganizationMemberResponse dto = new OrganizationMemberResponse();
        dto.organizationId = member.getOrganization().getId();
        dto.organizationName = member.getOrganization().getName();
        dto.organizationSlug = member.getOrganization().getSlug();
        dto.userId = member.getUser().getId();
        dto.username = member.getUser().getUsername();
        dto.orgRole = member.getOrgRole();
        dto.joinedAt = member.getJoinedAt();
        return dto;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationSlug() {
        return organizationSlug;
    }

    public void setOrganizationSlug(String organizationSlug) {
        this.organizationSlug = organizationSlug;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
