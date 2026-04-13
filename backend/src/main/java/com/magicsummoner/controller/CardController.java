package com.magicsummoner.controller;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    
    private final CardService cardService;
    
    @GetMapping
    public ResponseEntity<List<CardDTO>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }
    
    @GetMapping("/type/{cardType}")
    public ResponseEntity<List<CardDTO>> getCardsByType(@PathVariable CardType cardType) {
        return ResponseEntity.ok(cardService.getCardsByType(cardType));
    }
    
    @GetMapping("/element/{elementType}")
    public ResponseEntity<List<CardDTO>> getCardsByElement(@PathVariable ElementType elementType) {
        return ResponseEntity.ok(cardService.getCardsByElement(elementType));
    }
    
    @GetMapping("/rarity/{rarity}")
    public ResponseEntity<List<CardDTO>> getCardsByRarity(@PathVariable CardRarity rarity) {
        return ResponseEntity.ok(cardService.getCardsByRarity(rarity));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<CardDTO>> searchCards(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CardType cardType,
            @RequestParam(required = false) ElementType elementType,
            @RequestParam(required = false) CardRarity rarity,
            @RequestParam(required = false) Integer minMana,
            @RequestParam(required = false) Integer maxMana,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(cardService.searchCards(keyword, PageRequest.of(page, size)));
        }
        
        return ResponseEntity.ok(cardService.getCardsByFilters(
            cardType, elementType, rarity, minMana, maxMana, PageRequest.of(page, size)));
    }
    
    @PostMapping
    public ResponseEntity<CardDTO> createCard(@RequestBody CardDTO cardDTO) {
        return ResponseEntity.ok(cardService.createCard(cardDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable Long id, @RequestBody CardDTO cardDTO) {
        return ResponseEntity.ok(cardService.updateCard(id, cardDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/open-pack")
    public ResponseEntity<List<CardDTO>> openCardPack(@RequestParam(defaultValue = "5") int packSize) {
        return ResponseEntity.ok(cardService.openCardPack(packSize));
    }
    
    @PostMapping("/open-single")
    public ResponseEntity<CardDTO> openSingleCard() {
        return ResponseEntity.ok(cardService.openSingleCard());
    }
}
