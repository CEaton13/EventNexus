package com.app.eventnexus.tenant;

import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.TenantUnauthorizedException;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that resolves and validates the tenant (organization) for
 * every request that targets an org-scoped API path ({@code /api/orgs/{orgSlug}/...}).
 *
 * <p>Execution flow:
 * <ol>
 *   <li>Non-org-scoped paths (auth, genres, organizations meta) are passed through.</li>
 *   <li>The org slug is extracted from the URI segment after {@code /api/orgs/}.</li>
 *   <li>The slug is resolved to an {@link Organization} via the repository.</li>
 *   <li>If the user is authenticated, membership in the org is validated.
 *       Platform {@code TOURNAMENT_ADMIN} users bypass the membership check.</li>
 *   <li>{@link TenantContext#setTenantId(Long)} is called with the resolved org ID.</li>
 *   <li>The tenant ID is cleared in a {@code finally} block to prevent ThreadLocal leaks.</li>
 * </ol>
 *
 * <p>This filter runs <em>after</em> {@code JwtAuthenticationFilter} so the
 * {@link SecurityContextHolder} is already populated when membership is checked.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private static final String ORG_PATH_PREFIX = "/api/orgs/";

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    public TenantFilter(OrganizationRepository organizationRepository,
                        OrganizationMemberRepository organizationMemberRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Only process requests targeting org-scoped paths
        if (!uri.startsWith(ORG_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String orgSlug = extractOrgSlug(uri);
            if (orgSlug == null || orgSlug.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            Organization org = organizationRepository.findBySlug(orgSlug)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Organization with slug '" + orgSlug + "' not found."));

            validateMembership(org);

            TenantContext.setTenantId(org.getId());
            log.debug("Tenant resolved: org='{}' id={}", orgSlug, org.getId());

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Extracts the org slug from a URI of the form {@code /api/orgs/{slug}/...}.
     *
     * @param uri the full request URI
     * @return the slug segment, or null if the path is malformed
     */
    private String extractOrgSlug(String uri) {
        // uri = /api/orgs/evo-2026/tournaments/...
        String afterPrefix = uri.substring(ORG_PATH_PREFIX.length()); // "evo-2026/tournaments/..."
        int slashIndex = afterPrefix.indexOf('/');
        if (slashIndex == -1) {
            return afterPrefix; // just the slug, no sub-path
        }
        return afterPrefix.substring(0, slashIndex);
    }

    /**
     * Validates that the currently authenticated user is a member of the
     * resolved organization. Platform {@code TOURNAMENT_ADMIN} users are
     * treated as superusers and bypass the membership check.
     *
     * @param org the resolved organization
     * @throws TenantUnauthorizedException if the user is not a member of the org
     */
    private void validateMembership(Organization org) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            // Unauthenticated request — Spring Security will enforce auth on protected routes.
            // Public read routes (e.g. GET /api/orgs/{slug}/tournaments) are allowed through.
            return;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Platform TOURNAMENT_ADMIN is a superuser — can access any org
        if (principal.getRole() == UserRole.TOURNAMENT_ADMIN) {
            return;
        }

        boolean isMember = organizationMemberRepository
                .existsByIdOrganizationIdAndIdUserId(org.getId(), principal.getUserId());

        if (!isMember) {
            throw new TenantUnauthorizedException(
                    "You are not a member of organization '" + org.getSlug() + "'.");
        }
    }
}
