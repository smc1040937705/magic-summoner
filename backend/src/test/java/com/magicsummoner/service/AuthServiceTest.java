package com.magicsummoner.service;

import com.magicsummoner.dto.AuthResponse;
import com.magicsummoner.dto.LoginRequest;
import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.dto.RegisterRequest;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.security.JwtTokenProvider;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private Player testPlayer;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private PlayerDTO playerDTO;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .displayName("Test User")
                .gold(100)
                .gems(0)
                .rankPoints(0)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setDisplayName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        playerDTO = new PlayerDTO();
        playerDTO.setId(1L);
        playerDTO.setUsername("testuser");
        playerDTO.setDisplayName("Test User");
    }

    @Test
    @DisplayName("注册成功 - 返回token和player信息")
    void register_Success() {
        when(playerRepository.existsByUsername("testuser")).thenReturn(false);
        when(playerRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        when(jwtTokenProvider.generateToken(1L, "testuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(playerDTO);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getPlayer());
        assertEquals("testuser", response.getPlayer().getUsername());

        verify(playerRepository).save(any(Player.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists_ThrowsException() {
        when(playerRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void register_EmailExists_ThrowsException() {
        when(playerRepository.existsByUsername("testuser")).thenReturn(false);
        when(playerRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    @DisplayName("注册成功 - 使用username作为默认displayName")
    void register_DefaultDisplayName() {
        registerRequest.setDisplayName(null);
        
        when(playerRepository.existsByUsername(anyString())).thenReturn(false);
        when(playerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        when(jwtTokenProvider.generateToken(anyLong(), anyString())).thenReturn("token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(playerDTO);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
    }

    @Test
    @DisplayName("登录成功 - 返回token和player信息")
    void login_Success() {
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(testPlayer));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "testuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(playerDTO);
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getPlayer());

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound_ThrowsException() {
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword_ThrowsException() {
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(testPlayer));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("登出成功 - 更新在线状态")
    void logout_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> authService.logout(1L));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("登出失败 - 用户不存在")
    void logout_PlayerNotFound_ThrowsException() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.logout(1L);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取当前用户成功")
    void getCurrentPlayer_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(playerDTO);

        PlayerDTO result = authService.getCurrentPlayer(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("获取当前用户失败 - 用户不存在")
    void getCurrentPlayer_NotFound_ThrowsException() {
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentPlayer(1L);
        });

        assertEquals("Player not found", exception.getMessage());
    }
}
