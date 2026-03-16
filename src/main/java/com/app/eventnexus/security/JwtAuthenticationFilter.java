package com.app.eventnexus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that runs once per request to extract and validate a JWT
 * Bearer token from the {@code Authorization} header.
 *
 * <p>If the token is valid, a {@link UsernamePasswordAuthenticationToken} is
 * placed into the {@link SecurityContextHolder} so that downstream filters and
 * {@code @PreAuthorize} expressions can inspect the principal.
 *
 * <p>Any failure (missing header, expired token, unknown user) is silently
 * swallowed and the request continues unauthenticated — Spring Security's
 * {@code ExceptionTranslationFilter} will return 401 if a protected route is
 * reached without authentication.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                // Token was structurally valid but user lookup failed or account
                // was deactivated; leave SecurityContext empty — 401 will follow.
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT from the {@code Authorization: Bearer <token>} header.
     *
     * @param request the incoming HTTP request
     * @return the token string, or null if the header is absent or malformed
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
