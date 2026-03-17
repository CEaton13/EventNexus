package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.VenueRequest;
import com.app.eventnexus.dtos.responses.VenueResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing venue data.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>A venue cannot be deleted while it has one or more linked tournaments.</li>
 *   <li>The {@code venue_station_utilization} view is queried for single-venue
 *       lookups to include live station usage figures.</li>
 * </ul>
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all venues in database insertion order.
     * Utilization fields are not included in the list response.
     *
     * @return list of all venues as response DTOs; never null
     */
    @Transactional(readOnly = true)
    public List<VenueResponse> findAll() {
        return venueRepository.findAll()
                .stream()
                .map(VenueResponse::from)
                .toList();
    }

    /**
     * Returns a single venue by its ID, including live utilization statistics
     * from the {@code venue_station_utilization} view.
     *
     * @param id the venue's primary key
     * @return the venue with utilization data populated
     * @throws ResourceNotFoundException if no venue exists with the given ID
     */
    @Transactional(readOnly = true)
    public VenueResponse findById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));

        VenueResponse dto = VenueResponse.from(venue);
        venueRepository.findUtilizationByVenueId(id).ifPresent(u -> {
            dto.setActiveMatches(u.getActiveMatches());
            dto.setAvailableStations(u.getAvailableStations());
            dto.setUtilizationPct(
                    u.getUtilizationPct() != null ? u.getUtilizationPct().doubleValue() : 0.0);
        });
        return dto;
    }

    // ─── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates a new venue.
     *
     * @param request venue details (name, location, stationCount)
     * @return the newly created venue as a response DTO
     */
    @Transactional
    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue(request.getName(), request.getLocation(), request.getStationCount());
        return VenueResponse.from(venueRepository.save(venue));
    }

    /**
     * Updates an existing venue's mutable fields.
     *
     * @param id      the venue's primary key
     * @param request updated values
     * @return the updated venue as a response DTO
     * @throws ResourceNotFoundException if no venue exists with the given ID
     */
    @Transactional
    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));

        venue.setName(request.getName());
        venue.setLocation(request.getLocation());
        venue.setStationCount(request.getStationCount());
        venue.setUpdatedAt(LocalDateTime.now());

        return VenueResponse.from(venueRepository.save(venue));
    }

    /**
     * Deletes a venue by its ID.
     * Deletion is blocked if any tournament currently references this venue,
     * since removing it would break the tournament's schedule data.
     *
     * @param id the venue's primary key
     * @throws ResourceNotFoundException if no venue exists with the given ID
     * @throws ConflictException         if the venue is referenced by one or more tournaments
     */
    @Transactional
    public void delete(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venue", id);
        }
        if (venueRepository.countLinkedTournaments(id) > 0) {
            throw new ConflictException(
                    "Venue " + id + " cannot be deleted while it is referenced by existing tournaments.");
        }
        venueRepository.deleteById(id);
    }

    /**
     * Returns live station utilization figures for a single venue by querying
     * the {@code venue_station_utilization} database view.
     * This method is exposed separately for cases where only utilization data
     * is needed without the full venue payload.
     *
     * @param id the venue's primary key
     * @return the venue response with utilization fields populated
     * @throws ResourceNotFoundException if no venue exists with the given ID
     */
    @Transactional(readOnly = true)
    public VenueResponse getUtilization(Long id) {
        return findById(id);
    }
}
