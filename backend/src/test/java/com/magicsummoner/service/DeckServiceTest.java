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

    @Test
    void getDeckById_WithValidId_ShouldReturnDeckDTO() {
        Deck deck = Deck.builder()
                .id(1L)
                .name("Fire Deck")
                .build();

        DeckDTO deckDTO = DeckDTO.builder()
                .id(1L)
                .name("Fire Deck")
                .build();

        when(deckRepository.findByIdWithCards(1L)).thenReturn(Optional.of(deck));
        when(dtoMapper.toDeckDTO(deck)).thenReturn(deckDTO);

        DeckDTO result = deckService.getDeckById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fire Deck", result.getName());
    }

    @Test
    void getDeckById_WithInvalidId_ShouldThrowException() {
        when(deckRepository.findByIdWithCards(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> deckService.getDeckById(999L));

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    void getDeckByIdAndPlayerId_WithValidIds_ShouldReturnDeckDTO() {
        Long deckId = 1L;
        Long playerId = 1L;
        Deck deck = Deck.builder().id(deckId).name("My Deck").build();
        DeckDTO deckDTO = DeckDTO.builder().id(deckId).name("My Deck").build();

        when(deckRepository.findByIdAndPlayerId(deckId, playerId)).thenReturn(Optional.of(deck));
        when(dtoMapper.toDeckDTO(deck)).thenReturn(deckDTO);

        DeckDTO result = deckService.getDeckByIdAndPlayerId(deckId, playerId);

        assertNotNull(result);
        assertEquals(deckId, result.getId());
    }

    @Test
    void getDeckByIdAndPlayerId_WithInvalidIds_ShouldThrowException() {
        when(deckRepository.findByIdAndPlayerId(999L, 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.getDeckByIdAndPlayerId(999L, 1L));

        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    void getDecksByPlayerId_ShouldReturnPlayerDecks() {
        Long playerId = 1L;
        List<Deck> decks = Arrays.asList(
                Deck.builder().id(1L).name("Deck 1").build(),
                Deck.builder().id(2L).name("Deck 2").build()
        );

        when(deckRepository.findByPlayerIdAndIsActiveTrue(playerId)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenAnswer(i -> {
            Deck d = i.getArgument(0);
            return DeckDTO.builder().id(d.getId()).name(d.getName()).build();
        });

        List<DeckDTO> result = deckService.getDecksByPlayerId(playerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(deckRepository).findByPlayerIdAndIsActiveTrue(playerId);
    }

    @Test
    void createDeck_WithValidPlayer_ShouldReturnCreatedDeckDTO() {
        Long playerId = 1L;
        Player player = Player.builder().id(playerId).username("testuser").build();
        Deck savedDeck = Deck.builder().id(1L).name("New Deck").player(player).build();
        DeckDTO deckDTO = DeckDTO.builder().id(1L).name("New Deck").build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);
        when(dtoMapper.toDeckDTO(savedDeck)).thenReturn(deckDTO);

        DeckDTO result = deckService.createDeck(playerId, "New Deck", "My new deck");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Deck", result.getName());
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    void createDeck_WithInvalidPlayer_ShouldThrowException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.createDeck(999L, "Deck", "Desc"));

        assertEquals("Player not found", exception.getMessage());
        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void updateDeck_WithValidIds_ShouldReturnUpdatedDeck() {
        Long deckId = 1L;
        Long playerId = 1L;
        Deck deck = Deck.builder()
                .id(deckId)
                .player(Player.builder().id(playerId).build())
                .name("Old Name")
                .description("Old desc")
                .build();

        DeckDTO updatedDTO = DeckDTO.builder()
                .id(deckId)
                .name("New Name")
                .description("New desc")
                .build();

        when(deckRepository.findByIdAndPlayerId(deckId, playerId)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);
        when(dtoMapper.toDeckDTO(deck)).thenReturn(updatedDTO);

        DeckDTO result = deckService.updateDeck(deckId, playerId, "New Name", "New desc");

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New desc", result.getDescription());
        verify(deckRepository).save(deck);
    }

    @Test
    void updateDeck_WithPartialData_ShouldOnlyUpdateProvidedFields() {
        Long deckId = 1L;
        Long playerId = 1L;
        Deck deck = Deck.builder()
                .id(deckId)
                .player(Player.builder().id(playerId).build())
                .name("Original Name")
                .description("Original desc")
                .build();

        when(deckRepository.findByIdAndPlayerId(deckId, playerId)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);
        when(dtoMapper.toDeckDTO(deck)).thenReturn(DeckDTO.builder().name("New Name").description("Original desc").build());

        deckService.updateDeck(deckId, playerId, "New Name", null);

        assertEquals("New Name", deck.getName());
        assertEquals("Original desc", deck.getDescription());
    }

    @Test
    void addCardToDeck_WithValidData_ShouldAddCardToDeck() {
        Long deckId = 1L;
        Long playerId = 1L;
        Long cardId = 100L;

        Player player = Player.builder().id(playerId).build();
        Deck deck = Deck.builder()
                .id(deckId)
                .player(player)
                .deckCards(new java.util.ArrayList<>())
                .build();
        Card card = Card.builder().id(cardId).name("Fireball").build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);
        when(dtoMapper.toDeckDTO(deck)).thenReturn(DeckDTO.builder().build());

        deckService.addCardToDeck(deckId, playerId, cardId, 2);

        assertEquals(1, deck.getDeckCards().size());
        verify(deckRepository).save(deck);
    }

    @Test
    void addCardToDeck_WithWrongPlayer_ShouldThrowException() {
        Long deckId = 1L;
        Long deckOwnerId = 1L;
        Long wrongPlayerId = 2L;
        Long cardId = 100L;

        Deck deck = Deck.builder()
                .id(deckId)
                .player(Player.builder().id(deckOwnerId).build())
                .build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.addCardToDeck(deckId, wrongPlayerId, cardId, 1));

        assertEquals("Not authorized to modify this deck", exception.getMessage());
        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    void addCardToDeck_WithInvalidCard_ShouldThrowException() {
        Long deckId = 1L;
        Long playerId = 1L;
        Long invalidCardId = 999L;

        Deck deck = Deck.builder()
                .id(deckId)
                .player(Player.builder().id(playerId).build())
                .build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));
        when(cardRepository.findById(invalidCardId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> deckService.addCardToDeck(deckId, playerId, invalidCardId, 1));

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void removeCardFromDeck_WithValidData_ShouldRemoveCard() {
        Long deckId = 1L;
        Long playerId = 1L;
        Long cardId = 100L;

        Player player = Player.builder().id(playerId).build();
        Card card = Card.builder().id(cardId).build();
        List<DeckCard> deckCards = new java.util.ArrayList<>();
        deckCards.add(DeckCard.builder().card(card).quantity(2).build());

        Deck deck = Deck.builder()
                .id(deckId)
                .player(player)
                .deckCards(deckCards)
                .build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);
        when(dtoMapper.toDeckDTO(deck)).thenReturn(DeckDTO.builder().build());

        deckService.removeCardFromDeck(deckId, playerId, cardId);

        assertEquals(0, deck.getDeckCards().size());
        verify(deckRepository).save(deck);
    }

    @Test
    void deleteDeck_WithValidIds_ShouldSetDeckInactive() {
        Long deckId = 1L;
        Long playerId = 1L;
        Deck deck = Deck.builder()
                .id(deckId)
                .player(Player.builder().id(playerId).build())
                .isActive(true)
                .build();

        when(deckRepository.findByIdAndPlayerId(deckId, playerId)).thenReturn(Optional.of(deck));

        deckService.deleteDeck(deckId, playerId);

        assertFalse(deck.getIsActive());
        verify(deckRepository).save(deck);
    }

    @Test
    void validateDeck_WithValidCardCount_ShouldReturnTrue() {
        Long deckId = 1L;
        List<DeckCard> deckCards = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            deckCards.add(DeckCard.builder().card(Card.builder().id((long) i).build()).quantity(2).build());
        }
        Deck deck = Deck.builder().id(deckId).deckCards(deckCards).build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));

        boolean result = deckService.validateDeck(deckId);

        assertTrue(result);
    }

    @Test
    void validateDeck_WithInvalidCardCount_ShouldReturnFalse() {
        Long deckId = 1L;
        List<DeckCard> deckCards = new java.util.ArrayList<>();
        deckCards.add(DeckCard.builder().card(Card.builder().id(1L).build()).quantity(2).build());
        Deck deck = Deck.builder().id(deckId).deckCards(deckCards).build();

        when(deckRepository.findByIdWithCards(deckId)).thenReturn(Optional.of(deck));

        boolean result = deckService.validateDeck(deckId);

        assertFalse(result);
    }

    @Test
    void getBestDecksByPlayer_ShouldReturnSortedDecks() {
        Long playerId = 1L;
        List<Deck> decks = Arrays.asList(
                Deck.builder().id(1L).wins(10).losses(5).build(),
                Deck.builder().id(2L).wins(20).losses(5).build()
        );

        when(deckRepository.findBestDecksByPlayerId(playerId)).thenReturn(decks);
        when(dtoMapper.toDeckDTO(any(Deck.class))).thenAnswer(i -> {
            Deck d = i.getArgument(0);
            return DeckDTO.builder().id(d.getId()).wins(d.getWins()).build();
        });

        List<DeckDTO> result = deckService.getBestDecksByPlayer(playerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(deckRepository).findBestDecksByPlayerId(playerId);
    }
}
