package com.app.eventnexus.dtos.requests;

/**
 * Request body for creating or updating a venue.
 */
public class VenueRequest {

    private String name;
    private String location;
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
