package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.Equipment;

import java.time.LocalDateTime;

/**
 * Response DTO for a piece of equipment.
 */
public class EquipmentResponse {

    private Long id;
    private Long venueId;
    private String venueName;
    private String name;
    private String category;
    private String serialNumber;
    private boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds an {@code EquipmentResponse} from an {@link Equipment} entity.
     *
     * @param equipment the source entity (venue must not be lazy-unloaded)
     * @return a populated response DTO
     */
    public static EquipmentResponse from(Equipment equipment) {
        EquipmentResponse dto = new EquipmentResponse();
        dto.id = equipment.getId();
        dto.venueId = equipment.getVenue().getId();
        dto.venueName = equipment.getVenue().getName();
        dto.name = equipment.getName();
        dto.category = equipment.getCategory();
        dto.serialNumber = equipment.getSerialNumber();
        dto.isAvailable = equipment.isAvailable();
        dto.createdAt = equipment.getCreatedAt();
        dto.updatedAt = equipment.getUpdatedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public EquipmentResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
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

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
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
}
