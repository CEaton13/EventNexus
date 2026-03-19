package com.app.eventnexus.repositories;

import com.app.eventnexus.models.OrganizationMember;
import com.app.eventnexus.models.OrganizationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link OrganizationMember} entities.
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, OrganizationMemberId> {

    /**
     * Checks whether a user is a member of a given organization.
     *
     * @param organizationId the organization's primary key
     * @param userId         the user's primary key
     * @return true if the user belongs to the organization
     */
    boolean existsByIdOrganizationIdAndIdUserId(Long organizationId, Long userId);

    /**
     * Returns all organization memberships for a given user.
     * Used to populate the org list in the login response.
     *
     * @param userId the user's primary key
     * @return all memberships for the user; never null
     */
    List<OrganizationMember> findByIdUserId(Long userId);

    /**
     * Finds a specific membership record.
     *
     * @param organizationId the organization's primary key
     * @param userId         the user's primary key
     * @return the membership, or empty if none found
     */
    Optional<OrganizationMember> findByIdOrganizationIdAndIdUserId(Long organizationId, Long userId);

    /**
     * Returns all members of a given organization.
     *
     * @param organizationId the organization's primary key
     * @return all members; never null
     */
    List<OrganizationMember> findByIdOrganizationId(Long organizationId);
}
