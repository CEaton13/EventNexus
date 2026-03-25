package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Organization} entities.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Looks up an organization by its URL slug.
     *
     * @param slug the URL-safe slug (e.g. "evo-2026")
     * @return the organization, or empty if none matched
     */
    Optional<Organization> findBySlug(String slug);

    /**
     * Returns true if any organization uses the given slug.
     *
     * @param slug the slug to check
     * @return true if the slug is already in use
     */
    boolean existsBySlug(String slug);
}
