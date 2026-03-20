package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.DashboardResponse;
import com.app.eventnexus.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST controller for the admin dashboard endpoint.
 *
 * <p>Exposes a single summary endpoint that aggregates counts across
 * tournaments, teams, players, and upcoming matches. Access is restricted to
 * {@code TOURNAMENT_ADMIN} users only. All business logic is delegated to
 * {@link DashboardService}.
 */
@RestController
@RequestMapping("/api/orgs/{orgSlug}/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Returns aggregated admin dashboard metrics.
     *
     * <p>Metrics include:
     * <ul>
     *   <li>Tournament counts grouped by lifecycle status</li>
     *   <li>Total teams</li>
     *   <li>Active (non-soft-deleted) players</li>
     *   <li>Scheduled matches in the next 7 days</li>
     * </ul>
     *
     * @return 200 OK with a {@link DashboardResponse}
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
