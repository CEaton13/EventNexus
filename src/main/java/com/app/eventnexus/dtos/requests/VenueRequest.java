package com.app.eventnexus.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a venue.
 */
public class VenueRequest {

    @NotBlank(message = "Venue name is required")
    @Size(max = 200, message = "Venue name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @NotNull(message = "Station count is required")
    @Min(value = 1, message = "Station count must be at least 1")
    private Integer stationCount;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public VenueRequest() {
    }

    public VenueRequest(String name, String location, Integer stationCount) {
        this.name = name;
        this.location = location;
        this.stationCount = stationCount;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

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
}
