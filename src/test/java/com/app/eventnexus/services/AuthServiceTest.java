package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.RegisterRequest;
import com.app.eventnexus.dtos.responses.UserResponse;
import com.app.eventnexus.enums.UserRole;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.models.User;
import com.app.eventnexus.repositories.RefreshTokenRepository;
import com.app.eventnexus.repositories.UserRepository;
import com.app.eventnexus.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 * Tests focus on registration validation: duplicate username, duplicate email,
 * optional role defaulting to SPECTATOR.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private OrganizationService organizationService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("SecurePass1!");
    }

    @Test
    void register_withUniqueCredentials_returnsUserResponse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("testuser");
        saved.setEmail("test@example.com");
        saved.setRole(UserRole.SPECTATOR);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo(UserRole.SPECTATOR);
    }

    @Test
    void register_defaultsToSpectatorWhenRoleIsNull() {
        request.setRole(null);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("testuser");
        saved.setEmail("test@example.com");
        saved.setRole(UserRole.SPECTATOR);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo(UserRole.SPECTATOR);
    }

    @Test
    void register_throwsConflictException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsConflictException_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("test@example.com");

        verify(userRepository, never()).save(any());
    }
}
