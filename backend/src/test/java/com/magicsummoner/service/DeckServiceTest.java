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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private DeckService deckService;

    private Player testPlayer;
    private Deck testDeck;
    private DeckDTO testDeckDTO;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testPlayer = Player.builder()
                .id(1L)
                .username("testuser")
                .build();

        testCard = Card.builder()
                .id(1L)
                .name("Test Card")
                .build();

        testDeck = Deck.builder()
                .id(1L)
                .player(testPlayer)
                .name("Test Deck")
                .description("Test Description")
                .isActive(true)
                .deckCards(new ArrayList<>())
                .build();

        testDeckDTO = DeckDTO.builder()
                .id(1L)
                .name("Test Deck")
                .description("Test Description")
                .build();
    }

    @Test
    @DisplayName("根据ID获取卡组 - 正常场景")
    void getDeckById_Success() {
        // Given
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.getDeckById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
        verify(deckRepository).findByIdWithCards(1L);
    }

    @Test
    @DisplayName("根据ID获取卡组 - 卡组不存在")
    void getDeckById_NotFound_ThrowsException() {
        // Given
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.getDeckById(999L));
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据ID和玩家ID获取卡组 - 正常场景")
    void getDeckByIdAndPlayerId_Success() {
        // Given
        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.getDeckByIdAndPlayerId(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
    }

    @Test
    @DisplayName("获取玩家的所有卡组 - 正常场景")
    void getDecksByPlayerId_Success() {
        // Given
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findByPlayerIdAndIsActiveTrue(1L)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        List<DeckDTO> result = deckService.getDecksByPlayerId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("创建卡组 - 正常场景")
    void createDeck_Success() {
        // Given
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.createDeck(1L, "New Deck", "Description");

        // Then
        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("创建卡组 - 玩家不存在")
    void createDeck_PlayerNotFound_ThrowsException() {
        // Given
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.createDeck(999L, "New Deck", "Description"));
        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("更新卡组 - 正常场景")
    void updateDeck_Success() {
        // Given
        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.updateDeck(1L, 1L, "Updated Name", "Updated Description");

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", testDeck.getName());
        assertEquals("Updated Description", testDeck.getDescription());
        verify(deckRepository).save(testDeck);
    }

    @Test
    @DisplayName("更新卡组 - 卡组不存在")
    void updateDeck_NotFound_ThrowsException() {
        // Given
        when(deckRepository.findByIdAndPlayerId(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.updateDeck(999L, 1L, "Name", "Description"));
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("添加卡牌到卡组 - 正常场景")
    void addCardToDeck_Success() {
        // Given
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.addCardToDeck(1L, 1L, 1L, 2);

        // Then
        assertNotNull(result);
        verify(deckRepository).save(testDeck);
    }

    @Test
    @DisplayName("添加卡牌到卡组 - 无权修改")
    void addCardToDeck_NotAuthorized_ThrowsException() {
        // Given
        Player otherPlayer = Player.builder().id(2L).build();
        testDeck.setPlayer(otherPlayer);

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.addCardToDeck(1L, 1L, 1L, 1));
        assertEquals("Not authorized to modify this deck", exception.getMessage());
    }

    @Test
    @DisplayName("从卡组移除卡牌 - 正常场景")
    void removeCardFromDeck_Success() {
        // Given
        DeckCard deckCard = DeckCard.builder()
                .deck(testDeck)
                .card(testCard)
                .quantity(1)
                .build();
        testDeck.getDeckCards().add(deckCard);

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        DeckDTO result = deckService.removeCardFromDeck(1L, 1L, 1L);

        // Then
        assertNotNull(result);
        assertTrue(testDeck.getDeckCards().isEmpty());
    }

    @Test
    @DisplayName("删除卡组 - 正常场景")
    void deleteDeck_Success() {
        // Given
        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

        // When
        assertDoesNotThrow(() -> deckService.deleteDeck(1L, 1L));

        // Then
        assertFalse(testDeck.getIsActive());
        verify(deckRepository).save(testDeck);
    }

    @Test
    @DisplayName("验证卡组 - 有效卡组")
    void validateDeck_Valid() {
        // Given
        for (int i = 0; i < 30; i++) {
            Card card = Card.builder().id((long) i).build();
            DeckCard deckCard = DeckCard.builder()
                    .deck(testDeck)
                    .card(card)
                    .quantity(1)
                    .build();
            testDeck.getDeckCards().add(deckCard);
        }

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));

        // When
        boolean result = deckService.validateDeck(1L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证卡组 - 无效卡组（卡牌不足）")
    void validateDeck_Invalid() {
        // Given
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));

        // When
        boolean result = deckService.validateDeck(1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("获取玩家最佳卡组 - 正常场景")
    void getBestDecksByPlayer_Success() {
        // Given
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findBestDecksByPlayerId(1L)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(testDeck)).thenReturn(testDeckDTO);

        // When
        List<DeckDTO> result = deckService.getBestDecksByPlayer(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
