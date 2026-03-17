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
 * JPA entity representing a player's statistics scoped to one tournament.
 * Maps to the {@code player_stats} table.
 *
 * <p>The unique constraint {@code uq_player_tournament_stats} ensures one
 * stats record per player per tournament. Use upsert logic in the service
 * (load-or-create) when recording match results.
 *
 * <p>{@code tournamentId} is stored as a plain {@code Long} until the
 * {@code Tournament} entity is introduced in Day 11, at which point it will
 * be upgraded to a {@code @ManyToOne} relationship.
 */
@Entity
@Table(name = "player_stats",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_player_tournament_stats",
               columnNames = {"player_id", "tournament_id"}))
public class PlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "wins", nullable = false)
    private int wins = 0;

    @Column(name = "losses", nullable = false)
    private int losses = 0;

    @Column(name = "mvp_count", nullable = false)
    private int mvpCount = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public PlayerStats() {
    }

    public PlayerStats(Player player, Long tournamentId) {
        this.player = player;
        this.tournamentId = tournamentId;
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getMvpCount() { return mvpCount; }
    public void setMvpCount(int mvpCount) { this.mvpCount = mvpCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
