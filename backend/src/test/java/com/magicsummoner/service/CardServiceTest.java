package com.magicsummoner.service;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.entity.Card;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.repository.CardRepository;
import com.magicsummoner.util.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private CardDTO testCardDTO;

    @BeforeEach
    void setUp() {
        testCard = Card.builder()
                .id(1L)
                .name("Fire Dragon")
                .description("A powerful fire dragon")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.RARE)
                .manaCost(5)
                .attack(7)
                .health(6)
                .isActive(true)
                .build();

        testCardDTO = CardDTO.builder()
                .id(1L)
                .name("Fire Dragon")
                .description("A powerful fire dragon")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.RARE)
                .manaCost(5)
                .attack(7)
                .health(6)
                .build();
    }

    @Test
    @DisplayName("根据ID获取卡牌 - 正常场景")
    void getCardById_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        CardDTO result = cardService.getCardById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Fire Dragon", result.getName());
        assertEquals(CardType.CREATURE, result.getCardType());
        verify(cardRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取卡牌 - 卡牌不存在")
    void getCardById_NotFound_ThrowsException() {
        // Given
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.getCardById(999L));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有卡牌 - 正常场景")
    void getAllCards_Success() {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByIsActiveTrue()).thenReturn(cards);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        List<CardDTO> result = cardService.getAllCards();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Fire Dragon", result.get(0).getName());
    }

    @Test
    @DisplayName("获取所有卡牌 - 空列表")
    void getAllCards_EmptyList() {
        // Given
        when(cardRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // When
        List<CardDTO> result = cardService.getAllCards();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("根据类型获取卡牌 - 正常场景")
    void getCardsByType_Success() {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByCardTypeAndIsActiveTrue(CardType.CREATURE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        List<CardDTO> result = cardService.getCardsByType(CardType.CREATURE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CardType.CREATURE, result.get(0).getCardType());
    }

    @Test
    @DisplayName("根据元素类型获取卡牌 - 正常场景")
    void getCardsByElement_Success() {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByElementTypeAndIsActiveTrue(ElementType.FIRE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        List<CardDTO> result = cardService.getCardsByElement(ElementType.FIRE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ElementType.FIRE, result.get(0).getElementType());
    }

    @Test
    @DisplayName("根据稀有度获取卡牌 - 正常场景")
    void getCardsByRarity_Success() {
        // Given
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByRarityAndIsActiveTrue(CardRarity.RARE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        List<CardDTO> result = cardService.getCardsByRarity(CardRarity.RARE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CardRarity.RARE, result.get(0).getRarity());
    }

    @Test
    @DisplayName("搜索卡牌 - 正常场景")
    void searchCards_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(testCard));
        when(cardRepository.searchByName("dragon", pageable)).thenReturn(cardPage);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        Page<CardDTO> result = cardService.searchCards("dragon", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Fire Dragon", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("创建卡牌 - 正常场景")
    void createCard_Success() {
        // Given
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        CardDTO result = cardService.createCard(testCardDTO);

        // Then
        assertNotNull(result);
        assertEquals("Fire Dragon", result.getName());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("更新卡牌 - 正常场景")
    void updateCard_Success() {
        // Given
        CardDTO updateDTO = CardDTO.builder()
                .name("Updated Dragon")
                .manaCost(6)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(dtoMapper.toCardDTO(testCard)).thenReturn(testCardDTO);

        // When
        CardDTO result = cardService.updateCard(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("删除卡牌 - 正常场景")
    void deleteCard_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        assertDoesNotThrow(() -> cardService.deleteCard(1L));

        // Then
        verify(cardRepository).save(any(Card.class));
        assertFalse(testCard.getIsActive());
    }

    @Test
    @DisplayName("删除卡牌 - 卡牌不存在")
    void deleteCard_NotFound_ThrowsException() {
        // Given
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.deleteCard(999L));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("开卡包 - 正常场景")
    void openCardPack_Success() {
        // Given
        Card card1 = Card.builder().id(1L).name("Card 1").build();
        Card card2 = Card.builder().id(2L).name("Card 2").build();
        List<Card> allCards = Arrays.asList(card1, card2);

        when(cardRepository.findAllCollectibleCards()).thenReturn(allCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(
                CardDTO.builder().id(1L).name("Card 1").build(),
                CardDTO.builder().id(2L).name("Card 2").build()
        );

        // When
        List<CardDTO> result = cardService.openCardPack(5);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
    }
}
