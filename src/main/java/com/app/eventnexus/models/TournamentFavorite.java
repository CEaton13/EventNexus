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
 * JPA entity representing a user bookmarking a tournament.
 * Maps to the {@code tournament_favorites} table.
 * A user may favorite the same tournament only once (unique constraint on user_id + tournament_id).
 */
@Entity
@Table(
    name = "tournament_favorites",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_tournament_fav",
        columnNames = {"user_id", "tournament_id"}
    )
)
public class TournamentFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public TournamentFavorite() {}

    public TournamentFavorite(User user, Tournament tournament) {
        this.user = user;
        this.tournament = tournament;
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
