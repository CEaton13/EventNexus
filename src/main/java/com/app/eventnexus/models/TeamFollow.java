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
 * JPA entity representing a user following a team.
 * Maps to the {@code team_follows} table.
 * A user may follow the same team only once (unique constraint on user_id + team_id).
 */
@Entity
@Table(
    name = "team_follows",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_team_follow",
        columnNames = {"user_id", "team_id"}
    )
)
public class TeamFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TeamFollow() {}

    public TeamFollow(User user, Team team) {
        this.user = user;
        this.team = team;
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
