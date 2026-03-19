package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.AddMemberRequest;
import com.app.eventnexus.dtos.requests.CreateOrganizationRequest;
import com.app.eventnexus.dtos.responses.OrganizationMemberResponse;
import com.app.eventnexus.dtos.responses.OrganizationResponse;
import com.app.eventnexus.enums.OrgRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.models.OrganizationMember;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing organizations and their memberships.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Organization slugs must be unique across the platform.</li>
 *   <li>The creator of an organization is automatically added as {@code ORG_ADMIN}.</li>
 *   <li>A user cannot be added to the same organization twice.</li>
 * </ul>
 */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMemberRepository organizationMemberRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns an organization by its URL slug.
     *
     * @param slug the URL-safe slug (e.g. "evo-2026")
     * @return the organization as a response DTO
     * @throws ResourceNotFoundException if no organization uses the given slug
     */
    @Transactional(readOnly = true)
    public OrganizationResponse getBySlug(String slug) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization with slug '" + slug + "' not found."));
        return OrganizationResponse.from(org);
    }

    /**
     * Returns all organizations on the platform.
     * Intended for platform-level {@code TOURNAMENT_ADMIN} use only.
     *
     * @return list of all organizations; never null
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAll() {
        return organizationRepository.findAll()
                .stream()
                .map(OrganizationResponse::from)
                .toList();
    }

    /**
     * Returns all organization memberships for a given user.
     * Used to populate the {@code organizations} field in the login response.
     *
     * @param userId the user's primary key
     * @return list of memberships with org name, slug, and role; never null
     */
    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> getMembershipsForUser(Long userId) {
        return organizationMemberRepository.findByIdUserId(userId)
                .stream()
                .map(OrganizationMemberResponse::from)
                .toList();
    }

    /**
     * Returns all members of a given organization.
     *
     * @param orgId the organization's primary key
     * @return list of member responses; never null
     * @throws ResourceNotFoundException if no organization exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> getMembers(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization", orgId);
        }
        return organizationMemberRepository.findByIdOrganizationId(orgId)
                .stream()
                .map(OrganizationMemberResponse::from)
                .toList();
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new organization and adds the creator as {@code ORG_ADMIN}.
     *
     * @param request       organization details (name, slug, contactEmail)
     * @param creatorUserId ID of the authenticated user creating the organization
     * @return the newly created organization as a response DTO
     * @throws ConflictException         if the slug is already in use
     * @throws ResourceNotFoundException if the creator user does not exist
     */
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request,
                                                   Long creatorUserId) {
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException(
                    "Organization slug '" + request.getSlug() + "' is already in use.");
        }

        Organization org = new Organization(
                request.getName(),
                request.getSlug(),
                request.getContactEmail());
        org = organizationRepository.save(org);

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorUserId));

        organizationMemberRepository.save(new OrganizationMember(org, creator, OrgRole.ORG_ADMIN));

        return OrganizationResponse.from(org);
    }

    /**
     * Adds a user to an organization with the specified role.
     *
     * @param orgId   the organization's primary key
     * @param request contains the user ID and desired org role
     * @return the new membership as a response DTO
     * @throws ResourceNotFoundException if the organization or user does not exist
     * @throws ConflictException         if the user is already a member of the organization
     */
    @Transactional
    public OrganizationMemberResponse addMember(Long orgId, AddMemberRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", orgId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (organizationMemberRepository.existsByIdOrganizationIdAndIdUserId(orgId, user.getId())) {
            throw new ConflictException(
                    "User '" + user.getUsername() + "' is already a member of this organization.");
        }

        OrgRole role = request.getOrgRole() != null ? request.getOrgRole() : OrgRole.ORG_MEMBER;
        OrganizationMember member = new OrganizationMember(org, user, role);
        return OrganizationMemberResponse.from(organizationMemberRepository.save(member));
    }

    /**
     * Removes a user from an organization.
     *
     * @param orgId  the organization's primary key
     * @param userId the user's primary key
     * @throws ResourceNotFoundException if no membership exists for the given org + user pair
     */
    @Transactional
    public void removeMember(Long orgId, Long userId) {
        OrganizationMember member = organizationMemberRepository
                .findByIdOrganizationIdAndIdUserId(orgId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership not found for org " + orgId + " and user " + userId));
        organizationMemberRepository.delete(member);
    }
}
