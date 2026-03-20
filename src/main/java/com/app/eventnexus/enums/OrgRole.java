package com.app.eventnexus.enums;

/**
 * Represents a user's role within a specific organization.
 * Maps to the {@code org_role} PostgreSQL enum type.
 *
 * <p>This is distinct from the platform-level {@link UserRole}: a user may be
 * {@code ORG_ADMIN} in one organization and {@code ORG_MEMBER} in another.
 */
public enum OrgRole {

    /** Full administrative access within the organization. */
    ORG_ADMIN,

    /** Standard membership — can participate but cannot manage the org. */
    ORG_MEMBER
}
