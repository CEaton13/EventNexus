package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.AddMemberRequest;
import com.app.eventnexus.dtos.requests.CreateOrganizationRequest;
import com.app.eventnexus.dtos.responses.OrganizationMemberResponse;
import com.app.eventnexus.dtos.responses.OrganizationResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller for organization management.
 * Organization lookup ({@code GET /{slug}}) is public.
 * All write and list operations require {@code TOURNAMENT_ADMIN}.
 * All business logic is delegated to {@link OrganizationService}.
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Returns all organizations on the platform.
     * Restricted to platform administrators.
     *
     * @return 200 OK with a list of all organizations
     */
    @GetMapping
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAll());
    }

    /**
     * Returns a single organization by its URL slug.
     * No authentication required — allows public discovery of org existence.
     *
     * @param slug the org's URL slug
     * @return 200 OK with the organization, or 404 if not found
     */
    @GetMapping("/{slug}")
    public ResponseEntity<OrganizationResponse> getOrganizationBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(organizationService.getBySlug(slug));
    }

    /**
     * Creates a new organization.
     * The authenticated admin is automatically added as {@code ORG_ADMIN}.
     *
     * @param request        organization details (name, slug, contactEmail)
     * @param authentication the current user's security context
     * @return 201 Created with the new organization
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request, principal.getUserId()));
    }

    /**
     * Returns all members of a given organization.
     *
     * @param orgId the organization's primary key
     * @return 200 OK with the member list
     */
    @GetMapping("/{orgId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrganizationMemberResponse>> getMembers(@PathVariable Long orgId) {
        return ResponseEntity.ok(organizationService.getMembers(orgId));
    }

    /**
     * Adds a user to an organization.
     *
     * @param orgId   the organization's primary key
     * @param request contains the user ID and desired org role
     * @return 201 Created with the new membership entry
     */
    @PostMapping("/{orgId}/members")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<OrganizationMemberResponse> addMember(
            @PathVariable Long orgId,
            @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.addMember(orgId, request));
    }

    /**
     * Removes a user from an organization.
     *
     * @param orgId  the organization's primary key
     * @param userId the user's primary key
     * @return 204 No Content on success
     */
    @DeleteMapping("/{orgId}/members/{userId}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> removeMember(@PathVariable Long orgId,
                                             @PathVariable Long userId) {
        organizationService.removeMember(orgId, userId);
        return ResponseEntity.noContent().build();
    }
}
