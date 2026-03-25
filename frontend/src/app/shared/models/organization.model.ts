/** Shape of an organization returned by GET /api/organizations/{slug}. */
export interface Organization {
  id: number;
  name: string;
  slug: string;
  contactEmail: string;
  createdAt: string;
}

/** Org membership entry included in the login response. */
export interface OrganizationMembership {
  organizationId: number;
  organizationName: string;
  organizationSlug: string;
  orgRole: 'ORG_ADMIN' | 'ORG_MEMBER';
}
