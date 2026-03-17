package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.Venue;

import java.time.LocalDateTime;

/**
 * Response DTO for a venue.
 * Utilization fields ({@code activeMatches}, {@code availableStations},
 * {@code utilizationPct}) are populated only for the single-venue
 * {@code GET /api/venues/{id}} endpoint; they are {@code null} in list responses.
 */
public class VenueResponse {

    private Long id;
    private String name;
    private String location;
    private Integer stationCount;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Utilization fields — null unless fetched via findById()
    private Long activeMatches;
    private Long availableStations;
    private Double utilizationPct;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a basic {@code VenueResponse} from a {@link Venue} entity.
     * Utilization fields are left null; populate them separately when needed.
     *
     * @param venue the source entity
     * @return a populated response DTO
     */
    public static VenueResponse from(Venue venue) {
        VenueResponse dto = new VenueResponse();
        dto.id = venue.getId();
        dto.name = venue.getName();
        dto.location = venue.getLocation();
        dto.stationCount = venue.getStationCount();
        dto.isActive = venue.isActive();
        dto.createdAt = venue.getCreatedAt();
        dto.updatedAt = venue.getUpdatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public VenueResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getStationCount() {
        return stationCount;
    }

    public void setStationCount(Integer stationCount) {
        this.stationCount = stationCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getActiveMatches() {
        return activeMatches;
    }

    public void setActiveMatches(Long activeMatches) {
        this.activeMatches = activeMatches;
    }

    public Long getAvailableStations() {
        return availableStations;
    }

    public void setAvailableStations(Long availableStations) {
        this.availableStations = availableStations;
    }

    public Double getUtilizationPct() {
        return utilizationPct;
    }

    public void setUtilizationPct(Double utilizationPct) {
        this.utilizationPct = utilizationPct;
    }
}
