package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.EquipmentRequest;
import com.app.eventnexus.dtos.requests.LoadoutRequest;
import com.app.eventnexus.dtos.responses.EquipmentResponse;
import com.app.eventnexus.dtos.responses.LoadoutResponse;
import com.app.eventnexus.services.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller for equipment management and loadout assignments.
 *
 * <p>Equipment CRUD is scoped to the authenticated org context.
 * Loadout endpoints are nested under the tournament path:
 * {@code /api/orgs/{orgSlug}/tournaments/{tournamentId}/loadouts}.
 * All business logic is delegated to {@link EquipmentService}.
 */
@RestController
@RequestMapping("/api/orgs/{orgSlug}")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    // ─── Equipment CRUD ────────────────────────────────────────────────────────

    /**
     * Returns all equipment items in the organisation.
     * Requires authentication.
     *
     * @return 200 OK with a list of all equipment
     */
    @GetMapping("/equipment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.findAll());
    }

    /**
     * Returns a single piece of equipment by its ID.
     * Requires authentication.
     *
     * @param id the equipment's primary key
     * @return 200 OK with equipment details, or 404 if not found
     */
    @GetMapping("/equipment/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.findById(id));
    }

    /**
     * Returns all equipment associated with a specific venue.
     * Requires authentication.
     *
     * @param venueId the venue's primary key
     * @return 200 OK with a list of equipment for that venue
     */
    @GetMapping("/venues/{venueId}/equipment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(equipmentService.findByVenueId(venueId));
    }

    /**
     * Creates a new piece of equipment.
     * Requires {@code TOURNAMENT_ADMIN} role.
     *
     * @param request equipment details (venueId, name, category, serialNumber)
     * @return 201 Created with the new equipment, or 409 if serial number is duplicate
     */
    @PostMapping("/equipment")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<EquipmentResponse> createEquipment(@RequestBody EquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(request));
    }

    /**
     * Updates an existing piece of equipment's mutable fields.
     * Requires {@code TOURNAMENT_ADMIN} role.
     *
     * @param id      the equipment's primary key
     * @param request updated values
     * @return 200 OK with the updated equipment, or 404 if not found
     */
    @PutMapping("/equipment/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<EquipmentResponse> updateEquipment(@PathVariable Long id,
                                                             @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.update(id, request));
    }

    /**
     * Deletes a piece of equipment.
     * Returns 409 if the equipment has active loadout assignments.
     * Requires {@code TOURNAMENT_ADMIN} role.
     *
     * @param id the equipment's primary key
     * @return 204 No Content on success
     */
    @DeleteMapping("/equipment/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Loadout Endpoints ────────────────────────────────────────────────────

    /**
     * Returns all equipment loadout assignments for a given tournament.
     * Requires authentication.
     *
     * @param tournamentId the tournament's primary key
     * @return 200 OK with a list of loadout assignments
     */
    @GetMapping("/tournaments/{tournamentId}/loadouts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LoadoutResponse>> getLoadouts(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(equipmentService.getLoadoutsForTournament(tournamentId));
    }

    /**
     * Assigns a piece of equipment to a team for a specific tournament.
     * The equipment must be currently available.
     * Requires {@code TOURNAMENT_ADMIN} role.
     *
     * @param tournamentId the tournament's primary key
     * @param request      body containing the {@code equipmentId} and {@code teamId}
     * @return 201 Created with the new loadout, or 409 if equipment is unavailable
     */
    @PostMapping("/tournaments/{tournamentId}/loadouts")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<LoadoutResponse> assignLoadout(@PathVariable Long tournamentId,
                                                         @RequestBody LoadoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.assignLoadout(
                        tournamentId, request.getTeamId(), request.getEquipmentId()));
    }

    /**
     * Removes an equipment loadout assignment, restoring the equipment's availability.
     * Requires {@code TOURNAMENT_ADMIN} role.
     *
     * @param tournamentId the tournament's primary key (used for URL scoping)
     * @param loadoutId    the loadout's primary key
     * @return 204 No Content on success
     */
    @DeleteMapping("/tournaments/{tournamentId}/loadouts/{loadoutId}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> removeLoadout(@PathVariable Long tournamentId,
                                              @PathVariable Long loadoutId) {
        equipmentService.removeLoadout(loadoutId);
        return ResponseEntity.noContent().build();
    }
}
