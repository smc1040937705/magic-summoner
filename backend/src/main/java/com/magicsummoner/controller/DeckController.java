package com.magicsummoner.controller;

import com.magicsummoner.dto.DeckDTO;
import com.magicsummoner.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    
    private final DeckService deckService;
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<DeckDTO>> getDecksByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(deckService.getDecksByPlayerId(playerId));
    }
    
    @GetMapping("/{deckId}")
    public ResponseEntity<DeckDTO> getDeckById(@PathVariable Long deckId) {
        return ResponseEntity.ok(deckService.getDeckById(deckId));
    }
    
    @PostMapping
    public ResponseEntity<DeckDTO> createDeck(
            @RequestAttribute Long userId,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(deckService.createDeck(userId, name, description));
    }
    
    @PutMapping("/{deckId}")
    public ResponseEntity<DeckDTO> updateDeck(
            @PathVariable Long deckId,
            @RequestAttribute Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(deckService.updateDeck(deckId, userId, name, description));
    }
    
    @PostMapping("/{deckId}/cards")
    public ResponseEntity<DeckDTO> addCardToDeck(
            @PathVariable Long deckId,
            @RequestAttribute Long userId,
            @RequestParam Long cardId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(deckService.addCardToDeck(deckId, userId, cardId, quantity));
    }
    
    @DeleteMapping("/{deckId}/cards/{cardId}")
    public ResponseEntity<DeckDTO> removeCardFromDeck(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(deckService.removeCardFromDeck(deckId, userId, cardId));
    }
    
    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(
            @PathVariable Long deckId,
            @RequestAttribute Long userId) {
        deckService.deleteDeck(deckId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{deckId}/validate")
    public ResponseEntity<Map<String, Boolean>> validateDeck(@PathVariable Long deckId) {
        boolean isValid = deckService.validateDeck(deckId);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    @GetMapping("/player/{playerId}/best")
    public ResponseEntity<List<DeckDTO>> getBestDecks(@PathVariable Long playerId) {
        return ResponseEntity.ok(deckService.getBestDecksByPlayer(playerId));
    }
}
