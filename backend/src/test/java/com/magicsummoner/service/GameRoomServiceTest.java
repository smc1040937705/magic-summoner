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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
                .username("hostuser")
                .build();

        guestPlayer = Player.builder()
                .id(2L)
                .username("guestuser")
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

        testRoomDTO = GameRoomDTO.builder()
                .id(1L)
                .roomCode("ABC123")
                .name("Test Room")
                .status(GameRoomStatus.WAITING)
                .build();
    }

    @Test
    @DisplayName("根据ID获取房间 - 正常场景")
    void getRoomById_Success() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.getRoomById(1L);

        // Then
        assertNotNull(result);
        assertEquals("ABC123", result.getRoomCode());
    }

    @Test
    @DisplayName("根据ID获取房间 - 房间不存在")
    void getRoomById_NotFound_ThrowsException() {
        // Given
        when(gameRoomRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.getRoomById(999L));
        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据房间代码获取房间 - 正常场景")
    void getRoomByCode_Success() {
        // Given
        when(gameRoomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.getRoomByCode("ABC123");

        // Then
        assertNotNull(result);
        assertEquals("ABC123", result.getRoomCode());
    }

    @Test
    @DisplayName("获取可用房间列表 - 正常场景")
    void getAvailableRooms_Success() {
        // Given
        List<GameRoom> rooms = Arrays.asList(testRoom);
        when(gameRoomRepository.findAvailablePublicRooms()).thenReturn(rooms);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        List<GameRoomDTO> result = gameRoomService.getAvailableRooms();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("获取可用房间列表 - 空列表")
    void getAvailableRooms_Empty() {
        // Given
        when(gameRoomRepository.findAvailablePublicRooms()).thenReturn(Collections.emptyList());

        // When
        List<GameRoomDTO> result = gameRoomService.getAvailableRooms();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建房间 - 正常场景")
    void createRoom_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.createRoom(1L, "My Room", false, null, 120);

        // Then
        assertNotNull(result);
        verify(gameRoomRepository).save(any(GameRoom.class));
    }

    @Test
    @DisplayName("创建房间 - 玩家不存在")
    void createRoom_PlayerNotFound_ThrowsException() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.createRoom(999L, "Room", false, null, 120));
        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("创建房间 - 使用默认名称")
    void createRoom_DefaultName() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        gameRoomService.createRoom(1L, null, false, null, null);

        // Then
        verify(gameRoomRepository).save(argThat(room -> room.getName().contains("hostuser")));
    }

    @Test
    @DisplayName("加入房间 - 正常场景")
    void joinRoom_Success() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(guestPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.joinRoom(1L, 2L, null);

        // Then
        assertNotNull(result);
        assertEquals(guestPlayer, testRoom.getGuest());
    }

    @Test
    @DisplayName("加入房间 - 房间已满")
    void joinRoom_RoomFull_ThrowsException() {
        // Given
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.joinRoom(1L, 3L, null));
        assertEquals("Room is full", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间 - 房间不在等待状态")
    void joinRoom_NotWaiting_ThrowsException() {
        // Given
        testRoom.setStatus(GameRoomStatus.PLAYING);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.joinRoom(1L, 2L, null));
        assertEquals("Room is not available", exception.getMessage());
    }

    @Test
    @DisplayName("加入房间 - 密码错误")
    void joinRoom_WrongPassword_ThrowsException() {
        // Given
        testRoom.setIsPrivate(true);
        testRoom.setPassword("correctpass");
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.joinRoom(1L, 2L, "wrongpass"));
        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    @DisplayName("离开房间 - 房主离开")
    void leaveRoom_HostLeaves() {
        // Given
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.leaveRoom(1L, 1L);

        // Then
        assertEquals(GameRoomStatus.ABANDONED, testRoom.getStatus());
    }

    @Test
    @DisplayName("离开房间 - 客人离开")
    void leaveRoom_GuestLeaves() {
        // Given
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.leaveRoom(1L, 2L);

        // Then
        assertNull(testRoom.getGuest());
    }

    @Test
    @DisplayName("开始游戏 - 正常场景")
    void startGame_Success() {
        // Given
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.startGame(1L, 1L);

        // Then
        assertEquals(GameRoomStatus.PLAYING, testRoom.getStatus());
        assertNotNull(testRoom.getStartedAt());
    }

    @Test
    @DisplayName("开始游戏 - 非房主")
    void startGame_NotHost_ThrowsException() {
        // Given
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.startGame(1L, 2L));
        assertEquals("Only host can start the game", exception.getMessage());
    }

    @Test
    @DisplayName("开始游戏 - 房间未满")
    void startGame_RoomNotFull_ThrowsException() {
        // Given
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.startGame(1L, 1L));
        assertEquals("Room is not full", exception.getMessage());
    }

    @Test
    @DisplayName("结束游戏 - 正常场景")
    void endGame_Success() {
        // Given
        testRoom.setStatus(GameRoomStatus.PLAYING);
        testRoom.setGuest(guestPlayer);
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(hostPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.endGame(1L, 1L);

        // Then
        assertEquals(GameRoomStatus.FINISHED, testRoom.getStatus());
        assertNotNull(testRoom.getEndedAt());
        assertEquals(hostPlayer, testRoom.getWinner());
    }

    @Test
    @DisplayName("通过代码加入房间 - 正常场景")
    void joinRoomByCode_Success() {
        // Given
        // joinRoomByCode calls findByRoomCode then joinRoom
        // joinRoom calls findById, so we need to mock both
        when(gameRoomRepository.findByRoomCode("ABC123")).thenReturn(Optional.of(testRoom));
        when(gameRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(guestPlayer));
        when(gameRoomRepository.save(any(GameRoom.class))).thenReturn(testRoom);
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.joinRoomByCode("ABC123", 2L, null);

        // Then
        assertNotNull(result);
        assertEquals(guestPlayer, testRoom.getGuest());
    }

    @Test
    @DisplayName("通过代码加入房间 - 房间代码不存在")
    void joinRoomByCode_RoomNotFound_ThrowsException() {
        // Given
        when(gameRoomRepository.findByRoomCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameRoomService.joinRoomByCode("INVALID", 2L, null));
        assertEquals("Room not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取玩家活跃游戏 - 有活跃游戏")
    void getActiveGameByPlayer_Success() {
        // Given
        when(gameRoomRepository.findActiveGameByPlayerId(1L)).thenReturn(Optional.of(testRoom));
        when(dtoMapper.toGameRoomDTO(testRoom)).thenReturn(testRoomDTO);

        // When
        GameRoomDTO result = gameRoomService.getActiveGameByPlayer(1L);

        // Then
        assertNotNull(result);
        assertEquals("ABC123", result.getRoomCode());
    }

    @Test
    @DisplayName("获取玩家活跃游戏 - 无活跃游戏")
    void getActiveGameByPlayer_NoActiveGame() {
        // Given
        when(gameRoomRepository.findActiveGameByPlayerId(1L)).thenReturn(Optional.empty());

        // When
        GameRoomDTO result = gameRoomService.getActiveGameByPlayer(1L);

        // Then
        assertNull(result);
    }
}
