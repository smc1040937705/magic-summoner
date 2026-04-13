package com.magicsummoner.service;

import com.magicsummoner.dto.GameRoomDTO;
import com.magicsummoner.entity.GameRoom;
import com.magicsummoner.entity.Player;
import com.magicsummoner.enums.GameRoomStatus;
import com.magicsummoner.repository.GameRoomRepository;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomService {
    
    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository;
    private final DtoMapper dtoMapper;
    private final Random random = new Random();
    
    public GameRoomDTO getRoomById(Long roomId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        return dtoMapper.toGameRoomDTO(room);
    }
    
    public GameRoomDTO getRoomByCode(String roomCode) {
        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        return dtoMapper.toGameRoomDTO(room);
    }
    
    public List<GameRoomDTO> getAvailableRooms() {
        return gameRoomRepository.findAvailablePublicRooms().stream()
            .map(dtoMapper::toGameRoomDTO)
            .collect(Collectors.toList());
    }
    
    public List<GameRoomDTO> getRoomsByStatus(GameRoomStatus status) {
        return gameRoomRepository.findByStatus(status).stream()
            .map(dtoMapper::toGameRoomDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public GameRoomDTO createRoom(Long hostId, String name, Boolean isPrivate, String password, Integer turnTimeLimit) {
        Player host = playerRepository.findById(hostId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        String roomCode = generateRoomCode();
        while (gameRoomRepository.existsByRoomCode(roomCode)) {
            roomCode = generateRoomCode();
        }
        
        GameRoom room = GameRoom.builder()
            .roomCode(roomCode)
            .name(name != null ? name : host.getUsername() + "的房间")
            .host(host)
            .isPrivate(isPrivate != null ? isPrivate : false)
            .password(password)
            .turnTimeLimit(turnTimeLimit != null ? turnTimeLimit : 120)
            .build();
        
        GameRoom savedRoom = gameRoomRepository.save(room);
        return dtoMapper.toGameRoomDTO(savedRoom);
    }
    
    @Transactional
    public GameRoomDTO joinRoom(Long roomId, Long playerId, String password) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (!room.isWaiting()) {
            throw new RuntimeException("Room is not available");
        }
        
        if (room.isFull()) {
            throw new RuntimeException("Room is full");
        }
        
        if (room.getIsPrivate() && (password == null || !password.equals(room.getPassword()))) {
            throw new RuntimeException("Invalid password");
        }
        
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        room.setGuest(player);
        GameRoom updatedRoom = gameRoomRepository.save(room);
        return dtoMapper.toGameRoomDTO(updatedRoom);
    }
    
    @Transactional
    public GameRoomDTO joinRoomByCode(String roomCode, Long playerId, String password) {
        GameRoom room = gameRoomRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        return joinRoom(room.getId(), playerId, password);
    }
    
    @Transactional
    public GameRoomDTO leaveRoom(Long roomId, Long playerId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (room.getHost().getId().equals(playerId)) {
            room.setStatus(GameRoomStatus.ABANDONED);
        } else if (room.getGuest() != null && room.getGuest().getId().equals(playerId)) {
            room.setGuest(null);
        }
        
        GameRoom updatedRoom = gameRoomRepository.save(room);
        return dtoMapper.toGameRoomDTO(updatedRoom);
    }
    
    @Transactional
    public GameRoomDTO startGame(Long roomId, Long hostId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (!room.getHost().getId().equals(hostId)) {
            throw new RuntimeException("Only host can start the game");
        }
        
        if (!room.isFull()) {
            throw new RuntimeException("Room is not full");
        }
        
        room.setStatus(GameRoomStatus.PLAYING);
        room.setStartedAt(LocalDateTime.now());
        
        GameRoom updatedRoom = gameRoomRepository.save(room);
        return dtoMapper.toGameRoomDTO(updatedRoom);
    }
    
    @Transactional
    public GameRoomDTO endGame(Long roomId, Long winnerId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        room.setStatus(GameRoomStatus.FINISHED);
        room.setEndedAt(LocalDateTime.now());
        
        if (winnerId != null) {
            Player winner = playerRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
            room.setWinner(winner);
        }
        
        GameRoom updatedRoom = gameRoomRepository.save(room);
        return dtoMapper.toGameRoomDTO(updatedRoom);
    }
    
    public GameRoomDTO getActiveGameByPlayer(Long playerId) {
        return gameRoomRepository.findActiveGameByPlayerId(playerId)
            .map(dtoMapper::toGameRoomDTO)
            .orElse(null);
    }
    
    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
