package com.magicsummoner.controller;

import com.magicsummoner.dto.GameRoomDTO;
import com.magicsummoner.enums.GameRoomStatus;
import com.magicsummoner.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomController {
    
    private final GameRoomService gameRoomService;
    
    @GetMapping
    public ResponseEntity<List<GameRoomDTO>> getAvailableRooms() {
        return ResponseEntity.ok(gameRoomService.getAvailableRooms());
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoomDTO> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(gameRoomService.getRoomById(roomId));
    }
    
    @GetMapping("/code/{roomCode}")
    public ResponseEntity<GameRoomDTO> getRoomByCode(@PathVariable String roomCode) {
        return ResponseEntity.ok(gameRoomService.getRoomByCode(roomCode));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<GameRoomDTO>> getRoomsByStatus(@PathVariable GameRoomStatus status) {
        return ResponseEntity.ok(gameRoomService.getRoomsByStatus(status));
    }
    
    @GetMapping("/active/{playerId}")
    public ResponseEntity<GameRoomDTO> getActiveGame(@PathVariable Long playerId) {
        return ResponseEntity.ok(gameRoomService.getActiveGameByPlayer(playerId));
    }
    
    @PostMapping
    public ResponseEntity<GameRoomDTO> createRoom(
            @RequestAttribute Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isPrivate,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Integer turnTimeLimit) {
        return ResponseEntity.ok(gameRoomService.createRoom(userId, name, isPrivate, password, turnTimeLimit));
    }
    
    @PostMapping("/{roomId}/join")
    public ResponseEntity<GameRoomDTO> joinRoom(
            @PathVariable Long roomId,
            @RequestAttribute Long userId,
            @RequestParam(required = false) String password) {
        return ResponseEntity.ok(gameRoomService.joinRoom(roomId, userId, password));
    }
    
    @PostMapping("/join/{roomCode}")
    public ResponseEntity<GameRoomDTO> joinRoomByCode(
            @PathVariable String roomCode,
            @RequestAttribute Long userId,
            @RequestParam(required = false) String password) {
        return ResponseEntity.ok(gameRoomService.joinRoomByCode(roomCode, userId, password));
    }
    
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<GameRoomDTO> leaveRoom(
            @PathVariable Long roomId,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(gameRoomService.leaveRoom(roomId, userId));
    }
    
    @PostMapping("/{roomId}/start")
    public ResponseEntity<GameRoomDTO> startGame(
            @PathVariable Long roomId,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(gameRoomService.startGame(roomId, userId));
    }
    
    @PostMapping("/{roomId}/end")
    public ResponseEntity<GameRoomDTO> endGame(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long winnerId) {
        return ResponseEntity.ok(gameRoomService.endGame(roomId, winnerId));
    }
}
