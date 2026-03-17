package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Venue} entities.
 */
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * Counts how many tournaments currently reference this venue.
     * Used in {@code VenueService.delete()} to prevent deletion of a venue
     * that has dependent tournaments.
     *
     * @param venueId the venue's primary key
     * @return number of tournaments linked to the venue
     */
    @Query(value = "SELECT COUNT(*) FROM tournaments WHERE venue_id = :venueId",
           nativeQuery = true)
    Long countLinkedTournaments(@Param("venueId") Long venueId);

    /**
     * Queries the {@code venue_station_utilization} view for a single venue.
     * Returns an {@link UtilizationData} projection with live station usage figures.
     *
     * @param venueId the venue's primary key
     * @return an Optional containing utilization data, or empty if the venue is not in the view
     */
    @Query(value = """
            SELECT total_stations    AS totalStations,
                   active_matches    AS activeMatches,
                   available_stations AS availableStations,
                   utilization_pct   AS utilizationPct
            FROM venue_station_utilization
            WHERE venue_id = :venueId
            """, nativeQuery = true)
    Optional<UtilizationData> findUtilizationByVenueId(@Param("venueId") Long venueId);

    /**
     * Projection interface for the {@code venue_station_utilization} view columns
     * returned by {@link #findUtilizationByVenueId(Long)}.
     */
    interface UtilizationData {
        Integer getTotalStations();
        Long getActiveMatches();
        Long getAvailableStations();
        BigDecimal getUtilizationPct();
    }
}
