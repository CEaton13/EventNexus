package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.RegistrationRequest;
import com.app.eventnexus.dtos.requests.TournamentRequest;
import com.app.eventnexus.dtos.responses.RegistrationResponse;
import com.app.eventnexus.dtos.responses.TournamentResponse;
import com.app.eventnexus.enums.BackgroundStyle;
import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.enums.TournamentFormat;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.InvalidStateTransitionException;
import com.app.eventnexus.models.GameGenre;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.TournamentTeam;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.GameGenreRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.TournamentTeamRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.repositories.VenueRepository;
import com.app.eventnexus.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Service-level integration tests for the tournament lifecycle.
 *
 * <p>All repositories and the {@link BracketService} are mocked so no database
 * is needed. Tests verify the full status-transition chain and team-registration
 * rules enforced by {@link TournamentService}.
 *
 * <pre>
 *  DRAFT → REGISTRATION_OPEN → REGISTRATION_CLOSED → IN_PROGRESS → COMPLETED → ARCHIVED
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class TournamentLifecycleTest {

    // ─── Mocks ────────────────────────────────────────────────────────────────

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TournamentTeamRepository tournamentTeamRepository;
    @Mock private GameGenreRepository gameGenreRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private BracketService bracketService;

    @InjectMocks
    private TournamentService tournamentService;

    // ─── Shared fixtures ──────────────────────────────────────────────────────

    private static final long TOURNAMENT_ID = 1L;
    private static final long ADMIN_ID      = 10L;
    private static final long TENANT_ID     = 99L;

    private Tournament tournament;
    private User admin;
    private GameGenre genre;
    private Organization org;

    @BeforeEach
    void setUp() {
        // Populate TenantContext so TournamentService.create() can resolve the org
        TenantContext.setTenantId(TENANT_ID);

        admin = new User("admin1", "admin@test.com", "hash", UserRole.TOURNAMENT_ADMIN);
        admin.setId(ADMIN_ID);

        genre = new GameGenre();
        genre.setId(1L);
        genre.setName("FPS / Tactical");
        genre.setPrimaryColor("#FF0000");
        genre.setSecondaryColor("#00FF00");
        genre.setAccentColor("#0000FF");
        genre.setFontFamily("Arial");
        genre.setIconPackKey("fps");
        genre.setBackgroundStyle(BackgroundStyle.DARK);

        org = new Organization();
        org.setId(TENANT_ID);
        org.setName("Test Org");
        org.setSlug("test-org");

        tournament = new Tournament();
        tournament.setId(TOURNAMENT_ID);
        tournament.setName("Spring Championship");
        tournament.setGameTitle("Test Game");
        tournament.setStatus(TournamentStatus.DRAFT);
        tournament.setFormat(TournamentFormat.SINGLE_ELIMINATION);
        tournament.setMaxTeams(8);
        tournament.setGameGenre(genre);
        tournament.setCreatedBy(admin);
        tournament.setOrganization(org);
        tournament.setCreatedAt(LocalDateTime.now());
        tournament.setUpdatedAt(LocalDateTime.now());

        // save() returns the entity as-is so status mutations are visible.
        // Using lenient() because step1 overrides this stub with a custom answer.
        lenient().when(tournamentRepository.save(any(Tournament.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ─── Status transition: valid chain ───────────────────────────────────────

    @Test
    void step1_tournamentCreated_statusIsDraft() {
        TournamentRequest request = makeTournamentRequest();
        when(gameGenreRepository.findById(1L)).thenReturn(Optional.of(genre));
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(organizationRepository.findById(TENANT_ID)).thenReturn(Optional.of(org));
        when(tournamentRepository.save(any())).thenAnswer(inv -> {
            Tournament t = inv.getArgument(0);
            t.setId(TOURNAMENT_ID);
            return t;
        });

        TournamentResponse response = tournamentService.create(request, ADMIN_ID);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.DRAFT);
    }

    @Test
    void step2_draftToRegistrationOpen_succeeds() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.updateStatus(
                TOURNAMENT_ID, TournamentStatus.REGISTRATION_OPEN);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_OPEN);
    }

    @Test
    void step3_registerFourTeams_allSucceed() {
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        for (int i = 1; i <= 4; i++) {
            final long teamId = i;
            Team team = makeTeam(teamId, "Team " + teamId);
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(tournamentTeamRepository.findByTournamentIdAndTeamId(TOURNAMENT_ID, teamId))
                    .thenReturn(Optional.empty());
            when(tournamentTeamRepository.save(any(TournamentTeam.class)))
                    .thenAnswer(inv -> {
                        TournamentTeam tt = inv.getArgument(0);
                        tt.setId(teamId);
                        return tt;
                    });

            RegistrationResponse response =
                    tournamentService.registerTeam(TOURNAMENT_ID, teamId,
                            ADMIN_ID, UserRole.TOURNAMENT_ADMIN);

            assertThat(response.getRegistrationStatus()).isEqualTo(RegistrationStatus.PENDING);
            assertThat(response.getTeamName()).isEqualTo("Team " + teamId);
        }
    }

    @Test
    void step4_registrationOpenToRegistrationClosed_succeeds() {
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.updateStatus(
                TOURNAMENT_ID, TournamentStatus.REGISTRATION_CLOSED);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_CLOSED);
    }

    @Test
    void step5_registrationClosedToInProgress_generatesBracket() {
        tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.updateStatus(
                TOURNAMENT_ID, TournamentStatus.IN_PROGRESS);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
        verify(bracketService).generateBracket(TOURNAMENT_ID);
    }

    @Test
    void step6_inProgressToCompleted_succeeds() {
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.updateStatus(
                TOURNAMENT_ID, TournamentStatus.COMPLETED);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.COMPLETED);
    }

    @Test
    void step7_completedToArchived_succeeds() {
        tournament.setStatus(TournamentStatus.COMPLETED);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        TournamentResponse response = tournamentService.updateStatus(
                TOURNAMENT_ID, TournamentStatus.ARCHIVED);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.ARCHIVED);
    }

    // ─── Status transition: invalid skips ────────────────────────────────────

    @Test
    void invalidTransition_draftToInProgress_throws() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() ->
                tournamentService.updateStatus(TOURNAMENT_ID, TournamentStatus.IN_PROGRESS))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("IN_PROGRESS");
    }

    @Test
    void invalidTransition_draftToCompleted_throws() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() ->
                tournamentService.updateStatus(TOURNAMENT_ID, TournamentStatus.COMPLETED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void invalidTransition_inProgressToRegistrationOpen_throws() {
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() ->
                tournamentService.updateStatus(TOURNAMENT_ID, TournamentStatus.REGISTRATION_OPEN))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void invalidTransition_completedToDraft_throws() {
        tournament.setStatus(TournamentStatus.COMPLETED);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() ->
                tournamentService.updateStatus(TOURNAMENT_ID, TournamentStatus.DRAFT))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    // ─── Team registration: business rules ───────────────────────────────────

    @Test
    void registerTeam_whenTournamentNotRegistrationOpen_throwsConflict() {
        // Tournament is DRAFT — registration not allowed
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() ->
                tournamentService.registerTeam(TOURNAMENT_ID, 1L,
                        ADMIN_ID, UserRole.TOURNAMENT_ADMIN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not open for registration");
    }

    @Test
    void registerTeam_duplicateRegistration_throwsConflict() {
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        Team team = makeTeam(1L, "Alpha");
        TournamentTeam existing = new TournamentTeam(tournament, team);

        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(tournamentTeamRepository.findByTournamentIdAndTeamId(TOURNAMENT_ID, 1L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                tournamentService.registerTeam(TOURNAMENT_ID, 1L,
                        ADMIN_ID, UserRole.TOURNAMENT_ADMIN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");

        verify(tournamentTeamRepository, never()).save(any());
    }

    @Test
    void registerTeam_teamManagerRegistersOwnTeam_succeeds() {
        long managerId = 50L;
        User manager = new User("mgr", "mgr@test.com", "hash", UserRole.TEAM_MANAGER);
        manager.setId(managerId);
        Team team = makeTeam(1L, "Alpha");
        team.setTeamManager(manager);

        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(tournamentTeamRepository.findByTournamentIdAndTeamId(TOURNAMENT_ID, 1L))
                .thenReturn(Optional.empty());
        when(tournamentTeamRepository.save(any())).thenAnswer(inv -> {
            TournamentTeam tt = inv.getArgument(0);
            tt.setId(1L);
            return tt;
        });

        RegistrationResponse response =
                tournamentService.registerTeam(TOURNAMENT_ID, 1L, managerId, UserRole.TEAM_MANAGER);

        assertThat(response.getTeamId()).isEqualTo(1L);
    }

    // ─── Bracket generation: only on IN_PROGRESS transition ──────────────────

    @Test
    void bracketNotGenerated_onOtherTransitions() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

        tournamentService.updateStatus(TOURNAMENT_ID, TournamentStatus.REGISTRATION_OPEN);

        verify(bracketService, never()).generateBracket(any());
    }

    @Test
    void standings_returnsEmptyList_whenNoneRecorded() {
        when(tournamentRepository.existsById(TOURNAMENT_ID)).thenReturn(true);
        when(tournamentRepository.findStandings(TOURNAMENT_ID)).thenReturn(List.of());

        var standings = tournamentService.getStandings(TOURNAMENT_ID);

        assertThat(standings).isEmpty();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TournamentRequest makeTournamentRequest() {
        TournamentRequest r = new TournamentRequest();
        r.setName("Spring Championship");
        r.setGameTitle("Test Game");
        r.setFormat(TournamentFormat.SINGLE_ELIMINATION);
        r.setMaxTeams(8);
        r.setGameGenreId(1L);
        return r;
    }

    private Team makeTeam(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setTag(name.substring(0, Math.min(3, name.length())).toUpperCase());
        return team;
    }
}