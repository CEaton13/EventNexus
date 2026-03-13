package com.app.eventnexus.enums;

/**
 * Application-level roles assigned to each user.
 * <ul>
 *   <li>TOURNAMENT_ADMIN — full CRUD access to all resources</li>
 *   <li>TEAM_MANAGER — manage own team and players only</li>
 *   <li>SPECTATOR — read-only access to public endpoints</li>
 * </ul>
 */
public enum UserRole {
    TOURNAMENT_ADMIN,
    TEAM_MANAGER,
    SPECTATOR
}
