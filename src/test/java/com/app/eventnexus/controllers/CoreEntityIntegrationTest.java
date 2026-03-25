package com.app.eventnexus.controllers;

import com.app.eventnexus.config.CorsConfig;
import com.app.eventnexus.config.SecurityConfig;
import com.app.eventnexus.dtos.responses.GameGenreResponse;
import com.app.eventnexus.dtos.responses.PlayerResponse;
import com.app.eventnexus.dtos.responses.PlayerStatsResponse;
import com.app.eventnexus.dtos.responses.VenueResponse;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.security.JwtTokenProvider;
import com.app.eventnexus.services.GameGenreService;
import com.app.eventnexus.services.PlayerService;
import com.app.eventnexus.services.TeamService;
import com.app.eventnexus.services.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying that all public GET endpoints return 200 OK
 * without any authentication token.
 *
 * <p>Uses {@code @WebMvcTest} — no database is started. Services are mocked.
 * Spring Security is loaded via {@code @Import} to validate the security
 * configuration permits unauthenticated access to these routes.
 */
@WebMvcTest(controllers = {
        GenreController.class,
        VenueController.class,
        TeamController.class,
        PlayerController.class
})
@Import({SecurityConfig.class, CorsConfig.class})
class CoreEntityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ─── Mocked services ──────────────────────────────────────────────────────

    @MockitoBean
    private GameGenreService gameGenreService;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private PlayerService playerService;

    // Required by TenantFilter loaded with SecurityConfig
    @MockitoBean
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private OrganizationMemberRepository organizationMemberRepository;

    // Required by JwtAuthenticationFilter loaded with SecurityConfig
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void stubTenantFilter() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Test Org");
        org.setSlug("test-org");
        when(organizationRepository.findBySlug(anyString())).thenReturn(Optional.of(org));
    }

    // ─── Genres ───────────────────────────────────────────────────────────────

    @Test
    void getGenres_returns200WithoutAuth() throws Exception {
        when(gameGenreService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk());
    }

    @Test
    void getGenreById_returns200WithoutAuth() throws Exception {
        when(gameGenreService.findById(1L)).thenReturn(new GameGenreResponse());

        mockMvc.perform(get("/api/genres/1"))
                .andExpect(status().isOk());
    }

    // ─── Venues ───────────────────────────────────────────────────────────────

    @Test
    void getVenues_returns200WithoutAuth() throws Exception {
        when(venueService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/orgs/test-org/venues"))
                .andExpect(status().isOk());
    }

    @Test
    void getVenueById_returns200WithoutAuth() throws Exception {
        VenueResponse venue = new VenueResponse();
        venue.setId(1L);
        venue.setName("Main Arena");
        when(venueService.findById(1L)).thenReturn(venue);

        mockMvc.perform(get("/api/orgs/test-org/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Arena"));
    }

    // ─── Teams (authenticated — not public) ──────────────────────────────────

    @Test
    void getTeams_returns401_whenNoAuthToken() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for anonymous access
    }

    @Test
    void getTeamById_returns401_whenNoAuthToken() throws Exception {
        mockMvc.perform(get("/api/teams/1"))
                .andExpect(status().isForbidden());
    }

    // ─── Players ──────────────────────────────────────────────────────────────

    @Test
    void getPlayers_returns200WithoutAuth() throws Exception {
        when(playerService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayerById_returns200WithoutAuth() throws Exception {
        PlayerResponse player = new PlayerResponse();
        player.setId(5L);
        player.setGamerTag("SniperX");
        player.setActive(true);
        when(playerService.findById(5L)).thenReturn(player);

        mockMvc.perform(get("/api/players/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gamerTag").value("SniperX"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getPlayerStats_returns200WithoutAuth() throws Exception {
        when(playerService.getStats(5L)).thenReturn(List.of(new PlayerStatsResponse()));

        mockMvc.perform(get("/api/players/5/stats"))
                .andExpect(status().isOk());
    }

}
