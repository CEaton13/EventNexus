package com.app.eventnexus.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * JPA entity representing a piece of equipment assigned to a team for a specific tournament.
 * Maps to the {@code equipment_loadouts} table.
 *
 * <p>The unique constraint {@code (equipment_id, tournament_id)} ensures that
 * each piece of equipment can be assigned to at most one team per tournament.
 *
 * <p>The {@code sync_equipment_availability} PostgreSQL trigger automatically updates
 * {@code equipment.is_available} when rows are inserted into or deleted from this table.
 * Setting {@code returnedAt} on an existing row also triggers the restore.
 */
@Entity
@Table(name = "equipment_loadouts",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_equipment_tournament",
               columnNames = {"equipment_id", "tournament_id"}))
public class EquipmentLoadout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public EquipmentLoadout() {
    }

    public EquipmentLoadout(Equipment equipment, Team team, Tournament tournament) {
        this.equipment = equipment;
        this.team = team;
        this.tournament = tournament;
        this.assignedAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
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
