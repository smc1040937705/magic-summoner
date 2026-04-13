package com.magicsummoner.service;

import com.magicsummoner.dto.AuthResponse;
import com.magicsummoner.dto.LoginRequest;
import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.dto.RegisterRequest;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.security.JwtTokenProvider;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "password123",
                "new@example.com",
                "New User"
        );

        Player savedPlayer = Player.builder()
                .id(1L)
                .username("newuser")
                .email("new@example.com")
                .displayName("New User")
                .build();

        PlayerDTO playerDTO = PlayerDTO.builder()
                .id(1L)
                .username("newuser")
                .displayName("New User")
                .build();

        when(playerRepository.existsByUsername("newuser")).thenReturn(false);
        when(playerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(playerRepository.save(any(Player.class))).thenReturn(savedPlayer);
        when(jwtTokenProvider.generateToken(1L, "newuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(dtoMapper.toPlayerDTO(savedPlayer)).thenReturn(playerDTO);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertEquals(playerDTO, response.getPlayer());

        verify(playerRepository).existsByUsername("newuser");
        verify(playerRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest(
                "existinguser",
                "password123",
                "user@example.com",
                null
        );

        when(playerRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("Username already exists", exception.getMessage());
        verify(playerRepository).existsByUsername("existinguser");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "password123",
                "existing@example.com",
                null
        );

        when(playerRepository.existsByUsername("newuser")).thenReturn(false);
        when(playerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("Email already exists", exception.getMessage());
        verify(playerRepository).existsByEmail("existing@example.com");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void register_WithoutDisplayName_ShouldUseUsernameAsDisplayName() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "password123",
                "new@example.com",
                null
        );

        Player savedPlayer = Player.builder()
                .id(1L)
                .username("newuser")
                .displayName("newuser")
                .build();

        when(playerRepository.existsByUsername(anyString())).thenReturn(false);
        when(playerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            assertEquals("newuser", player.getDisplayName());
            return savedPlayer;
        });
        when(jwtTokenProvider.generateToken(any(), any())).thenReturn("token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(dtoMapper.toPlayerDTO(any())).thenReturn(PlayerDTO.builder().build());

        authService.register(request);

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest(
                "testuser",
                "password123"
        );

        Player player = Player.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .build();

        PlayerDTO playerDTO = PlayerDTO.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "testuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(dtoMapper.toPlayerDTO(player)).thenReturn(playerDTO);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertTrue(player.getIsOnline());
        assertNotNull(player.getLastLogin());

        verify(playerRepository).save(player);
    }

    @Test
    void login_WithNonExistentUsername_ShouldThrowException() {
        LoginRequest request = new LoginRequest(
                "nonexistent",
                "password123"
        );

        when(playerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void login_WithWrongPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest(
                "testuser",
                "wrongpassword"
        );

        Player player = Player.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .build();

        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void logout_WithValidPlayerId_ShouldSetPlayerOffline() {
        Player player = Player.builder()
                .id(1L)
                .username("testuser")
                .isOnline(true)
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        authService.logout(1L);

        assertFalse(player.getIsOnline());
        verify(playerRepository).save(player);
    }

    @Test
    void logout_WithInvalidPlayerId_ShouldThrowException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.logout(999L));

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void getCurrentPlayer_WithValidId_ShouldReturnPlayerDTO() {
        Player player = Player.builder()
                .id(1L)
                .username("testuser")
                .build();

        PlayerDTO playerDTO = PlayerDTO.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(dtoMapper.toPlayerDTO(player)).thenReturn(playerDTO);

        PlayerDTO result = authService.getCurrentPlayer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getCurrentPlayer_WithInvalidId_ShouldThrowException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.getCurrentPlayer(999L));

        assertEquals("Player not found", exception.getMessage());
    }
}
