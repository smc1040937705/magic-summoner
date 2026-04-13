package com.magicsummoner.service;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.entity.Card;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.repository.CardRepository;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    private final DtoMapper dtoMapper;
    private final Random random = new Random();
    
    public CardDTO getCardById(Long id) {
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        return dtoMapper.toCardDTO(card);
    }
    
    public List<CardDTO> getAllCards() {
        return cardRepository.findByIsActiveTrue().stream()
            .map(dtoMapper::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public List<CardDTO> getCardsByType(CardType cardType) {
        return cardRepository.findByCardTypeAndIsActiveTrue(cardType).stream()
            .map(dtoMapper::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public List<CardDTO> getCardsByElement(ElementType elementType) {
        return cardRepository.findByElementTypeAndIsActiveTrue(elementType).stream()
            .map(dtoMapper::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public List<CardDTO> getCardsByRarity(CardRarity rarity) {
        return cardRepository.findByRarityAndIsActiveTrue(rarity).stream()
            .map(dtoMapper::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public Page<CardDTO> getCardsByFilters(CardType cardType, ElementType elementType, 
                                           CardRarity rarity, Integer minMana, Integer maxMana, 
                                           Pageable pageable) {
        return cardRepository.findByFilters(cardType, elementType, rarity, minMana, maxMana, pageable)
            .map(dtoMapper::toCardDTO);
    }
    
    public Page<CardDTO> searchCards(String keyword, Pageable pageable) {
        return cardRepository.searchByName(keyword, pageable)
            .map(dtoMapper::toCardDTO);
    }
    
    @Transactional
    public CardDTO createCard(CardDTO cardDTO) {
        Card card = Card.builder()
            .name(cardDTO.getName())
            .description(cardDTO.getDescription())
            .imageUrl(cardDTO.getImageUrl())
            .cardType(cardDTO.getCardType())
            .elementType(cardDTO.getElementType())
            .rarity(cardDTO.getRarity())
            .manaCost(cardDTO.getManaCost())
            .attack(cardDTO.getAttack())
            .health(cardDTO.getHealth())
            .maxHealth(cardDTO.getMaxHealth())
            .hasTaunt(cardDTO.getHasTaunt())
            .hasCharge(cardDTO.getHasCharge())
            .hasStealth(cardDTO.getHasStealth())
            .hasDivineShield(cardDTO.getHasDivineShield())
            .hasWindfury(cardDTO.getHasWindfury())
            .hasLifesteal(cardDTO.getHasLifesteal())
            .hasPoisonous(cardDTO.getHasPoisonous())
            .spellDamage(cardDTO.getSpellDamage())
            .healAmount(cardDTO.getHealAmount())
            .drawCards(cardDTO.getDrawCards())
            .summonCreatureId(cardDTO.getSummonCreatureId())
            .summonCount(cardDTO.getSummonCount())
            .attackBonus(cardDTO.getAttackBonus())
            .healthBonus(cardDTO.getHealthBonus())
            .equipmentSlot(cardDTO.getEquipmentSlot())
            .effectDescription(cardDTO.getEffectDescription())
            .goldCost(cardDTO.getGoldCost())
            .dustCost(cardDTO.getDustCost())
            .keywords(cardDTO.getKeywords())
            .build();
        
        Card savedCard = cardRepository.save(card);
        return dtoMapper.toCardDTO(savedCard);
    }
    
    @Transactional
    public CardDTO updateCard(Long id, CardDTO cardDTO) {
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (cardDTO.getName() != null) card.setName(cardDTO.getName());
        if (cardDTO.getDescription() != null) card.setDescription(cardDTO.getDescription());
        if (cardDTO.getImageUrl() != null) card.setImageUrl(cardDTO.getImageUrl());
        if (cardDTO.getManaCost() != null) card.setManaCost(cardDTO.getManaCost());
        if (cardDTO.getAttack() != null) card.setAttack(cardDTO.getAttack());
        if (cardDTO.getHealth() != null) card.setHealth(cardDTO.getHealth());
        if (cardDTO.getGoldCost() != null) card.setGoldCost(cardDTO.getGoldCost());
        if (cardDTO.getDustCost() != null) card.setDustCost(cardDTO.getDustCost());
        
        Card updatedCard = cardRepository.save(card);
        return dtoMapper.toCardDTO(updatedCard);
    }
    
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setIsActive(false);
        cardRepository.save(card);
    }
    
    public List<CardDTO> openCardPack(int packSize) {
        List<Card> allCards = cardRepository.findAllCollectibleCards();
        return random.ints(packSize, 0, allCards.size())
            .mapToObj(allCards::get)
            .map(dtoMapper::toCardDTO)
            .collect(Collectors.toList());
    }
    
    public CardDTO openSingleCard() {
        double roll = random.nextDouble();
        CardRarity rarity;
        
        if (roll < CardRarity.MYTHIC.getDropRate()) {
            rarity = CardRarity.MYTHIC;
        } else if (roll < CardRarity.LEGENDARY.getDropRate() + CardRarity.MYTHIC.getDropRate()) {
            rarity = CardRarity.LEGENDARY;
        } else if (roll < CardRarity.EPIC.getDropRate() + CardRarity.LEGENDARY.getDropRate() + CardRarity.MYTHIC.getDropRate()) {
            rarity = CardRarity.EPIC;
        } else if (roll < CardRarity.RARE.getDropRate() + CardRarity.EPIC.getDropRate() + CardRarity.LEGENDARY.getDropRate() + CardRarity.MYTHIC.getDropRate()) {
            rarity = CardRarity.RARE;
        } else if (roll < CardRarity.UNCOMMON.getDropRate() + CardRarity.RARE.getDropRate() + CardRarity.EPIC.getDropRate() + CardRarity.LEGENDARY.getDropRate() + CardRarity.MYTHIC.getDropRate()) {
            rarity = CardRarity.UNCOMMON;
        } else {
            rarity = CardRarity.COMMON;
        }
        
        List<Card> cardsOfRarity = cardRepository.findByRarityForPackOpening(rarity);
        if (cardsOfRarity.isEmpty()) {
            cardsOfRarity = cardRepository.findAllCollectibleCards();
        }
        
        Card selectedCard = cardsOfRarity.get(random.nextInt(cardsOfRarity.size()));
        return dtoMapper.toCardDTO(selectedCard);
    }
}
