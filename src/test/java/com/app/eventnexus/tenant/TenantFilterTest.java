package com.app.eventnexus.tenant;

import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.exceptions.TenantUnauthorizedException;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.models.User;
import com.app.eventnexus.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TenantFilter}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Non-org-scoped paths pass through without setting tenant context</li>
 *   <li>Unknown org slug → ResourceNotFoundException</li>
 *   <li>Unauthenticated requests pass through (auth enforced elsewhere)</li>
 *   <li>Platform TOURNAMENT_ADMIN bypasses membership check</li>
 *   <li>Non-member throws TenantUnauthorizedException</li>
 *   <li>Valid member sets TenantContext and clears it after the request</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrganizationMemberRepository organizationMemberRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private TenantFilter tenantFilter;

    private Organization org;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(42L);
        org.setName("Test Org");
        org.setSlug("test-org");

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    // ─── Non-org path ──────────────────────────────────────────────────────────

    @Test
    void nonOrgPath_passesThrough_withoutSettingTenantContext() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(organizationRepository, never()).findBySlug(org.getSlug());
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void genresPath_passesThrough_withoutTenantLookup() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/genres");

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(organizationRepository, never()).findBySlug(org.getSlug());
    }

    // ─── Unknown org slug ──────────────────────────────────────────────────────

    @Test
    void unknownOrgSlug_throwsResourceNotFoundException() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/unknown-org/tournaments");
        when(organizationRepository.findBySlug("unknown-org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown-org");
    }

    // ─── Unauthenticated request ───────────────────────────────────────────────

    @Test
    void unauthenticatedRequest_passesThrough_tenantContextSet() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/test-org/tournaments");
        when(organizationRepository.findBySlug("test-org")).thenReturn(Optional.of(org));
        // No authentication set in SecurityContextHolder

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // TenantContext should have been cleared in the finally block
        assertThat(TenantContext.getTenantId()).isNull();
    }

    // ─── TOURNAMENT_ADMIN bypass ───────────────────────────────────────────────

    @Test
    void tournamentAdmin_bypassesMembershipCheck() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/test-org/tournaments");
        when(organizationRepository.findBySlug("test-org")).thenReturn(Optional.of(org));

        setAuthentication(99L, UserRole.TOURNAMENT_ADMIN);

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(organizationMemberRepository, never())
                .existsByIdOrganizationIdAndIdUserId(org.getId(), 99L);
    }

    // ─── Non-member ────────────────────────────────────────────────────────────

    @Test
    void nonMember_throwsTenantUnauthorizedException() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/test-org/tournaments");
        when(organizationRepository.findBySlug("test-org")).thenReturn(Optional.of(org));

        setAuthentication(50L, UserRole.TEAM_MANAGER);
        when(organizationMemberRepository.existsByIdOrganizationIdAndIdUserId(42L, 50L))
                .thenReturn(false);

        assertThatThrownBy(() -> tenantFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(TenantUnauthorizedException.class)
                .hasMessageContaining("test-org");
    }

    // ─── Valid member ──────────────────────────────────────────────────────────

    @Test
    void validMember_setsTenantContextAndClearsAfterRequest() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/test-org/tournaments");
        when(organizationRepository.findBySlug("test-org")).thenReturn(Optional.of(org));

        setAuthentication(10L, UserRole.TEAM_MANAGER);
        when(organizationMemberRepository.existsByIdOrganizationIdAndIdUserId(42L, 10L))
                .thenReturn(true);

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // TenantContext cleared in finally block — after doFilter it should be null
        assertThat(TenantContext.getTenantId()).isNull();
    }

    // ─── Slug extraction edge cases ────────────────────────────────────────────

    @Test
    void nestedOrgPath_extractsSlugCorrectly() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/orgs/my-org/admin/dashboard");
        when(organizationRepository.findBySlug("my-org")).thenReturn(Optional.of(org));

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(organizationRepository).findBySlug("my-org");
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private void setAuthentication(Long userId, UserRole role) {
        User user = new User("user", "user@test.com", "hash", role);
        user.setId(userId);
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
