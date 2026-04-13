package com.magicsummoner.service;

import com.magicsummoner.dto.GameRoomDTO;
import com.magicsummoner.entity.GameRoom;
import com.magicsummoner.entity.Player;
import com.magicsummoner.enums.GameRoomStatus;
import com.magicsummoner.repository.GameRoomRepository;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRoomServiceTest {

    @Mock
    private GameRoomRepository gameRoomRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private GameRoomService gameRoomService;

    private Player hostPlayer;
    private Player guestPlayer;
    private GameRoom testRoom;
    private GameRoomDTO testRoomDTO;

    @BeforeEach
    void setUp() {
        hostPlayer = Player.builder()
                .id(1L)
                .username("host")
                .build();

        guestPlayer = Player.builder()
                .id(2L)
                .username("guest")
                .build();

        testRoom = GameRoom.builder()
                .id(1L)
                .roomCode("ABC123")
                .name("Test Room")
                .host(hostPlayer)
                .isPrivate(false)
                .status(GameRoomStatus.WAITING)
                .turnTimeLimit(120)
                .build();

        testRoomDTO = new GameRoomDTO();
        testRoomDTO.setId(1L);
        testRoomDTO.setRoomCode("ABC123");
        testRoomDTO.setName("Test Room");
        testRoomDTO.setStatus(GameRoomStatus.WAITING);
    }

    @Test
    @DisplayName("根据ID获取房间成功")
    void getRoomById_Success() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("Test Room", result.getName());
        verify(gameRoomRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取房间失败 - 房间不存在")
    void getRoomById_NotFound_ThrowsException() {
        when(gameRoomRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.getRoomById(999L);
        });

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据房间码获取房间成功")
    void getRoomByCode_Success() {
        when(gameRoomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.getRoomByCode("ABC123");

        assertNotNull(result);
        assertEquals("ABC123", result.getRoomCode());
    }

    @Test
    @DisplayName("根据房间码获取房间失败 - 房间不存在")
    void getRoomByCode_NotFound_ThrowsException() {
        when(gameRoomRepository.findByRoomCode("INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.getRoomByCode("INVALID");
        });

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取可用房间列表")
    void getAvailableRooms_Success() {
        List<GameRoom> rooms = Arrays.asList(testRoom);
        when(gameRoomRepository.findAvailablePublicRooms()).thenReturn(rooms);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        List<GameRoomDTO> result = gameRoomService.getAvailableRooms();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据状态获取房间列表")
    void getRoomsByStatus_Success() {
        List<GameRoom> rooms = Arrays.asList(testRoom);
        when(gameRoomRepository.findByStatus(GameRoomStatus.WAITING)).thenReturn(rooms);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        List<GameRoomDTO> result = gameRoomService.getRoomsByStatus(GameRoomStatus.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("创建房间成功 - 公开房间")
    void createRoom_Public_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.createRoom(1L, "New Room", false, null, 120);

        assertNotNull(result);
        verify(gameRoomRepository).save(any(GameRoom.class));
    }

    @Test
    @DisplayName("创建房间成功 - 私有房间")
    void createRoom_Private_Success() {
        GameRoom privateRoom = GameRoom.builder()
                .id(2L)
                .roomCode("PRIVATE")
                .name("Private Room")
                .host(hostPlayer)
                .isPrivate(true)
                .password("secret123")
                .status(GameRoomStatus.WAITING)
                .build();

        GameRoomDTO privateRoomDTO = new GameRoomDTO();
        privateRoomDTO.setId(2L);
        privateRoomDTO.setName("Private Room");
        privateRoomDTO.setIsPrivate(true);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(privateRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(privateRoomDTO);

        GameRoomDTO result = gameRoomService.createRoom(1L, "Private Room", true, "secret123", 120);

        assertNotNull(result);
    }

    @Test
    @DisplayName("创建房间失败 - 玩家不存在")
    void createRoom_PlayerNotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.createRoom(999L, "New Room", false, null, 120);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间成功")
    void joinRoom_Success() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(guestPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.joinRoom(1L, 2L, null);

        assertNotNull(result);
        verify(gameRoomRepository).save(any(GameRoom.class));
    }

    @Test
    @DisplayName("加入房间失败 - 房间不存在")
    void joinRoom_RoomNotFound_ThrowsException() {
        when(gameRoomRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(999L, 2L, null);
        });

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间失败 - 房间不可用")
    void joinRoom_RoomNotWaiting_ThrowsException() {
        testRoom.setStatus(GameRoomStatus.PLAYING);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(1L, 2L, null);
        });

        assertEquals("Room is not available", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间失败 - 房间已满")
    void joinRoom_RoomFull_ThrowsException() {
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(1L, 3L, null);
        });

        assertEquals("Room is full", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间失败 - 私有房间密码错误")
    void joinRoom_InvalidPassword_ThrowsException() {
        testRoom.setIsPrivate(true);
        testRoom.setPassword("correct");
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(1L, 2L, "wrong");
        });

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间失败 - 私有房间未提供密码")
    void joinRoom_NoPassword_ThrowsException() {
        testRoom.setIsPrivate(true);
        testRoom.setPassword("secret");
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(1L, 2L, null);
        });

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间失败 - 玩家不存在")
    void joinRoom_PlayerNotFound_ThrowsException() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.joinRoom(1L, 999L, null);
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("通过房间码加入房间成功")
    void joinRoomByCode_Success() {
        when(gameRoomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(guestPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.joinRoomByCode("ABC123", 2L, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("离开房间成功 - 房主离开")
    void leaveRoom_HostLeave_Success() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.leaveRoom(1L, 1L);

        assertNotNull(result);
        verify(gameRoomRepository).save(any(GameRoom.class));
    }

    @Test
    @DisplayName("离开房间成功 - 访客离开")
    void leaveRoom_GuestLeave_Success() {
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.leaveRoom(1L, 2L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("离开房间失败 - 房间不存在")
    void leaveRoom_NotFound_ThrowsException() {
        when(gameRoomRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.leaveRoom(999L, 1L);
        });

        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("开始游戏成功")
    void startGame_Success() {
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.startGame(1L, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("开始游戏失败 - 非房主尝试开始")
    void startGame_NotHost_ThrowsException() {
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.startGame(1L, 2L);
        });

        assertEquals("Only host can start the game", exception.getMessage());
    }

    @Test
    @DisplayName("开始游戏失败 - 房间未满")
    void startGame_NotFull_ThrowsException() {
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameRoomService.startGame(1L, 1L);
        });

        assertEquals("Room is not full", exception.getMessage());
    }

    @Test
    @DisplayName("结束游戏成功")
    void endGame_Success() {
        testRoom.setStatus(GameRoomStatus.PLAYING);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.endGame(1L, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("结束游戏成功 - 无胜者")
    void endGame_NoWinner_Success() {
        testRoom.setStatus(GameRoomStatus.PLAYING);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.endGame(1L, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取玩家活跃游戏")
    void getActiveGameByPlayer_Success() {
        when(gameRoomRepository.findActiveGameByPlayerId(1L)).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(any(GameRoom.class))).thenReturn(testRoomDTO);

        GameRoomDTO result = gameRoomService.getActiveGameByPlayer(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("获取玩家活跃游戏 - 无活跃游戏")
    void getActiveGameByPlayer_NoActiveGame() {
        when(gameRoomRepository.findActiveGameByPlayerId(1L)).thenReturn(Optional.empty());

        GameRoomDTO result = gameRoomService.getActiveGameByPlayer(1L);

        assertNull(result);
    }
}
