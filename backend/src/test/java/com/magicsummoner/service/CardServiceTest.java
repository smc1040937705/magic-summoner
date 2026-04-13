package com.magicsummoner.service;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.entity.Card;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.repository.CardRepository;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CardService cardService;

    @Test
    void getCardById_WithValidId_ShouldReturnCardDTO() {
        Card card = Card.builder()
                .id(1L)
                .name("Fireball")
                .cardType(CardType.SPELL)
                .rarity(CardRarity.RARE)
                .build();

        CardDTO cardDTO = CardDTO.builder()
                .id(1L)
                .name("Fireball")
                .cardType(CardType.SPELL)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(dtoMapper.toCardDTO(card)).thenReturn(cardDTO);

        CardDTO result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fireball", result.getName());
    }

    @Test
    void getCardById_WithInvalidId_ShouldThrowException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.getCardById(999L));

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void getAllCards_ShouldReturnActiveCardsList() {
        List<Card> cards = Arrays.asList(
                Card.builder().id(1L).name("Card 1").build(),
                Card.builder().id(2L).name("Card 2").build()
        );

        when(cardRepository.findByIsActiveTrue()).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> {
            Card c = i.getArgument(0);
            return CardDTO.builder().id(c.getId()).name(c.getName()).build();
        });

        List<CardDTO> result = cardService.getAllCards();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardRepository).findByIsActiveTrue();
    }

    @Test
    void getCardsByType_ShouldReturnCardsOfSpecifiedType() {
        List<Card> creatureCards = Arrays.asList(
                Card.builder().id(1L).name("Warrior").cardType(CardType.CREATURE).build(),
                Card.builder().id(2L).name("Mage").cardType(CardType.CREATURE).build()
        );

        when(cardRepository.findByCardTypeAndIsActiveTrue(CardType.CREATURE)).thenReturn(creatureCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> {
            Card c = i.getArgument(0);
            return CardDTO.builder().id(c.getId()).name(c.getName()).build();
        });

        List<CardDTO> result = cardService.getCardsByType(CardType.CREATURE);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardRepository).findByCardTypeAndIsActiveTrue(CardType.CREATURE);
    }

    @Test
    void getCardsByElement_ShouldReturnCardsOfSpecifiedElement() {
        List<Card> fireCards = Arrays.asList(
                Card.builder().id(1L).name("Fireball").elementType(ElementType.FIRE).build()
        );

        when(cardRepository.findByElementTypeAndIsActiveTrue(ElementType.FIRE)).thenReturn(fireCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> {
            Card c = i.getArgument(0);
            return CardDTO.builder().id(c.getId()).name(c.getName()).build();
        });

        List<CardDTO> result = cardService.getCardsByElement(ElementType.FIRE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findByElementTypeAndIsActiveTrue(ElementType.FIRE);
    }

    @Test
    void getCardsByRarity_ShouldReturnCardsOfSpecifiedRarity() {
        List<Card> legendaryCards = Arrays.asList(
                Card.builder().id(1L).name("Legendary Weapon").rarity(CardRarity.LEGENDARY).build()
        );

        when(cardRepository.findByRarityAndIsActiveTrue(CardRarity.LEGENDARY)).thenReturn(legendaryCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> {
            Card c = i.getArgument(0);
            return CardDTO.builder().id(c.getId()).build();
        });

        List<CardDTO> result = cardService.getCardsByRarity(CardRarity.LEGENDARY);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findByRarityAndIsActiveTrue(CardRarity.LEGENDARY);
    }

    @Test
    void getCardsByFilters_ShouldReturnFilteredCards() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Card> filteredCards = Arrays.asList(
                Card.builder().id(1L).name("Filtered Card").build()
        );
        Page<Card> cardPage = new PageImpl<>(filteredCards, pageable, 1);

        when(cardRepository.findByFilters(
                eq(CardType.CREATURE),
                eq(ElementType.FIRE),
                eq(CardRarity.RARE),
                eq(2),
                eq(5),
                eq(pageable)
        )).thenReturn(cardPage);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> CardDTO.builder().id(1L).build());

        Page<CardDTO> result = cardService.getCardsByFilters(
                CardType.CREATURE, ElementType.FIRE, CardRarity.RARE, 2, 5, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchCards_WithKeyword_ShouldReturnMatchingCards() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Card> matchedCards = Arrays.asList(
                Card.builder().id(1L).name("Fire Dragon").build()
        );
        Page<Card> cardPage = new PageImpl<>(matchedCards, pageable, 1);

        when(cardRepository.searchByName("fire", pageable)).thenReturn(cardPage);
        when(dtoMapper.toCardDTO(any(Card.class))).thenAnswer(i -> CardDTO.builder().id(1L).build());

        Page<CardDTO> result = cardService.searchCards("fire", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).searchByName("fire", pageable);
    }

    @Test
    void createCard_WithValidData_ShouldReturnCreatedCardDTO() {
        CardDTO inputDTO = CardDTO.builder()
                .name("New Card")
                .description("Test card")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.NEUTRAL)
                .rarity(CardRarity.COMMON)
                .manaCost(2)
                .build();

        Card savedCard = Card.builder()
                .id(1L)
                .name("New Card")
                .build();

        CardDTO outputDTO = CardDTO.builder()
                .id(1L)
                .name("New Card")
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(dtoMapper.toCardDTO(savedCard)).thenReturn(outputDTO);

        CardDTO result = cardService.createCard(inputDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Card", result.getName());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_WithValidId_ShouldReturnUpdatedCardDTO() {
        Long cardId = 1L;
        Card existingCard = Card.builder()
                .id(cardId)
                .name("Old Name")
                .description("Old description")
                .manaCost(3)
                .build();

        CardDTO updateDTO = CardDTO.builder()
                .name("New Name")
                .manaCost(4)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenReturn(existingCard);
        when(dtoMapper.toCardDTO(existingCard)).thenReturn(updateDTO);

        CardDTO result = cardService.updateCard(cardId, updateDTO);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(cardRepository).save(existingCard);
        assertEquals("New Name", existingCard.getName());
        assertEquals(4, existingCard.getManaCost());
    }

    @Test
    void updateCard_WithInvalidId_ShouldThrowException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.updateCard(999L, CardDTO.builder().build()));

        assertEquals("Card not found", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_WithValidId_ShouldSetCardInactive() {
        Long cardId = 1L;
        Card card = Card.builder()
                .id(cardId)
                .isActive(true)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardId);

        assertFalse(card.getIsActive());
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_WithInvalidId_ShouldThrowException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.deleteCard(999L));

        assertEquals("Card not found", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void openCardPack_ShouldReturnSpecifiedNumberOfCards() {
        int packSize = 5;
        List<Card> allCards = Arrays.asList(
                Card.builder().id(1L).build(),
                Card.builder().id(2L).build(),
                Card.builder().id(3L).build(),
                Card.builder().id(4L).build(),
                Card.builder().id(5L).build(),
                Card.builder().id(6L).build(),
                Card.builder().id(7L).build(),
                Card.builder().id(8L).build(),
                Card.builder().id(9L).build(),
                Card.builder().id(10L).build()
        );

        when(cardRepository.findAllCollectibleCards()).thenReturn(allCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(CardDTO.builder().build());

        List<CardDTO> result = cardService.openCardPack(packSize);

        assertNotNull(result);
        assertEquals(packSize, result.size());
        verify(cardRepository).findAllCollectibleCards();
    }

    @Test
    void openSingleCard_ShouldReturnCardBasedOnRarity() {
        List<Card> cards = Arrays.asList(
                Card.builder().id(1L).rarity(CardRarity.COMMON).build()
        );

        when(cardRepository.findByRarityForPackOpening(any())).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(CardDTO.builder().build());

        CardDTO result = cardService.openSingleCard();

        assertNotNull(result);
    }

    @Test
    void openSingleCard_WithEmptyRarityFallback_ShouldUseAllCards() {
        when(cardRepository.findByRarityForPackOpening(any())).thenReturn(List.of());
        when(cardRepository.findAllCollectibleCards()).thenReturn(Arrays.asList(
                Card.builder().id(1L).build()
        ));
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(CardDTO.builder().build());

        CardDTO result = cardService.openSingleCard();

        assertNotNull(result);
        verify(cardRepository).findAllCollectibleCards();
    }
}
