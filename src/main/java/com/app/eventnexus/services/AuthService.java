package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.LoginRequest;
import com.app.eventnexus.dtos.requests.RefreshTokenRequest;
import com.app.eventnexus.dtos.requests.RegisterRequest;
import com.app.eventnexus.dtos.responses.AuthResponse;
import com.app.eventnexus.dtos.responses.OrganizationMemberResponse;
import com.app.eventnexus.dtos.responses.UserResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.RefreshToken;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.RefreshTokenRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.security.JwtTokenProvider;
import com.app.eventnexus.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling all authentication operations: registration, login,
 * token refresh, and logout.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Username and email must be unique at registration time.</li>
 *   <li>Passwords are hashed with BCrypt before persistence.</li>
 *   <li>Each login invalidates previous refresh tokens for the user
 *       (single active session per user).</li>
 *   <li>Expired refresh tokens are deleted on use attempt.</li>
 * </ul>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OrganizationService organizationService;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       OrganizationService organizationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.organizationService = organizationService;
    }

    // ─── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a new user account.
     * Defaults the role to {@code SPECTATOR} if none is provided in the request.
     *
     * @param request registration details (username, email, password, optional role)
     * @return a {@link UserResponse} for the newly created user
     * @throws ConflictException if the username or email is already in use
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already registered.");
        }

        UserRole role = (request.getRole() != null) ? request.getRole() : UserRole.SPECTATOR;
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role);

        return UserResponse.from(userRepository.save(user));
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and issues a new access token and refresh token.
     * Any previously issued refresh tokens for this user are revoked so that
     * only one active session exists at a time.
     *
     * @param request login credentials (username, password)
     * @return an {@link AuthResponse} containing the access token, refresh token, and user profile
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        // Revoke existing refresh tokens (single session per user)
        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String rawRefreshToken = saveNewRefreshToken(user);
        List<OrganizationMemberResponse> orgs = organizationService.getMembershipsForUser(user.getId());

        return new AuthResponse(accessToken, rawRefreshToken, UserResponse.from(user), orgs);
    }

    // ─── Token Refresh ─────────────────────────────────────────────────────────

    /**
     * Exchanges a valid, non-expired refresh token for a new access token.
     * The same refresh token remains active; it is not rotated on each call.
     *
     * @param request the refresh token to validate
     * @return an {@link AuthResponse} with a fresh access token and the same refresh token
     * @throws ResourceNotFoundException if the token is not found in the database
     * @throws ConflictException if the token has passed its expiry date
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Refresh token not found or has already been invalidated."));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new ConflictException("Refresh token has expired. Please log in again.");
        }

        User user = storedToken.getUser();
        UserPrincipal principal = new UserPrincipal(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);
        List<OrganizationMemberResponse> orgs = organizationService.getMembershipsForUser(user.getId());

        return new AuthResponse(newAccessToken, storedToken.getToken(), UserResponse.from(user), orgs);
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    /**
     * Invalidates all refresh tokens for the given user by deleting them from the database.
     * Silently succeeds if no tokens are found (idempotent).
     *
     * @param userId the ID of the user whose session should be invalidated
     */
    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private String saveNewRefreshToken(User user) {
        String rawToken = jwtTokenProvider.generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000);
        refreshTokenRepository.save(new RefreshToken(user, rawToken, expiresAt));
        return rawToken;
    }
}
