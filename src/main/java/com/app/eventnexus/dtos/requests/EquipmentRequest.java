package com.app.eventnexus.dtos.requests;

/**
 * Request body for creating or updating a piece of equipment.
 */
public class EquipmentRequest {

    private Long venueId;
    private String name;
    private String category;
    private String serialNumber;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public EquipmentRequest() {
    }

    public EquipmentRequest(Long venueId, String name, String category, String serialNumber) {
        this.venueId = venueId;
        this.name = name;
        this.category = category;
        this.serialNumber = serialNumber;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
