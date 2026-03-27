package com.app.eventnexus.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter that enforces per-IP rate limits on authentication endpoints.
 *
 * <p>Limits:
 * <ul>
 *   <li>{@code POST /api/auth/login} — 10 requests per minute per IP</li>
 *   <li>{@code POST /api/auth/register} — 5 requests per minute per IP</li>
 * </ul>
 *
 * <p>Uses an in-memory {@link ConcurrentHashMap} of {@link Bucket} instances.
 * This is suitable for single-instance deployments. For clustered environments,
 * replace with a distributed cache (Redis + bucket4j-redis).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH    = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    private final ConcurrentHashMap<String, Bucket> loginBuckets    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri    = request.getRequestURI();
        String method = request.getMethod();

        if (!"POST".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (uri.equals(LOGIN_PATH)) {
            if (!tryConsume(loginBuckets, clientIp(request), 10)) {
                rejectRateLimited(response);
                return;
            }
        } else if (uri.equals(REGISTER_PATH)) {
            if (!tryConsume(registerBuckets, clientIp(request), 5)) {
                rejectRateLimited(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryConsume(ConcurrentHashMap<String, Bucket> store, String key, int capacity) {
        Bucket bucket = store.computeIfAbsent(key, k -> buildBucket(capacity));
        return bucket.tryConsume(1);
    }

    private Bucket buildBucket(int capacity) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Take the first IP in the chain (the original client)
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void rejectRateLimited(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
    }
}
