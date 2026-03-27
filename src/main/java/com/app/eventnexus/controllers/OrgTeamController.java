package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.PageResponse;
import com.app.eventnexus.dtos.responses.TeamResponse;
import com.app.eventnexus.services.TeamService;
import com.app.eventnexus.tenant.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Org-scoped read endpoint for teams.
 *
 * <p>Returns only teams that are registered in at least one tournament belonging
 * to the current organization (resolved from the URL slug by {@code TenantFilter}).
 * This keeps each admin's team view isolated from other organizations.
 *
 * <p>Write operations (create, update, delete) remain on {@link TeamController}
 * under {@code /api/teams}.
 */
@RestController
@RequestMapping("/api/orgs/{orgSlug}/teams")
public class OrgTeamController {

    private final TeamService teamService;

    public OrgTeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Returns a page of teams registered in this organization's tournaments.
     * Public — no authentication required.
     *
     * @param pageable pagination parameters (default: 20 per page, sorted by name)
     * @return 200 OK with a page of team DTOs scoped to this org
     */
    @GetMapping
    public ResponseEntity<PageResponse<TeamResponse>> getOrgTeams(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Long orgId = TenantContext.getTenantId();
        return ResponseEntity.ok(PageResponse.from(teamService.findByOrganization(orgId, pageable)));
    }
}
