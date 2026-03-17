package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.requests.VenueRequest;
import com.app.eventnexus.dtos.responses.VenueResponse;
import com.app.eventnexus.services.VenueService;
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
 * Thin REST controller for venue management.
 * Read endpoints are public; write endpoints require {@code TOURNAMENT_ADMIN}.
 * All business logic is delegated to {@link VenueService}.
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * Returns all venues.
     * Utilization statistics are not included in the list response.
     *
     * @return 200 OK with a list of all venues
     */
    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        return ResponseEntity.ok(venueService.findAll());
    }

    /**
     * Returns a single venue including live station utilization statistics.
     *
     * @param id the venue's primary key
     * @return 200 OK with venue details and utilization data, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    /**
     * Creates a new venue.
     *
     * @param request venue details (name, location, stationCount)
     * @return 201 Created with the new venue
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<VenueResponse> createVenue(@RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    /**
     * Updates an existing venue's name, location, or station count.
     *
     * @param id      the venue's primary key
     * @param request updated values
     * @return 200 OK with the updated venue, or 404 if not found
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id,
                                                     @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.update(id, request));
    }

    /**
     * Deletes a venue.
     * Returns 409 Conflict if the venue is referenced by any existing tournament.
     *
     * @param id the venue's primary key
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
