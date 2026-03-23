package com.app.eventnexus.dtos.responses;

import com.app.eventnexus.models.EquipmentLoadout;

import java.time.LocalDateTime;

/**
 * Response DTO for an equipment loadout assignment.
 */
public class LoadoutResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentCategory;
    private Long teamId;
    private String teamName;
    private Long tournamentId;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;

    // ─── Factory ───────────────────────────────────────────────────────────────

    /**
     * Builds a {@code LoadoutResponse} from an {@link EquipmentLoadout} entity.
     *
     * @param loadout the source entity (associations must not be lazy-unloaded)
     * @return a populated response DTO
     */
    public static LoadoutResponse from(EquipmentLoadout loadout) {
        LoadoutResponse dto = new LoadoutResponse();
        dto.id = loadout.getId();
        dto.equipmentId = loadout.getEquipment().getId();
        dto.equipmentName = loadout.getEquipment().getName();
        dto.equipmentCategory = loadout.getEquipment().getCategory();
        dto.teamId = loadout.getTeam().getId();
        dto.teamName = loadout.getTeam().getName();
        dto.tournamentId = loadout.getTournament().getId();
        dto.assignedAt = loadout.getAssignedAt();
        dto.returnedAt = loadout.getReturnedAt();
        return dto;
    }

    // ─── Constructors ──────────────────────────────────────────────────────────

    public LoadoutResponse() {
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentCategory() {
        return equipmentCategory;
    }

    public void setEquipmentCategory(String equipmentCategory) {
        this.equipmentCategory = equipmentCategory;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}
