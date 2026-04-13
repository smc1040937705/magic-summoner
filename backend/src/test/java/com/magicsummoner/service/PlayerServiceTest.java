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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .gold(100)
                .gems(10)
                .rankPoints(1500)
                .isOnline(true)
                .build();

        testPlayerDTO = new PlayerDTO();
        testPlayerDTO.setId(1L);
        testPlayerDTO.setUsername("testuser");
        testPlayerDTO.setDisplayName("Test User");
        testPlayerDTO.setGold(100);
        testPlayerDTO.setGems(10);
        testPlayerDTO.setRankPoints(1500);
    }

    @Test
    @DisplayName("根据ID获取玩家成功")
    void getPlayerById_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(testPlayerDTO);

        PlayerDTO result = playerService.getPlayerById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(playerRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取玩家失败 - 玩家不存在")
    void getPlayerById_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.getPlayerById(999L);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据用户名获取玩家成功")
    void getPlayerByUsername_Success() {
        when(playerRepository.findByUsername("testuser")).thenReturn(Optional.of(testPlayer));
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(testPlayerDTO);

        PlayerDTO result = playerService.getPlayerByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("根据用户名获取玩家失败 - 玩家不存在")
    void getPlayerByUsername_NotFound_ThrowsException() {
        when(playerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.getPlayerByUsername("nonexistent");
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有玩家")
    void getAllPlayers_Success() {
        Player player2 = Player.builder().id(2L).username("user2").build();
        List<Player> players = Arrays.asList(testPlayer, player2);

        when(playerRepository.findAll()).thenReturn(players);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(testPlayerDTO);

        List<PlayerDTO> result = playerService.getAllPlayers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("获取在线玩家")
    void getOnlinePlayers_Success() {
        List<Player> onlinePlayers = Arrays.asList(testPlayer);

        when(playerRepository.findByIsOnlineTrue()).thenReturn(onlinePlayers);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(testPlayerDTO);

        List<PlayerDTO> result = playerService.getOnlinePlayers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取排行榜前N名玩家")
    void getTopPlayers_Success() {
        List<Player> topPlayers = Arrays.asList(testPlayer);

        when(playerRepository.findTopPlayersByRank(any(PageRequest.class))).thenReturn(topPlayers);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(testPlayerDTO);

        List<PlayerDTO> result = playerService.getTopPlayers(10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("更新玩家信息成功")
    void updatePlayer_Success() {
        PlayerDTO updateDTO = new PlayerDTO();
        updateDTO.setDisplayName("Updated Name");
        updateDTO.setAvatarUrl("http://example.com/avatar.png");

        Player updatedPlayer = Player.builder()
                .id(1L)
                .username("testuser")
                .displayName("Updated Name")
                .avatarUrl("http://example.com/avatar.png")
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(updatedPlayer);
        when(dtoMapper.toPlayerDTO(any(Player.class))).thenReturn(updateDTO);

        PlayerDTO result = playerService.updatePlayer(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getDisplayName());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("更新玩家信息失败 - 玩家不存在")
    void updatePlayer_NotFound_ThrowsException() {
        PlayerDTO updateDTO = new PlayerDTO();
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.updatePlayer(999L, updateDTO);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("更新玩家在线状态 - 上线")
    void updatePlayerOnlineStatus_Online_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.updatePlayerOnlineStatus(1L, true));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("更新玩家在线状态 - 下线")
    void updatePlayerOnlineStatus_Offline_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.updatePlayerOnlineStatus(1L, false));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("更新玩家在线状态失败 - 玩家不存在")
    void updatePlayerOnlineStatus_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.updatePlayerOnlineStatus(999L, true);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("增加金币成功")
    void addGold_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.addGold(1L, 50));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("增加金币失败 - 玩家不存在")
    void addGold_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.addGold(999L, 50);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("增加宝石成功")
    void addGems_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.addGems(1L, 10));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("增加宝石失败 - 玩家不存在")
    void addGems_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.addGems(999L, 10);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("增加经验值成功")
    void addExperience_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.addExperience(1L, 100));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("增加经验值失败 - 玩家不存在")
    void addExperience_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.addExperience(999L, 100);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("更新排位积分成功 - 增加")
    void updateRankPoints_Increase_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.updateRankPoints(1L, 100));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("更新排位积分成功 - 减少")
    void updateRankPoints_Decrease_Success() {
        testPlayer.setRankPoints(100);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.updateRankPoints(1L, -50));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("更新排位积分 - 积分不会为负数")
    void updateRankPoints_NotNegative_Success() {
        testPlayer.setRankPoints(50);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(testPlayer);

        assertDoesNotThrow(() -> playerService.updateRankPoints(1L, -100));

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("获取玩家排名成功")
    void getPlayerRank_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(playerRepository.countPlayersWithHigherRank(1500)).thenReturn(5L);

        int rank = playerService.getPlayerRank(1L);

        assertEquals(6, rank);
    }

    @Test
    @DisplayName("获取玩家排名失败 - 玩家不存在")
    void getPlayerRank_NotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.getPlayerRank(999L);
        });

        assertEquals("Player not found", exception.getMessage());
    }
}
