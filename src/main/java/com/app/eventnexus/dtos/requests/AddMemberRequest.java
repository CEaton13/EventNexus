package com.app.eventnexus.dtos.requests;

import com.app.eventnexus.enums.OrgRole;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a user to an organization.
 */
public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Org role is required")
    private OrgRole orgRole;

    public AddMemberRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrgRole getOrgRole() {
        return orgRole;
    }

    public void setOrgRole(OrgRole orgRole) {
        this.orgRole = orgRole;
    }
}
