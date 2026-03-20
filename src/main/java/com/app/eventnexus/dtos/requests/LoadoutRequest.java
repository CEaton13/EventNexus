package com.app.eventnexus.dtos.requests;

/**
 * Request body for assigning a piece of equipment to a team for a tournament.
 */
public class LoadoutRequest {

    private Long equipmentId;
    private Long teamId;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public LoadoutRequest() {
    }

    public LoadoutRequest(Long equipmentId, Long teamId) {
        this.equipmentId = equipmentId;
        this.teamId = teamId;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
