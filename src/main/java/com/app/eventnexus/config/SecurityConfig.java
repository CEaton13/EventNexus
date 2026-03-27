package com.app.eventnexus.config;

import com.app.eventnexus.security.JwtAuthenticationFilter;
import com.app.eventnexus.tenant.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <ul>
 *   <li>Stateless JWT-based authentication — no HTTP sessions.</li>
 *   <li>CSRF disabled (tokens replace the CSRF protection model).</li>
 *   <li>Public routes are whitelisted; all others require a valid Bearer token.</li>
 *   <li>{@code @PreAuthorize} is enabled via {@code @EnableMethodSecurity}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantFilter tenantFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantFilter = tenantFilter;
    }

    /**
     * Defines the main security filter chain.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Auth (all public) ──────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()

                // ── Org-scoped public reads ────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/tournaments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/tournaments/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/tournaments/*/bracket").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/tournaments/*/standings").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/tournaments/*/teams").permitAll()

                // ── Public tournament hub (org-agnostic read) ─────────────
                .requestMatchers(HttpMethod.GET, "/api/tournaments/**").permitAll()

                // ── Teams (public read, except /mine which needs auth) ────
                .requestMatchers(HttpMethod.GET, "/api/teams").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/teams/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/teams/*/players").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/teams/*/tournaments").permitAll()

                // ── Matches (public read) ─────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/matches/**").permitAll()

                // ── Players (public read) ──────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/players/**").permitAll()

                // ── Genres (fully public) ──────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/genres/**").permitAll()

                // ── Venues (public read) ───────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/orgs/*/venues/**").permitAll()

                // ── Organizations meta (public slug lookup) ────────────────
                .requestMatchers(HttpMethod.GET, "/api/organizations/*").permitAll()

                // ── Actuator ──────────────────────────────────────────────
                .requestMatchers("/actuator/**").permitAll()

                // ── Everything else requires authentication ────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so that
     * {@code AuthService} can authenticate credentials during login.
     *
     * @param config Spring's {@link AuthenticationConfiguration}
     * @return the configured authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder used for hashing and verifying passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
