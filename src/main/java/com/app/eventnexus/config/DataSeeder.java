package com.app.eventnexus.config;

import com.app.eventnexus.enums.MatchStatus;
import com.app.eventnexus.enums.OrgRole;
import com.app.eventnexus.enums.RegistrationStatus;
import com.app.eventnexus.enums.TournamentFormat;
import com.app.eventnexus.enums.TournamentStatus;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.models.GameGenre;
import com.app.eventnexus.models.Match;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.models.OrganizationMember;
import com.app.eventnexus.models.Player;
import com.app.eventnexus.models.PlayerStats;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.TournamentTeam;
import com.app.eventnexus.models.User;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.GameGenreRepository;
import com.app.eventnexus.repositories.MatchRepository;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.repositories.PlayerRepository;
import com.app.eventnexus.repositories.PlayerStatsRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.TournamentTeamRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.repositories.VenueRepository;
import com.app.eventnexus.services.BracketService;
import com.app.eventnexus.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with realistic demo data on first startup.
 *
 * <p>Idempotent — skips all seeding if the sentinel user {@code admin1} already exists.
 * Safe to run on every startup without side effects.
 *
 * <h3>Seed Dataset</h3>
 * <ul>
 *   <li>2 organizations: NexusOps Gaming, Circuit Gaming</li>
 *   <li>8 users: 2 admins, 4 team managers, 2 spectators (all password: {@code password123})</li>
 *   <li>2 venues (1 per org)</li>
 *   <li>8 teams with 5 players each</li>
 *   <li>6 tournaments across both orgs in varying lifecycle states</li>
 *   <li>Fully completed bracket with player stats for COMPLETED tournaments</li>
 * </ul>
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SEED_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository orgMemberRepository;
    private final GameGenreRepository gameGenreRepository;
    private final VenueRepository venueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final MatchRepository matchRepository;
    private final BracketService bracketService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      OrganizationRepository organizationRepository,
                      OrganizationMemberRepository orgMemberRepository,
                      GameGenreRepository gameGenreRepository,
                      VenueRepository venueRepository,
                      TeamRepository teamRepository,
                      PlayerRepository playerRepository,
                      PlayerStatsRepository playerStatsRepository,
                      TournamentRepository tournamentRepository,
                      TournamentTeamRepository tournamentTeamRepository,
                      MatchRepository matchRepository,
                      BracketService bracketService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.orgMemberRepository = orgMemberRepository;
        this.gameGenreRepository = gameGenreRepository;
        this.venueRepository = venueRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.playerStatsRepository = playerStatsRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentTeamRepository = tournamentTeamRepository;
        this.matchRepository = matchRepository;
        this.bracketService = bracketService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Use tournament_teams count as the sentinel — it is not RLS-protected and is
        // only populated near the end of a successful seed run. Checking a user name
        // was unreliable because users are created early; a partial failure (e.g. at
        // venues) left users in the DB, causing subsequent runs to skip incorrectly.
        if (tournamentTeamRepository.count() > 0) {
            log.info("DataSeeder: demo data already present — skipping.");
            return;
        }
        log.info("DataSeeder: seeding demo data...");
        try {
            seed();
            log.info("DataSeeder: seeding complete.");
        } catch (Exception e) {
            log.error("DataSeeder: seeding failed — {}", e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    protected void seed() {
        String hash = passwordEncoder.encode(SEED_PASSWORD);

        // ── Users ───────────────────────────────────────────────────────────────
        User admin1  = userRepository.findByUsername("admin1").orElseGet(() -> save(new User("admin1",      "admin1@nexusops.com",     hash, UserRole.TOURNAMENT_ADMIN)));
        User admin2  = userRepository.findByUsername("admin2").orElseGet(() -> save(new User("admin2",      "admin2@circuit.com",      hash, UserRole.TOURNAMENT_ADMIN)));
        User mgr1    = userRepository.findByUsername("mgr1").orElseGet(() -> save(new User("mgr1",        "mgr1@example.com",        hash, UserRole.TEAM_MANAGER)));
        User mgr2    = userRepository.findByUsername("mgr2").orElseGet(() -> save(new User("mgr2",        "mgr2@example.com",        hash, UserRole.TEAM_MANAGER)));
        User mgr3    = userRepository.findByUsername("mgr3").orElseGet(() -> save(new User("mgr3",        "mgr3@example.com",        hash, UserRole.TEAM_MANAGER)));
        User mgr4    = userRepository.findByUsername("mgr4").orElseGet(() -> save(new User("mgr4",        "mgr4@example.com",        hash, UserRole.TEAM_MANAGER)));
        userRepository.findByUsername("spectator1").orElseGet(() -> save(new User("spectator1",  "spectator1@example.com",  hash, UserRole.SPECTATOR)));
        userRepository.findByUsername("spectator2").orElseGet(() -> save(new User("spectator2",  "spectator2@example.com",  hash, UserRole.SPECTATOR)));

        // ── Organizations ───────────────────────────────────────────────────────
        Organization nexusops = organizationRepository.findBySlug("nexusops")
            .orElseGet(() -> organizationRepository.save(
                new Organization("NexusOps Gaming", "nexusops", "contact@nexusops.com")));
        Organization circuit = organizationRepository.findBySlug("circuit-gaming")
            .orElseGet(() -> organizationRepository.save(
                new Organization("Circuit Gaming", "circuit-gaming", "contact@circuit.com")));

        // ── Org memberships ─────────────────────────────────────────────────────
        ensureMember(nexusops, admin1, OrgRole.ORG_ADMIN);
        ensureMember(nexusops, mgr1,   OrgRole.ORG_MEMBER);
        ensureMember(nexusops, mgr2,   OrgRole.ORG_MEMBER);
        ensureMember(circuit,  admin2, OrgRole.ORG_ADMIN);
        ensureMember(circuit,  mgr3,   OrgRole.ORG_MEMBER);
        ensureMember(circuit,  mgr4,   OrgRole.ORG_MEMBER);

        // ── Teams & Players ─────────────────────────────────────────────────────
        Team storm   = buildTeam("Storm Surge",       "STORM", "Northeast US", mgr1);
        Team phantom = buildTeam("Phantom Strike",    "PHNTM", "Southeast US", mgr1);
        Team iron    = buildTeam("Iron Fist",         "IRON",  "Midwest US",   mgr2);
        Team voids   = buildTeam("Void Walkers",      "VOID",  "West Coast",   mgr2);
        Team neon    = buildTeam("Neon Rush",         "NEON",  "South US",     mgr3);
        Team cyber   = buildTeam("Cyber Wolves",      "CYBR",  "Pacific NW",   mgr3);
        Team apex    = buildTeam("Apex Squad",        "APEX",  "Mid-Atlantic",  mgr4);
        Team shadow  = buildTeam("Shadow Protocol",   "SHDW",  "Great Lakes",  mgr4);

        buildPlayers(storm,   new String[]{"StormKing","ThunderClap","LightningRod","CloudBurst","WindShear"},
                      new String[]{"Alex M.","Jordan T.","Casey R.","Sam P.","Drew L."});
        buildPlayers(phantom, new String[]{"Phantom","Specter","Wraith","Ghost","Shade"},
                      new String[]{"Riley K.","Morgan S.","Taylor B.","Quinn A.","Avery N."});
        buildPlayers(iron,    new String[]{"IronMike","SteelFist","CobaltKnight","TitanArm","AlloyClaw"},
                      new String[]{"Chris H.","Pat W.","Lee D.","Dana F.","Jesse Q."});
        buildPlayers(voids,   new String[]{"VoidWalker","NullSpace","DarkMatter","AbyssLord","EtherDrift"},
                      new String[]{"Blake C.","Skyler M.","Reese G.","Cameron O.","Hayden Z."});
        buildPlayers(neon,    new String[]{"NeonFlash","LaserEdge","UVKing","GlowRider","PrismSplit"},
                      new String[]{"Jamie V.","Ronnie X.","Frankie T.","Billie E.","Charlie U."});
        buildPlayers(cyber,   new String[]{"CyberAlpha","ByteWolf","DataFang","NetPack","PingKiller"},
                      new String[]{"Ash R.","Robin J.","Scout P.","Finley W.","River B."});
        buildPlayers(apex,    new String[]{"ApexPred","TopTier","HighGround","PeakForm","AltitudePro"},
                      new String[]{"Shane Y.","Dani I.","Remi O.","Corey U.","Toni E."});
        buildPlayers(shadow,  new String[]{"ShadowOps","Blackout","NightCrawl","EclipseX","UmbraStrike"},
                      new String[]{"Terry N.","Kai M.","Yuri S.","Zara C.","Nico A."});

        // ── Game Genres: ensure defaults exist ────────────────────────────────
        List<GameGenre> genres = gameGenreRepository.findAll();
        GameGenre fps      = genreByName(genres, "FPS");
        GameGenre moba     = genreByName(genres, "MOBA");
        GameGenre fighting = genreByName(genres, "Fighting");
        GameGenre battle   = genreByName(genres, "Battle");
        GameGenre racing   = genreByName(genres, "Racing");
        GameGenre retro    = genreByName(genres, "Retro");

        // ── Venues ───────────────────────────────────────────────────────────────
        TenantContext.setTenantId(nexusops.getId());
        Venue nexusArena  = venueRepository.findByName("Nexus Arena")
                .orElseGet(() -> venueRepository.save(new Venue("Nexus Arena",  "123 LAN Blvd, Chicago IL",  32, nexusops)));
        Venue nexusLounge = venueRepository.findByName("Nexus Lounge")
                .orElseGet(() -> venueRepository.save(new Venue("Nexus Lounge", "123 LAN Blvd, Chicago IL",   8, nexusops)));

        TenantContext.setTenantId(circuit.getId());
        Venue circuitHall = venueRepository.findByName("Circuit Hall")
                .orElseGet(() -> venueRepository.save(new Venue("Circuit Hall", "456 Esports Ave, Austin TX", 16, circuit)));

        // ── NexusOps Tournaments ────────────────────────────────────────────────
        TenantContext.setTenantId(nexusops.getId());

        // 1. Nexus Open 2024 — COMPLETED (FPS)
        Tournament nexusOpen = createTournament(
            "Nexus Open 2024",
            "The flagship annual 8-team FPS tournament hosted by NexusOps Gaming. "
                + "Eight of the region's best squads compete for the championship title.",
            "Valorant", TournamentFormat.SINGLE_ELIMINATION, 8,
            LocalDateTime.of(2024, 11, 1,  9, 0),
            LocalDateTime.of(2024, 11, 15, 23, 59),
            LocalDateTime.of(2024, 11, 20, 10, 0),
            LocalDateTime.of(2024, 11, 20, 22, 0),
            nexusArena, fps, admin1, nexusops, TournamentStatus.COMPLETED);

        registerApproved(nexusOpen, storm,   1);
        registerApproved(nexusOpen, phantom, 2);
        registerApproved(nexusOpen, iron,    3);
        registerApproved(nexusOpen, voids,   4);
        registerApproved(nexusOpen, neon,    5);
        registerApproved(nexusOpen, cyber,   6);
        registerApproved(nexusOpen, apex,    7);
        registerApproved(nexusOpen, shadow,  8);

        bracketService.generateBracket(nexusOpen.getId());
        simulateCompletedBracket(nexusOpen, nexusArena,
            LocalDateTime.of(2024, 11, 20, 10, 0));

        // 2. Winter Clash 2025 — IN_PROGRESS (MOBA)
        Tournament winterClash = createTournament(
            "Winter Clash 2025",
            "A fast-paced 4-team MOBA showdown to kick off the new year. "
                + "Semifinals are complete — the final is set!",
            "League of Legends", TournamentFormat.SINGLE_ELIMINATION, 4,
            LocalDateTime.of(2025, 1, 5,  9, 0),
            LocalDateTime.of(2025, 1, 15, 23, 59),
            LocalDateTime.of(2025, 1, 20, 10, 0),
            LocalDateTime.of(2025, 1, 20, 22, 0),
            nexusArena, moba, admin1, nexusops, TournamentStatus.IN_PROGRESS);

        registerApproved(winterClash, storm,   1);
        registerApproved(winterClash, phantom, 2);
        registerApproved(winterClash, iron,    3);
        registerApproved(winterClash, voids,   4);

        bracketService.generateBracket(winterClash.getId());
        simulateInProgressBracket(winterClash, nexusArena,
            LocalDateTime.of(2025, 1, 20, 10, 0));

        // 3. Spring Showdown 2025 — REGISTRATION_OPEN (Fighting)
        Tournament springShowdown = createTournament(
                "Spring Showdown 2025",
                "Open registration for the premier fighting game tournament of the spring season. "
                        + "Register your team and compete for glory!",
                "Street Fighter 6", TournamentFormat.SINGLE_ELIMINATION, 8,
                LocalDateTime.of(2025, 3, 1,  9, 0),
                LocalDateTime.of(2025, 4, 15, 23, 59),
                LocalDateTime.of(2025, 4, 20, 10, 0),
                LocalDateTime.of(2025, 4, 20, 20, 0),
                nexusLounge, fighting, admin1, nexusops, TournamentStatus.REGISTRATION_OPEN);

        registerPending(springShowdown, neon);
        registerPending(springShowdown, cyber);
        registerApproved(springShowdown, apex, null);

        // 4. Summer Championship 2025 — DRAFT (Battle Royale, early planning stage)
        createTournament(
                "Summer Championship 2025",
                "Coming soon: NexusOps' biggest Battle Royale tournament yet. "
                        + "Details will be announced shortly — stay tuned.",
                "Apex Legends", TournamentFormat.SINGLE_ELIMINATION, 8,
                LocalDateTime.of(2025, 6, 1, 9, 0),
                LocalDateTime.of(2025, 7, 1, 23, 59),
                LocalDateTime.of(2025, 7, 5, 10, 0),
                LocalDateTime.of(2025, 7, 5, 22, 0),
                nexusArena, battle, admin1, nexusops, TournamentStatus.DRAFT);

        // ── Circuit Gaming Tournaments ───────────────────────────────────────────
        TenantContext.setTenantId(circuit.getId());

        // 5. Circuit Cup Winter — COMPLETED (4-team Racing, full results)
        Tournament circuitCup = createTournament(
                "Circuit Cup Winter",
                "A completed 4-team racing game championship hosted by Circuit Gaming. "
                        + "All races have been run — check the bracket for final results.",
                "Mario Kart 8 Deluxe", TournamentFormat.SINGLE_ELIMINATION, 4,
                LocalDateTime.of(2024, 12, 1,  9, 0),
                LocalDateTime.of(2024, 12, 10, 23, 59),
                LocalDateTime.of(2024, 12, 15, 10, 0),
                LocalDateTime.of(2024, 12, 15, 18, 0),
                circuitHall, racing, admin2, circuit, TournamentStatus.COMPLETED);

        registerApproved(circuitCup, neon,   1);
        registerApproved(circuitCup, cyber,  2);
        registerApproved(circuitCup, apex,   3);
        registerApproved(circuitCup, shadow, 4);

        bracketService.generateBracket(circuitCup.getId());
        simulateCompletedBracket(circuitCup, circuitHall,
                LocalDateTime.of(2024, 12, 15, 10, 0));

        // 6. Weekend Warriors — REGISTRATION_OPEN (Retro/Sports, open registration)
        Tournament weekendWarriors = createTournament(
                "Weekend Warriors",
                "Casual retro sports tournament — all skill levels welcome! "
                        + "Register your team and enjoy some classic competition.",
                "Rocket League", TournamentFormat.SINGLE_ELIMINATION, 8,
                LocalDateTime.of(2025, 2, 1,  9, 0),
                LocalDateTime.of(2025, 3, 1, 23, 59),
                LocalDateTime.of(2025, 3, 8, 10, 0),
                LocalDateTime.of(2025, 3, 8, 20, 0),
                circuitHall, retro, admin2, circuit, TournamentStatus.REGISTRATION_OPEN);

        registerPending(weekendWarriors, storm);
        registerPending(weekendWarriors, iron);
        registerApproved(weekendWarriors, shadow, null);
        registerApproved(weekendWarriors, neon,   null);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private User save(User user) {
        return userRepository.save(user);
    }

    private Team buildTeam(String name, String tag, String region, User manager) {
        return teamRepository.findByName(name)
                .orElseGet(() -> teamRepository.save(new Team(name, tag, null, region, manager)));
    }

    private void ensureMember(Organization org, User user, OrgRole role) {
        if (!orgMemberRepository.existsByIdOrganizationIdAndIdUserId(org.getId(), user.getId())) {
            orgMemberRepository.save(new OrganizationMember(org, user, role));
        }
    }

    private void buildPlayers(Team team, String[] tags, String[] realNames) {
        String[] positions = {"Entry Fragger", "Support", "IGL", "Lurker", "AWPer"};
        for (int i = 0; i < tags.length; i++) {
            playerRepository.save(new Player(team, tags[i], realNames[i], positions[i % positions.length],
                    "US", null));
        }
    }

    /**
     * Finds a game genre whose name contains the given keyword (case-insensitive).
     * Falls back to the first genre if none matches.
     */
    private GameGenre genreByName(List<GameGenre> genres, String keyword) {
        return genres.stream()
                .filter(g -> g.getName().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst()
                .orElse(genres.isEmpty() ? null : genres.get(0));
    }

    /**
     * Creates and persists a tournament with a pre-set status, bypassing the service
     * layer's state-transition guard so the seeder can create realistic historic data.
     * Idempotent — returns the existing tournament if one with the same name already exists.
     */
    private Tournament createTournament(String name, String description, String gameTitle,
                                        TournamentFormat format, int maxTeams,
                                        LocalDateTime regStart, LocalDateTime regEnd,
                                        LocalDateTime startDate, LocalDateTime endDate,
                                        Venue venue, GameGenre genre, User createdBy,
                                        Organization org, TournamentStatus status) {
        return tournamentRepository.findByName(name).orElseGet(() -> {
            Tournament t = new Tournament(name, description, gameTitle, format, maxTeams,
                    regStart, regEnd, startDate, endDate, venue, genre, createdBy, org);
            t.setStatus(status);
            return tournamentRepository.save(t);
        });
    }

    private void registerApproved(Tournament t, Team team, Integer seed) {
        if (tournamentTeamRepository.findByTournamentIdAndTeamId(t.getId(), team.getId()).isPresent()) {
            return;
        }
        TournamentTeam tt = new TournamentTeam(t, team);
        tt.setRegistrationStatus(RegistrationStatus.APPROVED);
        tt.setSeed(seed);
        tournamentTeamRepository.save(tt);
    }

    private void registerPending(Tournament t, Team team) {
        if (tournamentTeamRepository.findByTournamentIdAndTeamId(t.getId(), team.getId()).isPresent()) {
            return;
        }
        tournamentTeamRepository.save(new TournamentTeam(t, team));
    }

    /**
     * Simulates a fully completed single-elimination bracket.
     *
     * <p>Matches are loaded in round/match order after bracket generation.
     * Each round-1 match is given a result, propagating winners into subsequent
     * rounds. The top seed's side of the bracket wins every match.
     */
    private void simulateCompletedBracket(Tournament tournament, Venue venue, LocalDateTime startTime) {
        List<Match> allMatches = matchRepository.findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(
                tournament.getId());

        // Determine how many rounds exist
        int maxRound = allMatches.stream().mapToInt(Match::getRoundNumber).max().orElse(1);

        LocalDateTime matchTime = startTime;
        for (int round = 1; round <= maxRound; round++) {
            int r = round;
            List<Match> roundMatches = allMatches.stream()
                    .filter(m -> m.getRoundNumber() == r)
                    .toList();

            for (Match match : roundMatches) {
                if (match.getStatus() == MatchStatus.BYE) {
                    matchTime = matchTime.plusHours(1);
                    continue;
                }
                if (match.getTeamA() != null && match.getTeamB() != null) {
                    // Top seed (teamA) wins each match for simplicity
                    Team winner = match.getTeamA();
                    recordMatchResult(match, winner, venue, matchTime);
                    matchTime = matchTime.plusHours(2);
                }
            }
            // Re-fetch to get propagated winners for subsequent rounds
            allMatches = matchRepository.findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(
                    tournament.getId());
        }
    }

    /**
     * Simulates a bracket that is still in progress: completes all rounds except
     * the final, leaving the championship match scheduled but unplayed.
     */
    private void simulateInProgressBracket(Tournament tournament, Venue venue, LocalDateTime startTime) {
        List<Match> allMatches = matchRepository.findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(
                tournament.getId());

        int maxRound = allMatches.stream().mapToInt(Match::getRoundNumber).max().orElse(1);

        LocalDateTime matchTime = startTime;
        for (int round = 1; round < maxRound; round++) {
            int r = round;
            List<Match> roundMatches = allMatches.stream()
                    .filter(m -> m.getRoundNumber() == r)
                    .toList();

            for (Match match : roundMatches) {
                if (match.getStatus() == MatchStatus.BYE) {
                    matchTime = matchTime.plusHours(1);
                    continue;
                }
                if (match.getTeamA() != null && match.getTeamB() != null) {
                    Team winner = match.getTeamA();
                    recordMatchResult(match, winner, venue, matchTime);
                    matchTime = matchTime.plusHours(2);
                }
            }
            allMatches = matchRepository.findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(
                    tournament.getId());
        }

        // Schedule the final match without recording a result
        List<Match> finals = allMatches.stream()
                .filter(m -> m.getRoundNumber() == maxRound)
                .toList();
        for (Match finalMatch : finals) {
            if (finalMatch.getTeamA() != null && finalMatch.getTeamB() != null) {
                finalMatch.setScheduledTime(matchTime.plusHours(1));
                finalMatch.setVenue(venue);
                finalMatch.setStatus(MatchStatus.SCHEDULED);
                finalMatch.setUpdatedAt(LocalDateTime.now());
                matchRepository.save(finalMatch);
            }
        }
    }

    /**
     * Records a match result directly via the repository, bypassing the service-layer
     * status guard and conflict checks. Propagates the winner into the next bracket
     * slot and creates player stats records.
     */
    private void recordMatchResult(Match match, Team winner, Venue venue, LocalDateTime scheduledTime) {
        Team loser = winner.getId().equals(
                match.getTeamA() != null ? match.getTeamA().getId() : null)
                ? match.getTeamB()
                : match.getTeamA();

        match.setWinner(winner);
        match.setStatus(MatchStatus.COMPLETED);
        match.setScheduledTime(scheduledTime);
        match.setVenue(venue);
        match.setUpdatedAt(LocalDateTime.now());
        matchRepository.save(match);

        // Propagate winner to next bracket match
        Match nextMatch = match.getNextMatch();
        if (nextMatch != null) {
            if (match.getMatchNumber() % 2 != 0) {
                nextMatch.setTeamA(winner);
            } else {
                nextMatch.setTeamB(winner);
            }
            nextMatch.setUpdatedAt(LocalDateTime.now());
            matchRepository.save(nextMatch);
        }

        // Record player stats
        Long tournamentId = match.getTournament().getId();
        upsertStats(winner.getId(), tournamentId, true);
        if (loser != null) {
            upsertStats(loser.getId(), tournamentId, false);
        }
    }

    /** Creates or increments player stats for every active player on a team. */
    private void upsertStats(Long teamId, Long tournamentId, boolean won) {
        playerRepository.findByTeam_Id(teamId).stream()
                .filter(Player::isActive)
                .forEach(player -> {
                    PlayerStats stats = playerStatsRepository
                            .findByPlayer_IdAndTournamentId(player.getId(), tournamentId)
                            .orElseGet(() -> new PlayerStats(player, tournamentId));
                    if (won) {
                        stats.setWins(stats.getWins() + 1);
                    } else {
                        stats.setLosses(stats.getLosses() + 1);
                    }
                    stats.setUpdatedAt(LocalDateTime.now());
                    playerStatsRepository.save(stats);
                });
    }
}
