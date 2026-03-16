package com.app.eventnexus.security;

import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} adapter wrapping a {@link User} entity.
 * Authorities are prefixed with {@code ROLE_} so that
 * {@code @PreAuthorize("hasRole('TOURNAMENT_ADMIN')")} resolves correctly.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // ─── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    // ─── Convenience accessors ─────────────────────────────────────────────────

    /**
     * Returns the database ID of the underlying user.
     *
     * @return user ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Returns the role of the underlying user.
     *
     * @return user role enum
     */
    public UserRole getRole() {
        return user.getRole();
    }

    /**
     * Returns the underlying {@link User} entity.
     * Use only within the security layer — do not pass this to controllers.
     *
     * @return the wrapped User entity
     */
    public User getUser() {
        return user;
    }
}
