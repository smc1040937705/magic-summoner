package com.magicsummoner.controller;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.entity.Deck;
import com.magicsummoner.entity.GameRoom;
import com.magicsummoner.game.GameEngine;
import com.magicsummoner.game.model.GamePlayer;
import com.magicsummoner.game.model.GameState;
import com.magicsummoner.repository.DeckRepository;
import com.magicsummoner.repository.GameRoomRepository;
import com.magicsummoner.service.CardService;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    
    private final GameRoomRepository gameRoomRepository;
    private final DeckRepository deckRepository;
    private final CardService cardService;
    private final GameEngine gameEngine;
    private final DtoMapper dtoMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    private final Map<Long, GameState> activeGames = new ConcurrentHashMap<>();
    
    @PostMapping("/{roomId}/start")
    public Map<String, Object> startGame(@PathVariable Long roomId, @RequestAttribute Long userId) {
        GameRoom room = gameRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        if (room.getGuest() == null) {
            throw new RuntimeException("Need opponent to start game");
        }
        
        List<CardDTO> player1Deck = generateDeckFromCards(20);
        List<CardDTO> player2Deck = generateDeckFromCards(20);
        
        GamePlayer p1 = GamePlayer.create(
            room.getHost().getId(),
            room.getHost().getUsername(),
            player1Deck
        );
        
        GamePlayer p2 = GamePlayer.create(
            room.getGuest().getId(),
            room.getGuest().getUsername(),
            player2Deck
        );
        
        GameState gameState = GameState.create(roomId, p1, p2, room.getTurnTimeLimit());
        activeGames.put(roomId, gameState);
        
        room.setStatus(com.magicsummoner.enums.GameRoomStatus.PLAYING);
        gameRoomRepository.save(room);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Game started");
        response.put("state", gameState);
        
        messagingTemplate.convertAndSend("/topic/game/" + roomId, Map.of("type", "GAME_START", "state", gameState));
        
        return response;
    }
    
    @GetMapping("/{roomId}/state")
    public GameState getGameState(@PathVariable Long roomId) {
        GameState state = activeGames.get(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        return sanitizeGameState(state);
    }
    
    @PostMapping("/{roomId}/play")
    public Map<String, Object> playCard(
            @PathVariable Long roomId,
            @RequestParam Long cardId,
            @RequestAttribute Long userId) {
        
        GameState state = activeGames.get(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        
        gameEngine.playCard(state, userId, cardId, null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("state", sanitizeGameState(state));
        
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            Map.of("type", "STATE_UPDATE", "state", sanitizeGameState(state)));
        
        if (state.isGameOver()) {
            handleGameEnd(roomId, state);
        }
        
        return response;
    }
    
    @PostMapping("/{roomId}/attack")
    public Map<String, Object> attack(
            @PathVariable Long roomId,
            @RequestParam Long attackerId,
            @RequestParam(required = false) Long targetId,
            @RequestAttribute Long userId) {
        
        GameState state = activeGames.get(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        
        gameEngine.attack(state, userId, attackerId, targetId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("state", sanitizeGameState(state));
        
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            Map.of("type", "STATE_UPDATE", "state", sanitizeGameState(state)));
        
        if (state.isGameOver()) {
            handleGameEnd(roomId, state);
        }
        
        return response;
    }
    
    @PostMapping("/{roomId}/endTurn")
    public Map<String, Object> endTurn(@PathVariable Long roomId, @RequestAttribute Long userId) {
        GameState state = activeGames.get(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        
        gameEngine.endTurn(state, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("state", sanitizeGameState(state));
        
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            Map.of("type", "STATE_UPDATE", "state", sanitizeGameState(state)));
        
        return response;
    }
    
    @PostMapping("/{roomId}/surrender")
    public Map<String, Object> surrender(@PathVariable Long roomId, @RequestAttribute Long userId) {
        GameState state = activeGames.get(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        
        gameEngine.surrender(state, userId);
        handleGameEnd(roomId, state);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Game ended");
        response.put("winnerId", state.getWinnerId());
        
        return response;
    }
    
    private void handleGameEnd(Long roomId, GameState state) {
        GameRoom room = gameRoomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.setStatus(com.magicsummoner.enums.GameRoomStatus.FINISHED);
            gameRoomRepository.save(room);
        }
        
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            Map.of("type", "GAME_OVER", "winnerId", state.getWinnerId(), "result", state.getResult()));
        
        activeGames.remove(roomId);
    }
    
    private List<CardDTO> generateDeckFromCards(int count) {
        List<CardDTO> allCards = cardService.getAllCards();
        return allCards.stream()
            .limit(Math.min(count, allCards.size()))
            .toList();
    }
    
    private GameState sanitizeGameState(GameState state) {
        Long currentPlayerId = state.getCurrentPlayerId();
        
        GamePlayer p1 = state.getPlayer1();
        GamePlayer p2 = state.getPlayer2();
        
        if (p1.getPlayerId().equals(currentPlayerId)) {
            hideOpponentHand(p2);
        } else {
            hideOpponentHand(p1);
        }
        
        return state;
    }
    
    private void hideOpponentHand(GamePlayer player) {
        player.getHand().clear();
    }
}
