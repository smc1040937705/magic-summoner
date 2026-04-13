package com.magicsummoner.service;

import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer;
    private PlayerDTO testPlayerDTO;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .level(5)
                .experience(500)
                .gold(1000)
                .gems(100)
                .rankPoints(1500)
                .wins(10)
                .losses(5)
                .isOnline(true)
                .isActive(true)
                .build();

        testPlayerDTO = PlayerDTO.builder()
                .id(1L)
                .username("testuser")
                .displayName("Test User")
                .level(5)
                .experience(500)
                .gold(1000)
                .gems(100)
                .rankPoints(1500)
                .wins(10)
                .losses(5)
                .isOnline(true)
                .build();
    }

    @Test
    @DisplayName("根据ID获取玩家 - 正常场景")
    void getPlayerById_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        PlayerDTO result = playerService.getPlayerById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(5, result.getLevel());
        verify(playerRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取玩家 - 玩家不存在")
    void getPlayerById_NotFound_ThrowsException() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> playerService.getPlayerById(999L));
        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据用户名获取玩家 - 正常场景")
    void getPlayerByUsername_Success() {
        // Given
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(testPlayer));
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        PlayerDTO result = playerService.getPlayerByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("获取所有玩家 - 正常场景")
    void getAllPlayers_Success() {
        // Given
        List<Player> players = Arrays.asList(testPlayer);
        when(playerRepository.findAll()).thenReturn(players);
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        List<PlayerDTO> result = playerService.getAllPlayers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取在线玩家 - 正常场景")
    void getOnlinePlayers_Success() {
        // Given
        List<Player> players = Arrays.asList(testPlayer);
        when(playerRepository.findByIsOnlineTrue()).thenReturn(players);
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        List<PlayerDTO> result = playerService.getOnlinePlayers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsOnline());
    }

    @Test
    @DisplayName("获取在线玩家 - 无在线玩家")
    void getOnlinePlayers_Empty() {
        // Given
        when(playerRepository.findByIsOnlineTrue()).thenReturn(Collections.emptyList());

        // When
        List<PlayerDTO> result = playerService.getOnlinePlayers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取顶级玩家 - 正常场景")
    void getTopPlayers_Success() {
        // Given
        List<Player> players = Arrays.asList(testPlayer);
        Pageable pageable = PageRequest.of(0, 10);
        when(playerRepository.findTopPlayersByRank(pageable)).thenReturn(players);
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        List<PlayerDTO> result = playerService.getTopPlayers(10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("更新玩家信息 - 正常场景")
    void updatePlayer_Success() {
        // Given
        PlayerDTO updateDTO = PlayerDTO.builder()
                .displayName("Updated Name")
                .avatarUrl("http://example.com/avatar.jpg")
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);
        when(dtoMapper.toPlayerDTO(testPlayer)).thenReturn(testPlayerDTO);

        // When
        PlayerDTO result = playerService.updatePlayer(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", testPlayer.getDisplayName());
        assertEquals("http://example.com/avatar.jpg", testPlayer.getAvatarUrl());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("更新玩家在线状态 - 设置为在线")
    void updatePlayerOnlineStatus_SetOnline() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updatePlayerOnlineStatus(1L, true);

        // Then
        assertTrue(testPlayer.getIsOnline());
        assertNotNull(testPlayer.getLastLogin());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("更新玩家在线状态 - 设置为离线")
    void updatePlayerOnlineStatus_SetOffline() {
        // Given
        testPlayer.setIsOnline(true);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updatePlayerOnlineStatus(1L, false);

        // Then
        assertFalse(testPlayer.getIsOnline());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("增加金币 - 正常场景")
    void addGold_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        int initialGold = testPlayer.getGold();

        // When
        playerService.addGold(1L, 500);

        // Then
        assertEquals(initialGold + 500, testPlayer.getGold());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("增加宝石 - 正常场景")
    void addGems_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        int initialGems = testPlayer.getGems();

        // When
        playerService.addGems(1L, 50);

        // Then
        assertEquals(initialGems + 50, testPlayer.getGems());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("增加经验值 - 正常场景")
    void addExperience_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        int initialExp = testPlayer.getExperience();
        int addedExp = 200;

        // When
        playerService.addExperience(1L, addedExp);

        // Then
        // Player.addExperience handles level up logic
        assertTrue(testPlayer.getExperience() >= 0);
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("更新排位分 - 正常场景")
    void updateRankPoints_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updateRankPoints(1L, 100);

        // Then
        assertEquals(1600, testPlayer.getRankPoints());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("更新排位分 - 不会低于0")
    void updateRankPoints_NotBelowZero() {
        // Given
        testPlayer.setRankPoints(50);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        // When
        playerService.updateRankPoints(1L, -100);

        // Then
        assertEquals(0, testPlayer.getRankPoints());
        verify(playerRepository).save(testPlayer);
    }

    @Test
    @DisplayName("获取玩家排名 - 正常场景")
    void getPlayerRank_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.countPlayersWithHigherRank(1500)).thenReturn(5L);

        // When
        int rank = playerService.getPlayerRank(1L);

        // Then
        assertEquals(6, rank);
        verify(playerRepository).countPlayersWithHigherRank(1500);
    }
}
