package com.magicsummoner.game;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.enums.BattleResult;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.game.model.BoardCreature;
import com.magicsummoner.game.model.GamePlayer;
import com.magicsummoner.game.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine gameEngine;
    private GameState gameState;
    private GamePlayer player1;
    private GamePlayer player2;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();

        // Create test cards
        List<CardDTO> deck1 = createTestDeck();
        List<CardDTO> deck2 = createTestDeck();

        player1 = GamePlayer.create(1L, "Player1", deck1);
        player2 = GamePlayer.create(2L, "Player2", deck2);

        gameState = GameState.create(1L, player1, player2, 120);
    }

    private List<CardDTO> createTestDeck() {
        List<CardDTO> deck = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            deck.add(CardDTO.builder()
                    .id((long) i)
                    .name("Card " + i)
                    .cardType(CardType.CREATURE)
                    .manaCost(2)
                    .attack(3)
                    .health(3)
                    .build());
        }
        return deck;
    }

    private CardDTO createCreatureCard(Long id, String name, int manaCost, int attack, int health) {
        return CardDTO.builder()
                .id(id)
                .name(name)
                .cardType(CardType.CREATURE)
                .manaCost(manaCost)
                .attack(attack)
                .health(health)
                .maxHealth(health)
                .hasCharge(false)
                .build();
    }

    private CardDTO createSpellCard(Long id, String name, int manaCost, int spellDamage) {
        return CardDTO.builder()
                .id(id)
                .name(name)
                .cardType(CardType.SPELL)
                .manaCost(manaCost)
                .spellDamage(spellDamage)
                .build();
    }

    @Test
    @DisplayName("打出生物卡牌 - 正常场景")
    void playCard_Creature_Success() {
        // Given
        CardDTO creatureCard = createCreatureCard(100L, "Test Creature", 2, 3, 3);
        player1.getHand().add(creatureCard);
        player1.setMana(5);

        // When
        gameEngine.playCard(gameState, 1L, 100L, null);

        // Then
        assertEquals(1, player1.getBoard().size());
        assertFalse(player1.getHand().contains(creatureCard));
        assertEquals(3, player1.getMana());
    }

    @Test
    @DisplayName("打出生物卡牌 - 不是当前玩家回合")
    void playCard_NotYourTurn_ThrowsException() {
        // Given - player2's turn
        gameState.setCurrentPlayerId(2L);
        CardDTO creatureCard = createCreatureCard(100L, "Test Creature", 2, 3, 3);
        player1.getHand().add(creatureCard);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, 1L, 100L, null));
        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("打出生物卡牌 - 卡牌不在手牌中")
    void playCard_CardNotInHand_ThrowsException() {
        // Given
        player1.setMana(5);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, 1L, 999L, null));
        assertEquals("Card not found in hand", exception.getMessage());
    }

    @Test
    @DisplayName("打出生物卡牌 - 法力不足")
    void playCard_NotEnoughMana_ThrowsException() {
        // Given
        CardDTO expensiveCard = createCreatureCard(100L, "Expensive", 10, 5, 5);
        player1.getHand().add(expensiveCard);
        player1.setMana(3);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, 1L, 100L, null));
        assertEquals("Not enough mana", exception.getMessage());
    }

    @Test
    @DisplayName("打出生物卡牌 - 战场已满")
    void playCard_BoardFull_ThrowsException() {
        // Given
        player1.setMana(10);

        // Fill board with 5 creatures
        for (int i = 0; i < 5; i++) {
            CardDTO card = createCreatureCard((long) (100 + i), "Creature " + i, 1, 1, 1);
            BoardCreature creature = BoardCreature.fromCard(card);
            player1.getBoard().add(creature);
        }

        CardDTO newCard = createCreatureCard(200L, "New Creature", 1, 1, 1);
        player1.getHand().add(newCard);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, 1L, 200L, null));
        assertEquals("Board is full", exception.getMessage());
    }

    @Test
    @DisplayName("打出冲锋生物 - 可以立即攻击")
    void playCard_ChargeCreature_CanAttackImmediately() {
        // Given
        CardDTO chargeCard = CardDTO.builder()
                .id(100L)
                .name("Charge Creature")
                .cardType(CardType.CREATURE)
                .manaCost(3)
                .attack(4)
                .health(3)
                .hasCharge(true)
                .build();
        player1.getHand().add(chargeCard);
        player1.setMana(5);

        // When
        gameEngine.playCard(gameState, 1L, 100L, null);

        // Then
        BoardCreature creature = player1.getBoard().get(0);
        assertTrue(creature.isCanAttack());
    }

    @Test
    @DisplayName("施放伤害法术 - 正常场景")
    void playCard_DamageSpell_Success() {
        // Given
        CardDTO spellCard = createSpellCard(100L, "Fireball", 3, 5);
        player1.getHand().add(spellCard);
        player1.setMana(5);

        int initialHealth = player2.getHealth();

        // When
        gameEngine.playCard(gameState, 1L, 100L, null);

        // Then
        assertEquals(initialHealth - 5, player2.getHealth());
        assertTrue(player1.getGraveyard().contains(spellCard));
    }

    @Test
    @DisplayName("施放治疗法术 - 正常场景")
    void playCard_HealSpell_Success() {
        // Given
        player1.takeDamage(10);
        int damagedHealth = player1.getHealth();

        CardDTO healSpell = CardDTO.builder()
                .id(100L)
                .name("Heal")
                .cardType(CardType.SPELL)
                .manaCost(2)
                .healAmount(5)
                .build();
        player1.getHand().add(healSpell);
        player1.setMana(5);

        // When
        gameEngine.playCard(gameState, 1L, 100L, null);

        // Then
        assertEquals(damagedHealth + 5, player1.getHealth());
    }

    @Test
    @DisplayName("攻击敌方英雄 - 正常场景")
    void attack_Hero_Success() {
        // Given
        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 4, 3);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        int initialHealth = player2.getHealth();

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);

        // Then
        assertEquals(initialHealth - 4, player2.getHealth());
        assertFalse(attacker.isCanAttack());
    }

    @Test
    @DisplayName("攻击敌方英雄 - 有嘲讽随从必须先攻击嘲讽")
    void attack_HeroWithTaunt_ThrowsException() {
        // Given
        CardDTO tauntCard = CardDTO.builder()
                .id(50L)
                .name("Taunt")
                .cardType(CardType.CREATURE)
                .attack(2)
                .health(5)
                .hasTaunt(true)
                .build();
        BoardCreature tauntCreature = BoardCreature.fromCard(tauntCard);
        player2.getBoard().add(tauntCreature);

        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 4, 3);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null));
        assertEquals("Must attack taunt creature first", exception.getMessage());
    }

    @Test
    @DisplayName("攻击敌方随从 - 正常场景")
    void attack_Creature_Success() {
        // Given
        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 5, 4);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO targetCard = createCreatureCard(200L, "Target", 2, 3, 3);
        BoardCreature target = BoardCreature.fromCard(targetCard);
        player2.getBoard().add(target);

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), target.getInstanceId());

        // Then
        assertTrue(target.isDead());
        assertEquals(1, attacker.getHealth());
    }

    @Test
    @DisplayName("攻击潜行随从 - 无法攻击")
    void attack_StealthTarget_ThrowsException() {
        // Given
        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 5, 4);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO stealthCard = CardDTO.builder()
                .id(200L)
                .name("Stealth")
                .cardType(CardType.CREATURE)
                .attack(3)
                .health(3)
                .hasStealth(true)
                .build();
        BoardCreature stealthTarget = BoardCreature.fromCard(stealthCard);
        player2.getBoard().add(stealthTarget);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, 1L, attacker.getInstanceId(), stealthTarget.getInstanceId()));
        assertEquals("Cannot attack stealth creature", exception.getMessage());
    }

    @Test
    @DisplayName("攻击时触发吸血 - 恢复生命")
    void attack_WithLifesteal_Heals() {
        // Given
        player1.takeDamage(10);
        int damagedHealth = player1.getHealth();

        CardDTO lifestealCard = CardDTO.builder()
                .id(100L)
                .name("Lifesteal")
                .cardType(CardType.CREATURE)
                .attack(5)
                .health(4)
                .hasLifesteal(true)
                .build();
        BoardCreature attacker = BoardCreature.fromCard(lifestealCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);

        // Then
        assertEquals(damagedHealth + 5, player1.getHealth());
    }

    @Test
    @DisplayName("剧毒攻击 - 直接消灭目标")
    void attack_Poisonous_KillsTarget() {
        // Given
        CardDTO poisonousCard = CardDTO.builder()
                .id(100L)
                .name("Poisonous")
                .cardType(CardType.CREATURE)
                .attack(1)
                .health(1)
                .hasPoisonous(true)
                .build();
        BoardCreature attacker = BoardCreature.fromCard(poisonousCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO bigTarget = createCreatureCard(200L, "Big Target", 8, 10, 10);
        BoardCreature target = BoardCreature.fromCard(bigTarget);
        player2.getBoard().add(target);

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), target.getInstanceId());

        // Then
        assertTrue(target.isDead());
    }

    @Test
    @DisplayName("圣盾抵挡伤害 - 正常场景")
    void attack_DivineShield_BlocksDamage() {
        // Given
        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 5, 4);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO divineShieldCard = CardDTO.builder()
                .id(200L)
                .name("Divine Shield")
                .cardType(CardType.CREATURE)
                .attack(3)
                .health(3)
                .hasDivineShield(true)
                .build();
        BoardCreature target = BoardCreature.fromCard(divineShieldCard);
        player2.getBoard().add(target);

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), target.getInstanceId());

        // Then
        assertFalse(target.isDead());
        assertEquals(3, target.getHealth());
        assertFalse(target.isHasDivineShield());
    }

    @Test
    @DisplayName("结束回合 - 正常场景")
    void endTurn_Success() {
        // Given
        Long initialPlayerId = gameState.getCurrentPlayerId();

        // When
        gameEngine.endTurn(gameState, 1L);

        // Then
        assertNotEquals(initialPlayerId, gameState.getCurrentPlayerId());
        assertEquals(2, gameState.getTurnNumber());
    }

    @Test
    @DisplayName("结束回合 - 不是当前玩家")
    void endTurn_NotYourTurn_ThrowsException() {
        // Given
        gameState.setCurrentPlayerId(2L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.endTurn(gameState, 1L));
        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("投降 - 正常场景")
    void surrender_Success() {
        // When
        gameEngine.surrender(gameState, 1L);

        // Then
        assertTrue(gameState.isGameOver());
        assertEquals(2L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER1_SURRENDER, gameState.getResult());
    }

    @Test
    @DisplayName("游戏结束检测 - 玩家1死亡")
    void checkGameOver_Player1Dead() {
        // Given
        player1.takeDamage(30);

        // When
        gameState.checkGameOver();

        // Then
        assertTrue(gameState.isGameOver());
        assertEquals(2L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER2_WIN, gameState.getResult());
    }

    @Test
    @DisplayName("游戏结束检测 - 玩家2死亡")
    void checkGameOver_Player2Dead() {
        // Given
        player2.takeDamage(30);

        // When
        gameState.checkGameOver();

        // Then
        assertTrue(gameState.isGameOver());
        assertEquals(1L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER1_WIN, gameState.getResult());
    }

    @Test
    @DisplayName("装备装备牌 - 正常场景")
    void playCard_Equipment_Success() {
        // Given
        CardDTO creatureCard = createCreatureCard(100L, "Creature", 2, 3, 3);
        BoardCreature creature = BoardCreature.fromCard(creatureCard);
        player1.getBoard().add(creature);

        CardDTO equipmentCard = CardDTO.builder()
                .id(200L)
                .name("Sword")
                .cardType(CardType.EQUIPMENT)
                .manaCost(2)
                .attackBonus(2)
                .healthBonus(1)
                .build();
        player1.getHand().add(equipmentCard);
        player1.setMana(5);

        int initialAttack = creature.getAttack();
        int initialHealth = creature.getHealth();

        // When
        gameEngine.playCard(gameState, 1L, 200L, creature.getInstanceId());

        // Then
        assertEquals(initialAttack + 2, creature.getAttack());
        assertEquals(initialHealth + 1, creature.getHealth());
        assertTrue(player1.getGraveyard().contains(equipmentCard));
    }

    @Test
    @DisplayName("装备装备牌 - 没有指定目标")
    void playCard_EquipmentNoTarget_ThrowsException() {
        // Given
        CardDTO equipmentCard = CardDTO.builder()
                .id(200L)
                .name("Sword")
                .cardType(CardType.EQUIPMENT)
                .manaCost(2)
                .attackBonus(2)
                .build();
        player1.getHand().add(equipmentCard);
        player1.setMana(5);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.playCard(gameState, 1L, 200L, null));
        assertEquals("Equipment needs a target creature", exception.getMessage());
    }

    @Test
    @DisplayName("攻击后失去潜行")
    void attack_RemovesStealth() {
        // Given
        CardDTO stealthCard = CardDTO.builder()
                .id(100L)
                .name("Stealth Attacker")
                .cardType(CardType.CREATURE)
                .attack(3)
                .health(3)
                .hasStealth(true)
                .build();
        BoardCreature attacker = BoardCreature.fromCard(stealthCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        // When
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);

        // Then
        assertFalse(attacker.isHasStealth());
    }

    @Test
    @DisplayName("无法攻击 - 随从已经攻击过")
    void attack_AlreadyAttacked_ThrowsException() {
        // Given
        CardDTO attackerCard = createCreatureCard(100L, "Attacker", 2, 4, 3);
        BoardCreature attacker = BoardCreature.fromCard(attackerCard);
        attacker.setCanAttack(false);
        player1.getBoard().add(attacker);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null));
        assertEquals("Creature cannot attack", exception.getMessage());
    }
}
