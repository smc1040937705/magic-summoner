package com.magicsummoner.game;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.BattleResult;
import com.magicsummoner.game.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameEngineTest {

    private GameEngine gameEngine;
    private GameState gameState;
    private GamePlayer player1;
    private GamePlayer player2;
    private List<CardDTO> testDeck;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();
        
        testDeck = createTestDeck();
        player1 = GamePlayer.create(1L, "Player1", testDeck);
        player2 = GamePlayer.create(2L, "Player2", testDeck);
        
        gameState = GameState.create(1L, player1, player2, 90);
    }

    private List<CardDTO> createTestDeck() {
        List<CardDTO> deck = new ArrayList<>();
        
        deck.add(CardDTO.builder().id(1L).name("Warrior").cardType(CardType.CREATURE).manaCost(2).attack(2).health(3).build());
        deck.add(CardDTO.builder().id(2L).name("Mage").cardType(CardType.CREATURE).manaCost(3).attack(3).health(2).build());
        deck.add(CardDTO.builder().id(3L).name("Charge Warrior").cardType(CardType.CREATURE).manaCost(3).attack(3).health(1).hasCharge(true).build());
        deck.add(CardDTO.builder().id(4L).name("Taunt Golem").cardType(CardType.CREATURE).manaCost(4).attack(2).health(6).hasTaunt(true).build());
        deck.add(CardDTO.builder().id(5L).name("Fireball").cardType(CardType.SPELL).manaCost(4).spellDamage(6).build());
        deck.add(CardDTO.builder().id(6L).name("Healing Touch").cardType(CardType.SPELL).manaCost(3).healAmount(8).build());
        deck.add(CardDTO.builder().id(7L).name("Draw Card Spell").cardType(CardType.SPELL).manaCost(1).drawCards(2).build());
        deck.add(CardDTO.builder().id(8L).name("Battle Axe").cardType(CardType.EQUIPMENT).manaCost(3).attackBonus(3).healthBonus(0).build());
        deck.add(CardDTO.builder().id(9L).name("Lifesteal Creature").cardType(CardType.CREATURE).manaCost(4).attack(3).health(3).hasLifesteal(true).build());
        deck.add(CardDTO.builder().id(10L).name("Poisonous Snake").cardType(CardType.CREATURE).manaCost(2).attack(1).health(1).hasPoisonous(true).build());
        deck.add(CardDTO.builder().id(11L).name("Divine Shield").cardType(CardType.CREATURE).manaCost(2).attack(1).health(1).hasDivineShield(true).build());
        deck.add(CardDTO.builder().id(12L).name("Stealth Rogue").cardType(CardType.CREATURE).manaCost(3).attack(4).health(2).hasStealth(true).build());
        
        for (int i = 0; i < 8; i++) {
            deck.add(CardDTO.builder().id((long) (100 + i)).name("Creature " + i).cardType(CardType.CREATURE).manaCost(2).attack(2).health(2).build());
        }
        
        return deck;
    }

    @Test
    void playCard_NotYourTurn_ShouldThrowException() {
        Long wrongPlayerId = 999L;
        CardDTO card = player1.getHand().get(0);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, wrongPlayerId, card.getId(), null));

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    void playCard_CardNotInHand_ShouldThrowException() {
        Long nonExistentCardId = 999L;

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, player1.getPlayerId(), nonExistentCardId, null));

        assertEquals("Card not found in hand", exception.getMessage());
    }

    @Test
    void playCard_NotEnoughMana_ShouldThrowException() {
        CardDTO expensiveCard = CardDTO.builder().id(999L).name("Expensive").manaCost(999).build();
        player1.getHand().add(expensiveCard);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, player1.getPlayerId(), 999L, null));

        assertEquals("Not enough mana", exception.getMessage());
    }

    @Test
    void playCard_SummonCreature_ShouldAddCreatureToBoard() {
        player1.setMana(10);
        CardDTO creatureCard = CardDTO.builder().id(100L).name("Test Creature").cardType(CardType.CREATURE).manaCost(2).attack(2).health(3).build();
        player1.getHand().add(creatureCard);
        int initialBoardSize = player1.getBoard().size();

        gameEngine.playCard(gameState, player1.getPlayerId(), 100L, null);

        assertEquals(initialBoardSize + 1, player1.getBoard().size());
        assertFalse(player1.getHand().contains(creatureCard));
    }

    @Test
    void playCard_SummonCreatureWithCharge_ShouldBeAbleToAttack() {
        player1.setMana(10);
        CardDTO chargeCard = CardDTO.builder().id(200L).name("Charge Creature").cardType(CardType.CREATURE).manaCost(3).attack(3).health(2).hasCharge(true).build();
        player1.getHand().add(chargeCard);

        gameEngine.playCard(gameState, player1.getPlayerId(), 200L, null);

        BoardCreature summoned = player1.getBoard().get(player1.getBoard().size() - 1);
        assertTrue(summoned.isCanAttack());
        assertTrue(summoned.isHasCharge());
    }

    @Test
    void playCard_BoardFull_ShouldThrowException() {
        player1.setMana(10);
        for (int i = 0; i < 5; i++) {
            CardDTO card = CardDTO.builder().id((long) i).name("Creature" + i).build();
            BoardCreature creature = BoardCreature.builder()
                    .instanceId((long) i)
                    .card(card)
                    .attack(2)
                    .health(2)
                    .build();
            player1.summonCreature(null, creature);
        }
        CardDTO extraCreature = CardDTO.builder().id(300L).name("Extra").cardType(CardType.CREATURE).manaCost(1).attack(2).health(2).build();
        player1.getHand().add(extraCreature);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, player1.getPlayerId(), 300L, null));

        assertEquals("Board is full", exception.getMessage());
    }

    @Test
    void playCard_CastDamageSpellOnHero_ShouldDealDamage() {
        player1.setMana(10);
        int initialHealth = player2.getHealth();
        CardDTO fireball = CardDTO.builder().id(400L).name("Fireball").cardType(CardType.SPELL).manaCost(4).spellDamage(6).build();
        player1.getHand().add(fireball);

        gameEngine.playCard(gameState, player1.getPlayerId(), 400L, null);

        assertEquals(initialHealth - 6, player2.getHealth());
        assertTrue(player1.getGraveyard().contains(fireball));
    }

    @Test
    void playCard_CastDamageSpellOnCreature_ShouldDealDamageToCreature() {
        player1.setMana(10);
        BoardCreature targetCreature = BoardCreature.fromCard(
                CardDTO.builder().id(500L).name("Target").attack(2).health(5).build()
        );
        player2.getBoard().add(targetCreature);
        
        CardDTO fireball = CardDTO.builder().id(401L).name("Fireball").cardType(CardType.SPELL).manaCost(4).spellDamage(3).build();
        player1.getHand().add(fireball);

        gameEngine.playCard(gameState, player1.getPlayerId(), 401L, targetCreature.getInstanceId());

        assertEquals(2, targetCreature.getHealth());
    }

    @Test
    void playCard_CastHealingSpell_ShouldHealPlayer() {
        player1.setMana(10);
        player1.setHealth(10);
        CardDTO heal = CardDTO.builder().id(600L).name("Heal").cardType(CardType.SPELL).manaCost(3).healAmount(8).build();
        player1.getHand().add(heal);

        gameEngine.playCard(gameState, player1.getPlayerId(), 600L, null);

        assertEquals(18, player1.getHealth());
    }

    @Test
    void playCard_CastDrawSpell_ShouldDrawCards() {
        player1.setMana(10);
        CardDTO drawCard = CardDTO.builder().id(700L).name("Draw").cardType(CardType.SPELL).manaCost(1).drawCards(2).build();
        player1.getHand().add(drawCard);
        int handSizeBeforePlay = player1.getHand().size();

        gameEngine.playCard(gameState, player1.getPlayerId(), 700L, null);

        assertEquals(handSizeBeforePlay + 1, player1.getHand().size());
    }

    @Test
    void playCard_EquipmentWithoutTarget_ShouldThrowException() {
        player1.setMana(10);
        CardDTO equipment = CardDTO.builder().id(800L).name("Axe").cardType(CardType.EQUIPMENT).manaCost(3).attackBonus(3).build();
        player1.getHand().add(equipment);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, player1.getPlayerId(), 800L, null));

        assertEquals("Equipment needs a target creature", exception.getMessage());
    }

    @Test
    void playCard_EquipmentWithTarget_ShouldBuffCreature() {
        player1.setMana(10);
        CardDTO creatureCard = CardDTO.builder().id(900L).name("Creature").attack(2).health(3).build();
        BoardCreature targetCreature = BoardCreature.builder()
                .instanceId(900L)
                .card(creatureCard)
                .attack(2)
                .health(3)
                .build();
        player1.getBoard().add(targetCreature);
        
        CardDTO equipment = CardDTO.builder().id(801L).name("Axe").cardType(CardType.EQUIPMENT).manaCost(3).attackBonus(3).healthBonus(2).build();
        player1.getHand().add(equipment);

        gameEngine.playCard(gameState, player1.getPlayerId(), 801L, targetCreature.getInstanceId());

        assertEquals(5, targetCreature.getAttack());
        assertEquals(5, targetCreature.getHealth());
    }

    @Test
    void attack_NotYourTurn_ShouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, 999L, 1L, null));

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    void attack_CreatureCannotAttack_ShouldThrowException() {
        CardDTO card = CardDTO.builder().id(1L).name("Test Creature").build();
        BoardCreature creature = BoardCreature.builder()
                .instanceId(100L)
                .card(card)
                .attack(2)
                .health(2)
                .canAttack(false)
                .build();
        player1.getBoard().add(creature);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, player1.getPlayerId(), creature.getInstanceId(), null));

        assertEquals("Creature cannot attack", exception.getMessage());
    }

    @Test
    void attack_HeroWithTauntOnBoard_ShouldThrowException() {
        CardDTO card1 = CardDTO.builder().id(1L).name("Attacker").build();
        BoardCreature creature = BoardCreature.builder()
                .instanceId(101L)
                .card(card1)
                .attack(3)
                .health(3)
                .hasCharge(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(creature);
        
        CardDTO card2 = CardDTO.builder().id(2L).name("Taunt").build();
        BoardCreature tauntCreature = BoardCreature.builder()
                .instanceId(102L)
                .card(card2)
                .attack(2)
                .health(5)
                .hasTaunt(true)
                .build();
        player2.getBoard().add(tauntCreature);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, player1.getPlayerId(), creature.getInstanceId(), null));

        assertEquals("Must attack taunt creature first", exception.getMessage());
    }

    @Test
    void attack_AttackHeroSuccessfully_ShouldDealDamage() {
        int initialHealth = player2.getHealth();
        CardDTO card = CardDTO.builder().id(1L).name("Attacker").build();
        BoardCreature attacker = BoardCreature.builder()
                .instanceId(103L)
                .card(card)
                .attack(4)
                .health(3)
                .hasCharge(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(attacker);

        gameEngine.attack(gameState, player1.getPlayerId(), attacker.getInstanceId(), null);

        assertEquals(initialHealth - 4, player2.getHealth());
        assertFalse(attacker.isCanAttack());
    }

    @Test
    void attack_WithLifesteal_ShouldHealAttackerPlayer() {
        player1.setHealth(20);
        CardDTO card = CardDTO.builder().id(1L).name("Lifesteal").build();
        BoardCreature lifestealAttacker = BoardCreature.builder()
                .instanceId(104L)
                .card(card)
                .attack(5)
                .health(2)
                .hasCharge(true)
                .hasLifesteal(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(lifestealAttacker);

        gameEngine.attack(gameState, player1.getPlayerId(), lifestealAttacker.getInstanceId(), null);

        assertEquals(25, player1.getHealth());
    }

    @Test
    void attack_CreatureCombat_ShouldDealDamageBothWays() {
        CardDTO card1 = CardDTO.builder().id(1L).name("Attacker").build();
        BoardCreature attacker = BoardCreature.builder()
                .instanceId(105L)
                .card(card1)
                .attack(3)
                .health(5)
                .hasCharge(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(attacker);
        
        CardDTO card2 = CardDTO.builder().id(2L).name("Defender").build();
        BoardCreature defender = BoardCreature.builder()
                .instanceId(106L)
                .card(card2)
                .attack(2)
                .health(4)
                .build();
        player2.getBoard().add(defender);

        gameEngine.attack(gameState, player1.getPlayerId(), attacker.getInstanceId(), defender.getInstanceId());

        assertEquals(3, attacker.getHealth());
        assertEquals(1, defender.getHealth());
    }

    @Test
    void attack_WithPoisonous_ShouldKillTarget() {
        CardDTO card1 = CardDTO.builder().id(1L).name("Poison").build();
        BoardCreature poisonousAttacker = BoardCreature.builder()
                .instanceId(107L)
                .card(card1)
                .attack(1)
                .health(1)
                .hasCharge(true)
                .hasPoisonous(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(poisonousAttacker);
        
        CardDTO card2 = CardDTO.builder().id(2L).name("Victim").build();
        BoardCreature defender = BoardCreature.builder()
                .instanceId(108L)
                .card(card2)
                .attack(5)
                .health(10)
                .build();
        player2.getBoard().add(defender);

        gameEngine.attack(gameState, player1.getPlayerId(), poisonousAttacker.getInstanceId(), defender.getInstanceId());

        assertEquals(0, defender.getHealth());
        assertTrue(player2.getBoard().isEmpty());
    }

    @Test
    void attack_StealthCreature_ShouldLoseStealthAfterAttack() {
        CardDTO card = CardDTO.builder().id(1L).name("Stealth").build();
        BoardCreature stealthAttacker = BoardCreature.builder()
                .instanceId(109L)
                .card(card)
                .attack(3)
                .health(2)
                .hasCharge(true)
                .hasStealth(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(stealthAttacker);

        assertTrue(stealthAttacker.isHasStealth());
        
        gameEngine.attack(gameState, player1.getPlayerId(), stealthAttacker.getInstanceId(), null);

        assertFalse(stealthAttacker.isHasStealth());
    }

    @Test
    void attack_DivineShield_ShouldBlockFirstDamage() {
        CardDTO card1 = CardDTO.builder().id(1L).name("Attacker").build();
        BoardCreature attacker = BoardCreature.builder()
                .instanceId(110L)
                .card(card1)
                .attack(4)
                .health(2)
                .hasCharge(true)
                .canAttack(true)
                .build();
        player1.getBoard().add(attacker);
        
        CardDTO card2 = CardDTO.builder().id(2L).name("Shielded").build();
        BoardCreature shieldDefender = BoardCreature.builder()
                .instanceId(111L)
                .card(card2)
                .attack(2)
                .health(3)
                .hasDivineShield(true)
                .build();
        player2.getBoard().add(shieldDefender);

        assertTrue(shieldDefender.isHasDivineShield());
        
        gameEngine.attack(gameState, player1.getPlayerId(), attacker.getInstanceId(), shieldDefender.getInstanceId());

        assertFalse(shieldDefender.isHasDivineShield());
        assertEquals(3, shieldDefender.getHealth());
    }

    @Test
    void endTurn_NotYourTurn_ShouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.endTurn(gameState, 999L));

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    void endTurn_Success_ShouldChangePlayerTurn() {
        Long initialPlayerId = gameState.getCurrentPlayerId();
        int initialTurn = gameState.getTurnNumber();

        gameEngine.endTurn(gameState, initialPlayerId);

        assertNotEquals(initialPlayerId, gameState.getCurrentPlayerId());
        assertEquals(initialTurn + 1, gameState.getTurnNumber());
    }

    @Test
    void surrender_ShouldEndGameWithOpponentAsWinner() {
        gameEngine.surrender(gameState, player1.getPlayerId());

        assertTrue(gameState.isGameOver());
        assertEquals(player2.getPlayerId(), gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER1_SURRENDER, gameState.getResult());
    }
}
