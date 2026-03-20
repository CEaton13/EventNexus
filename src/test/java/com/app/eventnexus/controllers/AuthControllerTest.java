package com.app.eventnexus.controllers;

import com.app.eventnexus.config.CorsConfig;
import com.app.eventnexus.config.SecurityConfig;
import com.app.eventnexus.dtos.responses.AuthResponse;
import com.app.eventnexus.dtos.responses.UserResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.repositories.OrganizationMemberRepository;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.security.JwtTokenProvider;
import com.app.eventnexus.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link AuthController}.
 * Uses {@code @WebMvcTest} so no database is needed — {@link AuthService} is
 * mocked, and Spring Security + {@link com.app.eventnexus.config.SecurityConfig}
 * are loaded to verify the correct HTTP status codes are returned.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // Required by TenantFilter loaded with SecurityConfig
    @MockitoBean
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private OrganizationMemberRepository organizationMemberRepository;

    // Required by JwtAuthenticationFilter which is loaded as part of the web layer
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // Required by JwtAuthenticationFilter
    @MockitoBean
    private UserDetailsService userDetailsService;

    // ─── Fixtures ─────────────────────────────────────────────────────────────

    private UserResponse sampleUser() {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("admin1");
        user.setEmail("admin@test.com");
        user.setRole(UserRole.TOURNAMENT_ADMIN);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse("sample.access.token", "sample-refresh-uuid", sampleUser(), List.of());
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @Test
    void register_returnsCreatedWithUserProfile_whenRequestIsValid() throws Exception {
        when(authService.register(any())).thenReturn(sampleUser());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin1",
                                  "email": "admin@test.com",
                                  "password": "password123",
                                  "role": "TOURNAMENT_ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("TOURNAMENT_ADMIN"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void register_returnsConflict_whenUsernameIsAlreadyTaken() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ConflictException("Username 'admin1' is already taken."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin1",
                                  "email": "other@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username 'admin1' is already taken."));
    }

    @Test
    void register_returnsConflict_whenEmailIsAlreadyRegistered() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ConflictException("Email 'admin@test.com' is already registered."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "email": "admin@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email 'admin@test.com' is already registered."));
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Test
    void login_returnsOkWithTokens_whenCredentialsAreValid() throws Exception {
        when(authService.login(any())).thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin1",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("sample.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("sample-refresh-uuid"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin1"));
    }

    @Test
    void login_returnsUnauthorized_whenPasswordIsWrong() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin1",
                                  "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Test
    void refresh_returnsNewAccessToken_whenRefreshTokenIsValid() throws Exception {
        when(authService.refreshToken(any())).thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "sample-refresh-uuid" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("sample.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("sample-refresh-uuid"));
    }

    @Test
    void refresh_returnsConflict_whenRefreshTokenIsExpired() throws Exception {
        when(authService.refreshToken(any()))
                .thenThrow(new ConflictException("Refresh token has expired. Please log in again."));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "expired-token-value" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Refresh token has expired. Please log in again."));
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void logout_returnsNoContent_whenAuthenticatedAndTokenIsValid() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "sample-refresh-uuid" }
                                """))
                .andExpect(status().isNoContent());
    }
}
