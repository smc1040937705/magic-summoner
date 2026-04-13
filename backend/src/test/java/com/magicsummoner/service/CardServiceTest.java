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
                .attack(6)
                .health(5)
                .maxHealth(5)
                .isActive(true)
                .build();

        testCardDTO = new CardDTO();
        testCardDTO.setId(1L);
        testCardDTO.setName("Fire Dragon");
        testCardDTO.setDescription("A powerful fire dragon");
        testCardDTO.setCardType(CardType.CREATURE);
        testCardDTO.setElementType(ElementType.FIRE);
        testCardDTO.setRarity(CardRarity.RARE);
        testCardDTO.setManaCost(5);
        testCardDTO.setAttack(6);
        testCardDTO.setHealth(5);
    }

    @Test
    @DisplayName("根据ID获取卡牌成功")
    void getCardById_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        CardDTO result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals("Fire Dragon", result.getName());
        verify(cardRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取卡牌失败 - 卡牌不存在")
    void getCardById_NotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.getCardById(999L);
        });

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有活跃卡牌")
    void getAllCards_Success() {
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByIsActiveTrue()).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        List<CardDTO> result = cardService.getAllCards();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("根据类型获取卡牌")
    void getCardsByType_Success() {
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByCardTypeAndIsActiveTrue(CardType.CREATURE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        List<CardDTO> result = cardService.getCardsByType(CardType.CREATURE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据元素类型获取卡牌")
    void getCardsByElement_Success() {
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByElementTypeAndIsActiveTrue(ElementType.FIRE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        List<CardDTO> result = cardService.getCardsByElement(ElementType.FIRE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("根据稀有度获取卡牌")
    void getCardsByRarity_Success() {
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findByRarityAndIsActiveTrue(CardRarity.RARE)).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        List<CardDTO> result = cardService.getCardsByRarity(CardRarity.RARE);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("创建卡牌成功")
    void createCard_Success() {
        CardDTO newCardDTO = new CardDTO();
        newCardDTO.setName("Ice Golem");
        newCardDTO.setCardType(CardType.CREATURE);
        newCardDTO.setElementType(ElementType.WATER);
        newCardDTO.setRarity(CardRarity.COMMON);
        newCardDTO.setManaCost(3);

        Card newCard = Card.builder()
                .id(2L)
                .name("Ice Golem")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.WATER)
                .rarity(CardRarity.COMMON)
                .manaCost(3)
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(newCard);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(newCardDTO);

        CardDTO result = cardService.createCard(newCardDTO);

        assertNotNull(result);
        assertEquals("Ice Golem", result.getName());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("更新卡牌成功")
    void updateCard_Success() {
        CardDTO updateDTO = new CardDTO();
        updateDTO.setName("Fire Dragon Updated");
        updateDTO.setAttack(7);

        Card updatedCard = Card.builder()
                .id(1L)
                .name("Fire Dragon Updated")
                .attack(7)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(updateDTO);

        CardDTO result = cardService.updateCard(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Fire Dragon Updated", result.getName());
    }

    @Test
    @DisplayName("更新卡牌失败 - 卡牌不存在")
    void updateCard_NotFound_ThrowsException() {
        CardDTO updateDTO = new CardDTO();
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.updateCard(999L, updateDTO);
        });

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("删除卡牌成功 - 软删除")
    void deleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        assertDoesNotThrow(() -> cardService.deleteCard(1L));

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("删除卡牌失败 - 卡牌不存在")
    void deleteCard_NotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cardService.deleteCard(999L);
        });

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("打开卡包成功")
    void openCardPack_Success() {
        Card card2 = Card.builder().id(2L).name("Card 2").build();
        List<Card> allCards = Arrays.asList(testCard, card2);

        when(cardRepository.findAllCollectibleCards()).thenReturn(allCards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        List<CardDTO> result = cardService.openCardPack(3);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("根据过滤器搜索卡牌")
    void getCardsByFilters_Success() {
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(testCard));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(cardPage);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        Page<CardDTO> result = cardService.getCardsByFilters(
                CardType.CREATURE, ElementType.FIRE, CardRarity.RARE, 1, 10, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("根据关键词搜索卡牌")
    void searchCards_Success() {
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(testCard));
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.searchByName("Dragon", pageable)).thenReturn(cardPage);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        Page<CardDTO> result = cardService.searchCards("Dragon", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("打开单张卡牌成功")
    void openSingleCard_Success() {
        List<Card> cards = Arrays.asList(testCard);

        when(cardRepository.findByRarityForPackOpening(any(CardRarity.class))).thenReturn(cards);
        when(dtoMapper.toCardDTO(any(Card.class))).thenReturn(testCardDTO);

        CardDTO result = cardService.openSingleCard();

        assertNotNull(result);
    }
}
