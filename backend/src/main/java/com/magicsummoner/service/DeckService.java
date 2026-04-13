package com.magicsummoner.service;

import com.magicsummoner.dto.DeckDTO;
import com.magicsummoner.entity.Card;
import com.magicsummoner.entity.Deck;
import com.magicsummoner.entity.DeckCard;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.CardRepository;
import com.magicsummoner.repository.DeckRepository;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {
    
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final PlayerRepository playerRepository;
    private final DtoMapper dtoMapper;
    
    public DeckDTO getDeckById(Long deckId) {
        Deck deck = deckRepository.findByIdWithCards(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        return dtoMapper.toDeckDTO(deck);
    }
    
    public DeckDTO getDeckByIdAndPlayerId(Long deckId, Long playerId) {
        Deck deck = deckRepository.findByIdAndPlayerId(deckId, playerId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        return dtoMapper.toDeckDTO(deck);
    }
    
    public List<DeckDTO> getDecksByPlayerId(Long playerId) {
        return deckRepository.findByPlayerIdAndIsActiveTrue(playerId).stream()
            .map(dtoMapper::toDeckDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DeckDTO createDeck(Long playerId, String name, String description) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        Deck deck = Deck.builder()
            .player(player)
            .name(name)
            .description(description)
            .build();
        
        Deck savedDeck = deckRepository.save(deck);
        return dtoMapper.toDeckDTO(savedDeck);
    }
    
    @Transactional
    public DeckDTO updateDeck(Long deckId, Long playerId, String name, String description) {
        Deck deck = deckRepository.findByIdAndPlayerId(deckId, playerId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        
        if (name != null) deck.setName(name);
        if (description != null) deck.setDescription(description);
        
        Deck updatedDeck = deckRepository.save(deck);
        return dtoMapper.toDeckDTO(updatedDeck);
    }
    
    @Transactional
    public DeckDTO addCardToDeck(Long deckId, Long playerId, Long cardId, Integer quantity) {
        Deck deck = deckRepository.findByIdWithCards(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        
        if (!deck.getPlayer().getId().equals(playerId)) {
            throw new RuntimeException("Not authorized to modify this deck");
        }
        
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        deck.addCard(card, quantity);
        Deck updatedDeck = deckRepository.save(deck);
        return dtoMapper.toDeckDTO(updatedDeck);
    }
    
    @Transactional
    public DeckDTO removeCardFromDeck(Long deckId, Long playerId, Long cardId) {
        Deck deck = deckRepository.findByIdWithCards(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        
        if (!deck.getPlayer().getId().equals(playerId)) {
            throw new RuntimeException("Not authorized to modify this deck");
        }
        
        deck.removeCard(cardId);
        Deck updatedDeck = deckRepository.save(deck);
        return dtoMapper.toDeckDTO(updatedDeck);
    }
    
    @Transactional
    public void deleteDeck(Long deckId, Long playerId) {
        Deck deck = deckRepository.findByIdAndPlayerId(deckId, playerId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        
        deck.setIsActive(false);
        deckRepository.save(deck);
    }
    
    public boolean validateDeck(Long deckId) {
        Deck deck = deckRepository.findByIdWithCards(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));
        return deck.isValid();
    }
    
    public List<DeckDTO> getBestDecksByPlayer(Long playerId) {
        return deckRepository.findBestDecksByPlayerId(playerId).stream()
            .map(dtoMapper::toDeckDTO)
            .collect(Collectors.toList());
    }
}
