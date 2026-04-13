package com.magicsummoner.service;

import com.magicsummoner.dto.DeckDTO;
import com.magicsummoner.entity.Card;
import com.magicsummoner.entity.Deck;
import com.magicsummoner.entity.Player;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.enums.CardRarity;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .name("Fire Dragon")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.RARE)
                .manaCost(5)
                .build();

        testDeck = Deck.builder()
                .id(1L)
                .player(testPlayer)
                .name("Test Deck")
                .description("A test deck")
                .isActive(true)
                .build();

        testDeckDTO = new DeckDTO();
        testDeckDTO.setId(1L);
        testDeckDTO.setName("Test Deck");
        testDeckDTO.setDescription("A test deck");
    }

    @Test
    @DisplayName("根据ID获取卡组成功")
    void getDeckById_Success() {
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        DeckDTO result = deckService.getDeckById(1L);

        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
        verify(deckRepository).findByIdWithCards(1L);
    }

    @Test
    @DisplayName("根据ID获取卡组失败 - 卡组不存在")
    void getDeckById_NotFound_ThrowsException() {
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.getDeckById(999L);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据ID和玩家ID获取卡组成功")
    void getDeckByIdAndPlayerId_Success() {
        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        DeckDTO result = deckService.getDeckByIdAndPlayerId(1L, 1L);

        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
    }

    @Test
    @DisplayName("根据ID和玩家ID获取卡组失败 - 卡组不存在")
    void getDeckByIdAndPlayerId_NotFound_ThrowsException() {
        when(deckRepository.findByIdAndPlayerId(1L, 999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.getDeckByIdAndPlayerId(1L, 999L);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("根据玩家ID获取卡组列表")
    void getDecksByPlayerId_Success() {
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findByPlayerIdAndIsActiveTrue(1L)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        List<DeckDTO> result = deckService.getDecksByPlayerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("创建卡组成功")
    void createDeck_Success() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(testPlayer));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        DeckDTO result = deckService.createDeck(1L, "New Deck", "Description");

        assertNotNull(result);
        assertEquals("Test Deck", result.getName());
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("创建卡组失败 - 玩家不存在")
    void createDeck_PlayerNotFound_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.createDeck(999L, "New Deck", "Description");
        });

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    @DisplayName("更新卡组成功")
    void updateDeck_Success() {
        DeckDTO updateDTO = new DeckDTO();
        updateDTO.setName("Updated Deck");
        updateDTO.setDescription("Updated description");

        Deck updatedDeck = Deck.builder()
                .id(1L)
                .player(testPlayer)
                .name("Updated Deck")
                .description("Updated description")
                .build();

        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(updatedDeck);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(updateDTO);

        DeckDTO result = deckService.updateDeck(1L, 1L, "Updated Deck", "Updated description");

        assertNotNull(result);
        assertEquals("Updated Deck", result.getName());
    }

    @Test
    @DisplayName("更新卡组失败 - 卡组不存在")
    void updateDeck_NotFound_ThrowsException() {
        when(deckRepository.findByIdAndPlayerId(1L, 999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.updateDeck(1L, 999L, "Updated Deck", "Description");
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("添加卡牌到卡组成功")
    void addCardToDeck_Success() {
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        DeckDTO result = deckService.addCardToDeck(1L, 1L, 1L, 2);

        assertNotNull(result);
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("添加卡牌到卡组失败 - 卡组不存在")
    void addCardToDeck_DeckNotFound_ThrowsException() {
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.addCardToDeck(999L, 1L, 1L, 2);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("添加卡牌到卡组失败 - 无权限")
    void addCardToDeck_NotAuthorized_ThrowsException() {
        Player otherPlayer = Player.builder().id(2L).build();
        Deck otherDeck = Deck.builder().id(1L).player(otherPlayer).build();

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(otherDeck));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.addCardToDeck(1L, 1L, 1L, 2);
        });

        assertEquals("Not authorized to modify this deck", exception.getMessage());
    }

    @Test
    @DisplayName("添加卡牌到卡组失败 - 卡牌不存在")
    void addCardToDeck_CardNotFound_ThrowsException() {
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.addCardToDeck(1L, 1L, 999L, 2);
        });

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @DisplayName("从卡组移除卡牌成功")
    void removeCardFromDeck_Success() {
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        DeckDTO result = deckService.removeCardFromDeck(1L, 1L, 1L);

        assertNotNull(result);
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("从卡组移除卡牌失败 - 卡组不存在")
    void removeCardFromDeck_NotFound_ThrowsException() {
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.removeCardFromDeck(999L, 1L, 1L);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("从卡组移除卡牌失败 - 无权限")
    void removeCardFromDeck_NotAuthorized_ThrowsException() {
        Player otherPlayer = Player.builder().id(2L).build();
        Deck otherDeck = Deck.builder().id(1L).player(otherPlayer).build();

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(otherDeck));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.removeCardFromDeck(1L, 1L, 1L);
        });

        assertEquals("Not authorized to modify this deck", exception.getMessage());
    }

    @Test
    @DisplayName("删除卡组成功 - 软删除")
    void deleteDeck_Success() {
        when(deckRepository.findByIdAndPlayerId(1L, 1L)).thenReturn(Optional.of(testDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(testDeck);

        assertDoesNotThrow(() -> deckService.deleteDeck(1L, 1L));

        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("删除卡组失败 - 卡组不存在")
    void deleteDeck_NotFound_ThrowsException() {
        when(deckRepository.findByIdAndPlayerId(1L, 999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.deleteDeck(1L, 999L);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("验证卡组成功")
    void validateDeck_Success() {
        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(testDeck));

        boolean result = deckService.validateDeck(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("验证卡组失败 - 卡组不存在")
    void validateDeck_NotFound_ThrowsException() {
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deckService.validateDeck(999L);
        });

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @DisplayName("获取玩家最佳卡组")
    void getBestDecksByPlayer_Success() {
        List<Deck> decks = Arrays.asList(testDeck);
        when(deckRepository.findBestDecksByPlayerId(1L)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenReturn(testDeckDTO);

        List<DeckDTO> result = deckService.getBestDecksByPlayer(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
