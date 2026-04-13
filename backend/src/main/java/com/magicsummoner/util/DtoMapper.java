package com.magicsummoner.util;

import com.magicsummoner.dto.*;
import com.magicsummoner.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {
    
    public PlayerDTO toPlayerDTO(Player player) {
        if (player == null) return null;
        
        return PlayerDTO.builder()
            .id(player.getId())
            .username(player.getUsername())
            .displayName(player.getDisplayName())
            .avatarUrl(player.getAvatarUrl())
            .level(player.getLevel())
            .experience(player.getExperience())
            .gold(player.getGold())
            .gems(player.getGems())
            .rankPoints(player.getRankPoints())
            .wins(player.getWins())
            .losses(player.getLosses())
            .draws(player.getDraws())
            .winRate(player.getWinRate())
            .isOnline(player.getIsOnline())
            .lastLogin(player.getLastLogin())
            .createdAt(player.getCreatedAt())
            .build();
    }
    
    public CardDTO toCardDTO(Card card) {
        if (card == null) return null;
        
        return CardDTO.builder()
            .id(card.getId())
            .name(card.getName())
            .description(card.getDescription())
            .imageUrl(card.getImageUrl())
            .cardType(card.getCardType())
            .elementType(card.getElementType())
            .rarity(card.getRarity())
            .manaCost(card.getManaCost())
            .attack(card.getAttack())
            .health(card.getHealth())
            .maxHealth(card.getMaxHealth())
            .hasTaunt(card.getHasTaunt())
            .hasCharge(card.getHasCharge())
            .hasStealth(card.getHasStealth())
            .hasDivineShield(card.getHasDivineShield())
            .hasWindfury(card.getHasWindfury())
            .hasLifesteal(card.getHasLifesteal())
            .hasPoisonous(card.getHasPoisonous())
            .spellDamage(card.getSpellDamage())
            .healAmount(card.getHealAmount())
            .drawCards(card.getDrawCards())
            .summonCreatureId(card.getSummonCreatureId())
            .summonCount(card.getSummonCount())
            .attackBonus(card.getAttackBonus())
            .healthBonus(card.getHealthBonus())
            .equipmentSlot(card.getEquipmentSlot())
            .effectDescription(card.getEffectDescription())
            .goldCost(card.getGoldCost())
            .dustCost(card.getDustCost())
            .keywords(card.getKeywords())
            .build();
    }
    
    public DeckCardDTO toDeckCardDTO(DeckCard deckCard) {
        if (deckCard == null) return null;
        
        return DeckCardDTO.builder()
            .id(deckCard.getId())
            .card(toCardDTO(deckCard.getCard()))
            .quantity(deckCard.getQuantity())
            .build();
    }
    
    public DeckDTO toDeckDTO(Deck deck) {
        if (deck == null) return null;
        
        return DeckDTO.builder()
            .id(deck.getId())
            .name(deck.getName())
            .description(deck.getDescription())
            .cardCount(deck.getCardCount())
            .wins(deck.getWins())
            .losses(deck.getLosses())
            .winRate(deck.getWins() + deck.getLosses() > 0 ? 
                (double) deck.getWins() / (deck.getWins() + deck.getLosses()) * 100 : 0.0)
            .isActive(deck.getIsActive())
            .createdAt(deck.getCreatedAt())
            .updatedAt(deck.getUpdatedAt())
            .cards(deck.getDeckCards().stream()
                .map(this::toDeckCardDTO)
                .collect(Collectors.toList()))
            .build();
    }
    
    public GameRoomDTO toGameRoomDTO(GameRoom room) {
        if (room == null) return null;
        
        return GameRoomDTO.builder()
            .id(room.getId())
            .roomCode(room.getRoomCode())
            .name(room.getName())
            .host(toPlayerDTO(room.getHost()))
            .guest(toPlayerDTO(room.getGuest()))
            .status(room.getStatus())
            .maxSpectators(room.getMaxSpectators())
            .currentSpectators(room.getCurrentSpectators())
            .isPrivate(room.getIsPrivate())
            .turnTimeLimit(room.getTurnTimeLimit())
            .createdAt(room.getCreatedAt())
            .startedAt(room.getStartedAt())
            .isFull(room.isFull())
            .build();
    }
    
    public BattleRecordDTO toBattleRecordDTO(BattleRecord record) {
        if (record == null) return null;
        
        return BattleRecordDTO.builder()
            .id(record.getId())
            .player1(toPlayerDTO(record.getPlayer1()))
            .player2(toPlayerDTO(record.getPlayer2()))
            .winner(toPlayerDTO(record.getWinner()))
            .result(record.getResult())
            .player1Health(record.getPlayer1Health())
            .player2Health(record.getPlayer2Health())
            .turnsCount(record.getTurnsCount())
            .durationSeconds(record.getDurationSeconds())
            .player1RankChange(record.getPlayer1RankChange())
            .player2RankChange(record.getPlayer2RankChange())
            .player1ExpGained(record.getPlayer1ExpGained())
            .player2ExpGained(record.getPlayer2ExpGained())
            .player1GoldGained(record.getPlayer1GoldGained())
            .player2GoldGained(record.getPlayer2GoldGained())
            .playedAt(record.getPlayedAt())
            .build();
    }
    
    public List<CardDTO> toCardDTOList(List<Card> cards) {
        return cards.stream()
            .map(this::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public List<DeckDTO> toDeckDTOList(List<Deck> decks) {
        return decks.stream()
            .map(this::toDeckDTO)
            .collect(Collectors.toList());
    }
}
