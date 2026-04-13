package com.magicsummoner.game;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.enums.BattleResult;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.game.model.BoardCreature;
import com.magicsummoner.game.model.GameAction;
import com.magicsummoner.game.model.GamePlayer;
import com.magicsummoner.game.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine gameEngine;
    private GameState gameState;
    private GamePlayer player1;
    private GamePlayer player2;
    private CardDTO creatureCard;
    private CardDTO spellCard;
    private CardDTO equipmentCard;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngine();

        creatureCard = createCreatureCard(1L, "Fire Dragon", 5, 6, 5);
        spellCard = createSpellCard(2L, "Fireball", 3, 4);
        equipmentCard = createEquipmentCard(3L, "Sword", 2, 2, 0);

        List<CardDTO> deck1 = createTestDeck(creatureCard, spellCard, equipmentCard);
        List<CardDTO> deck2 = createTestDeck(creatureCard, spellCard, equipmentCard);

        player1 = GamePlayer.create(1L, "Player1", deck1);
        player2 = GamePlayer.create(2L, "Player2", deck2);

        gameState = GameState.create(1L, player1, player2, 120);
    }

    private CardDTO createCreatureCard(Long id, String name, int manaCost, int attack, int health) {
        CardDTO card = new CardDTO();
        card.setId(id);
        card.setName(name);
        card.setCardType(CardType.CREATURE);
        card.setElementType(ElementType.FIRE);
        card.setRarity(CardRarity.RARE);
        card.setManaCost(manaCost);
        card.setAttack(attack);
        card.setHealth(health);
        card.setMaxHealth(health);
        card.setHasCharge(false);
        card.setHasTaunt(false);
        card.setHasStealth(false);
        card.setHasDivineShield(false);
        card.setHasWindfury(false);
        card.setHasLifesteal(false);
        card.setHasPoisonous(false);
        return card;
    }

    private CardDTO createSpellCard(Long id, String name, int manaCost, int spellDamage) {
        CardDTO card = new CardDTO();
        card.setId(id);
        card.setName(name);
        card.setCardType(CardType.SPELL);
        card.setElementType(ElementType.FIRE);
        card.setRarity(CardRarity.RARE);
        card.setManaCost(manaCost);
        card.setSpellDamage(spellDamage);
        return card;
    }

    private CardDTO createEquipmentCard(Long id, String name, int manaCost, int attackBonus, int healthBonus) {
        CardDTO card = new CardDTO();
        card.setId(id);
        card.setName(name);
        card.setCardType(CardType.EQUIPMENT);
        card.setElementType(ElementType.FIRE);
        card.setRarity(CardRarity.RARE);
        card.setManaCost(manaCost);
        card.setAttackBonus(attackBonus);
        card.setHealthBonus(healthBonus);
        return card;
    }

    private List<CardDTO> createTestDeck(CardDTO... cards) {
        List<CardDTO> deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            for (CardDTO card : cards) {
                deck.add(card);
            }
        }
        return deck;
    }

    @Test
    @DisplayName("游戏初始化成功")
    void gameInitialization_Success() {
        assertNotNull(gameState);
        assertEquals(1L, gameState.getRoomId());
        assertEquals(1L, gameState.getCurrentPlayerId());
        assertEquals(1, gameState.getTurnNumber());
        assertFalse(gameState.isGameOver());
        assertEquals(4, player1.getHand().size());
        assertEquals(4, player2.getHand().size());
    }

    @Test
    @DisplayName("出牌成功 - 召唤生物")
    void playCard_SummonCreature_Success() {
        CardDTO card = player1.getHand().get(0);
        player1.setMana(10);
        int initialHandSize = player1.getHand().size();

        gameEngine.playCard(gameState, 1L, card.getId(), null);

        assertEquals(1, player1.getBoard().size());
        assertEquals(initialHandSize - 1, player1.getHand().size());
    }

    @Test
    @DisplayName("出牌失败 - 不是你的回合")
    void playCard_NotYourTurn_ThrowsException() {
        CardDTO card = player2.getHand().get(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 2L, card.getId(), null);
        });

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("出牌失败 - 卡牌不在手中")
    void playCard_CardNotInHand_ThrowsException() {
        CardDTO unknownCard = createCreatureCard(999L, "Unknown", 1, 1, 1);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 1L, unknownCard.getId(), null);
        });

        assertEquals("Card not found in hand", exception.getMessage());
    }

    @Test
    @DisplayName("出牌失败 - 法力不足")
    void playCard_NotEnoughMana_ThrowsException() {
        CardDTO card = player1.getHand().get(0);
        player1.setMana(1);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 1L, card.getId(), null);
        });

        assertEquals("Not enough mana", exception.getMessage());
    }

    @Test
    @DisplayName("出牌失败 - 场地已满")
    void playCard_BoardFull_ThrowsException() {
        player1.setMana(10);
        for (int i = 0; i < 5; i++) {
            BoardCreature creature = BoardCreature.fromCard(creatureCard);
            player1.getBoard().add(creature);
        }

        CardDTO card = player1.getHand().get(0);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 1L, card.getId(), null);
        });

        assertEquals("Board is full", exception.getMessage());
    }

    @Test
    @DisplayName("施放法术成功 - 对敌方英雄造成伤害")
    void playCard_CastSpell_DamageHero_Success() {
        CardDTO spell = createSpellCard(100L, "Fireball", 3, 6);
        player1.getHand().add(spell);
        player1.setMana(10);
        int initialHealth = player2.getHealth();

        gameEngine.playCard(gameState, 1L, spell.getId(), null);

        assertEquals(initialHealth - 6, player2.getHealth());
    }

    @Test
    @DisplayName("施放法术成功 - 对敌方生物造成伤害")
    void playCard_CastSpell_DamageCreature_Success() {
        CardDTO spell = createSpellCard(100L, "Fireball", 3, 3);
        player1.getHand().add(spell);
        player1.setMana(10);

        BoardCreature enemyCreature = BoardCreature.fromCard(creatureCard);
        player2.getBoard().add(enemyCreature);
        int initialHealth = enemyCreature.getHealth();

        gameEngine.playCard(gameState, 1L, spell.getId(), enemyCreature.getInstanceId());

        assertEquals(initialHealth - 3, enemyCreature.getHealth());
    }

    @Test
    @DisplayName("施放法术成功 - 消灭生物")
    void playCard_CastSpell_KillCreature_Success() {
        CardDTO weakCreature = createCreatureCard(50L, "Weak", 1, 1, 2);
        CardDTO spell = createSpellCard(100L, "Fireball", 3, 5);
        player1.getHand().add(spell);
        player1.setMana(10);

        BoardCreature enemyCreature = BoardCreature.fromCard(weakCreature);
        player2.getBoard().add(enemyCreature);

        gameEngine.playCard(gameState, 1L, spell.getId(), enemyCreature.getInstanceId());

        assertEquals(0, player2.getBoard().size());
    }

    @Test
    @DisplayName("装备物品成功")
    void playCard_EquipItem_Success() {
        CardDTO creature = createCreatureCard(50L, "Test Creature", 3, 3, 3);
        player1.setMana(10);
        player1.getHand().add(equipmentCard);

        BoardCreature boardCreature = BoardCreature.fromCard(creature);
        player1.getBoard().add(boardCreature);
        int initialAttack = boardCreature.getAttack();

        gameEngine.playCard(gameState, 1L, equipmentCard.getId(), boardCreature.getInstanceId());

        assertEquals(initialAttack + 2, boardCreature.getAttack());
    }

    @Test
    @DisplayName("装备物品失败 - 无目标")
    void playCard_EquipItem_NoTarget_ThrowsException() {
        player1.setMana(10);
        player1.getHand().add(equipmentCard);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 1L, equipmentCard.getId(), null);
        });

        assertEquals("Equipment needs a target creature", exception.getMessage());
    }

    @Test
    @DisplayName("装备物品失败 - 目标生物不存在")
    void playCard_EquipItem_TargetNotFound_ThrowsException() {
        player1.setMana(10);
        player1.getHand().add(equipmentCard);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.playCard(gameState, 1L, equipmentCard.getId(), 999L);
        });

        assertEquals("Target creature not found", exception.getMessage());
    }

    @Test
    @DisplayName("攻击成功 - 攻击敌方英雄")
    void attack_Hero_Success() {
        player1.setMana(10);
        CardDTO creature = player1.getHand().get(0);
        gameEngine.playCard(gameState, 1L, creature.getId(), null);

        BoardCreature attacker = player1.getBoard().get(0);
        attacker.setCanAttack(true);

        int initialHealth = player2.getHealth();
        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);

        assertEquals(initialHealth - attacker.getAttack(), player2.getHealth());
    }

    @Test
    @DisplayName("攻击成功 - 攻击敌方生物")
    void attack_Creature_Success() {
        player1.setMana(10);
        player2.setMana(10);

        CardDTO creature1 = player1.getHand().get(0);
        CardDTO creature2 = player2.getHand().get(0);

        gameEngine.playCard(gameState, 1L, creature1.getId(), null);
        BoardCreature attacker = player1.getBoard().get(0);
        attacker.setCanAttack(true);

        BoardCreature defender = BoardCreature.fromCard(creature2);
        player2.getBoard().add(defender);

        int attackerInitialHealth = attacker.getHealth();
        int defenderInitialHealth = defender.getHealth();

        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), defender.getInstanceId());

        assertEquals(attackerInitialHealth - defender.getAttack(), attacker.getHealth());
        assertEquals(defenderInitialHealth - attacker.getAttack(), defender.getHealth());
    }

    @Test
    @DisplayName("攻击失败 - 不是你的回合")
    void attack_NotYourTurn_ThrowsException() {
        BoardCreature creature = BoardCreature.fromCard(creatureCard);
        player2.getBoard().add(creature);
        creature.setCanAttack(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 2L, creature.getInstanceId(), null);
        });

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("攻击失败 - 攻击者不存在")
    void attack_AttackerNotFound_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 1L, 999L, null);
        });

        assertEquals("Attacker not found", exception.getMessage());
    }

    @Test
    @DisplayName("攻击失败 - 生物不能攻击")
    void attack_CreatureCannotAttack_ThrowsException() {
        BoardCreature creature = BoardCreature.fromCard(creatureCard);
        creature.setCanAttack(false);
        player1.getBoard().add(creature);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 1L, creature.getInstanceId(), null);
        });

        assertEquals("Creature cannot attack", exception.getMessage());
    }

    @Test
    @DisplayName("攻击失败 - 目标不存在")
    void attack_TargetNotFound_ThrowsException() {
        BoardCreature creature = BoardCreature.fromCard(creatureCard);
        creature.setCanAttack(true);
        player1.getBoard().add(creature);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 1L, creature.getInstanceId(), 999L);
        });

        assertEquals("Target not found", exception.getMessage());
    }

    @Test
    @DisplayName("攻击失败 - 必须先攻击嘲讽生物")
    void attack_MustAttackTaunt_ThrowsException() {
        BoardCreature attacker = BoardCreature.fromCard(creatureCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO tauntCard = createCreatureCard(50L, "Taunt", 3, 2, 5);
        tauntCard.setHasTaunt(true);
        BoardCreature tauntCreature = BoardCreature.fromCard(tauntCard);
        tauntCreature.setHasTaunt(true);
        player2.getBoard().add(tauntCreature);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);
        });

        assertEquals("Must attack taunt creature first", exception.getMessage());
    }

    @Test
    @DisplayName("攻击失败 - 不能攻击潜行生物")
    void attack_CannotAttackStealth_ThrowsException() {
        BoardCreature attacker = BoardCreature.fromCard(creatureCard);
        attacker.setCanAttack(true);
        player1.getBoard().add(attacker);

        CardDTO stealthCard = createCreatureCard(50L, "Stealth", 3, 2, 5);
        stealthCard.setHasStealth(true);
        BoardCreature stealthCreature = BoardCreature.fromCard(stealthCard);
        stealthCreature.setHasStealth(true);
        player2.getBoard().add(stealthCreature);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.attack(gameState, 1L, attacker.getInstanceId(), stealthCreature.getInstanceId());
        });

        assertEquals("Cannot attack stealth creature", exception.getMessage());
    }

    @Test
    @DisplayName("结束回合成功")
    void endTurn_Success() {
        Long currentTurn = gameState.getCurrentPlayerId();
        gameEngine.endTurn(gameState, 1L);

        assertNotEquals(currentTurn, gameState.getCurrentPlayerId());
        assertEquals(2, gameState.getTurnNumber());
    }

    @Test
    @DisplayName("结束回合失败 - 不是你的回合")
    void endTurn_NotYourTurn_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameEngine.endTurn(gameState, 2L);
        });

        assertEquals("Not your turn", exception.getMessage());
    }

    @Test
    @DisplayName("投降成功 - 玩家1投降")
    void surrender_Player1_Success() {
        gameEngine.surrender(gameState, 1L);

        assertTrue(gameState.isGameOver());
        assertEquals(2L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER1_SURRENDER, gameState.getResult());
    }

    @Test
    @DisplayName("投降成功 - 玩家2投降")
    void surrender_Player2_Success() {
        gameState.setCurrentPlayerId(2L);
        gameEngine.surrender(gameState, 2L);

        assertTrue(gameState.isGameOver());
        assertEquals(1L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER2_SURRENDER, gameState.getResult());
    }

    @Test
    @DisplayName("游戏结束 - 玩家生命值归零")
    void gameOver_PlayerHealthZero() {
        player2.setHealth(1);
        player1.setMana(10);

        CardDTO creature = player1.getHand().get(0);
        gameEngine.playCard(gameState, 1L, creature.getId(), null);

        BoardCreature attacker = player1.getBoard().get(0);
        attacker.setCanAttack(true);
        attacker.setAttack(10);

        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), null);

        assertTrue(gameState.isGameOver());
        assertEquals(1L, gameState.getWinnerId());
        assertEquals(BattleResult.PLAYER1_WIN, gameState.getResult());
    }

    @Test
    @DisplayName("圣盾效果测试")
    void divineShieldEffect_Success() {
        CardDTO divineCard = createCreatureCard(50L, "Divine Shield", 3, 3, 3);
        divineCard.setHasDivineShield(true);
        BoardCreature divineCreature = BoardCreature.fromCard(divineCard);
        divineCreature.setHasDivineShield(true);
        player2.getBoard().add(divineCreature);

        BoardCreature attacker = BoardCreature.fromCard(creatureCard);
        attacker.setCanAttack(true);
        attacker.setAttack(5);
        player1.getBoard().add(attacker);

        gameEngine.attack(gameState, 1L, attacker.getInstanceId(), divineCreature.getInstanceId());

        assertFalse(divineCreature.isHasDivineShield());
        assertEquals(3, divineCreature.getHealth());
    }

    @Test
    @DisplayName("剧毒效果测试")
    void poisonousEffect_Success() {
        CardDTO poisonCard = createCreatureCard(50L, "Poison", 3, 1, 1);
        poisonCard.setHasPoisonous(true);
        BoardCreature poisonCreature = BoardCreature.fromCard(poisonCard);
        poisonCreature.setHasPoisonous(true);
        poisonCreature.setCanAttack(true);
        player1.getBoard().add(poisonCreature);

        BoardCreature defender = BoardCreature.fromCard(creatureCard);
        defender.setHealth(10);
        player2.getBoard().add(defender);

        gameEngine.attack(gameState, 1L, poisonCreature.getInstanceId(), defender.getInstanceId());

        assertEquals(0, defender.getHealth());
    }

    @Test
    @DisplayName("吸血效果测试")
    void lifestealEffect_Success() {
        CardDTO lifestealCard = createCreatureCard(50L, "Lifesteal", 3, 4, 3);
        lifestealCard.setHasLifesteal(true);
        BoardCreature lifestealCreature = BoardCreature.fromCard(lifestealCard);
        lifestealCreature.setHasLifesteal(true);
        lifestealCreature.setCanAttack(true);
        player1.getBoard().add(lifestealCreature);

        player1.setHealth(25);
        int initialHealth = player1.getHealth();

        gameEngine.attack(gameState, 1L, lifestealCreature.getInstanceId(), null);

        assertEquals(initialHealth + 4, player1.getHealth());
    }

    @Test
    @DisplayName("冲锋效果测试 - 可以立即攻击")
    void chargeEffect_Success() {
        CardDTO chargeCard = createCreatureCard(50L, "Charge", 3, 3, 3);
        chargeCard.setHasCharge(true);
        player1.getHand().add(chargeCard);
        player1.setMana(10);

        gameEngine.playCard(gameState, 1L, chargeCard.getId(), null);

        BoardCreature creature = player1.getBoard().stream()
                .filter(c -> c.getCard().getId().equals(50L))
                .findFirst()
                .orElse(null);

        assertNotNull(creature);
        assertTrue(creature.isCanAttack());
    }

    @Test
    @DisplayName("风怒效果测试 - 可以攻击两次")
    void windfuryEffect_Success() {
        CardDTO windfuryCard = createCreatureCard(50L, "Windfury", 3, 2, 3);
        windfuryCard.setHasWindfury(true);
        BoardCreature windfuryCreature = BoardCreature.fromCard(windfuryCard);
        windfuryCreature.setHasWindfury(true);
        windfuryCreature.setCanAttack(true);
        player1.getBoard().add(windfuryCreature);

        gameEngine.attack(gameState, 1L, windfuryCreature.getInstanceId(), null);

        assertTrue(windfuryCreature.isCanAttack());
        assertFalse(windfuryCreature.isHasAttacked());

        gameEngine.attack(gameState, 1L, windfuryCreature.getInstanceId(), null);

        assertFalse(windfuryCreature.isCanAttack());
        assertTrue(windfuryCreature.isHasAttacked());
    }
}
