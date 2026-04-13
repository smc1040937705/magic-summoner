package com.magicsummoner.game;

import com.magicsummoner.dto.CardDTO;
import com.magicsummoner.enums.BattleResult;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.game.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GameEngine {

    public void playCard(GameState state, Long playerId, Long cardId, Long targetId) {
        if (!state.isPlayerTurn(playerId)) {
            throw new RuntimeException("Not your turn");
        }

        GamePlayer player = state.getPlayerById(playerId);
        CardDTO card = findCardInHand(player, cardId);

        if (card == null) {
            throw new RuntimeException("Card not found in hand");
        }

        if (!player.canPlayCard(card)) {
            throw new RuntimeException("Not enough mana");
        }

        player.playCard(card);

        switch (card.getCardType()) {
            case CREATURE -> summonCreature(state, player, card);
            case SPELL -> castSpell(state, player, card, targetId);
            case EQUIPMENT -> equipItem(state, player, card, targetId);
        }

        state.addAction(GameAction.playCard(playerId, cardId, card.getName()));
        state.addEvent(player.getUsername() + " 使用了 " + card.getName());
    }

    private void summonCreature(GameState state, GamePlayer player, CardDTO card) {
        if (player.getBoard().size() >= 5) {
            throw new RuntimeException("Board is full");
        }

        BoardCreature creature = BoardCreature.fromCard(card);
        player.summonCreature(card, creature);

        state.addAction(GameAction.summonCreature(player.getPlayerId(), card.getId(), card.getName()));

        if (card.getHasCharge() != null && card.getHasCharge()) {
            creature.setCanAttack(true);
        }
    }

    private void castSpell(GameState state, GamePlayer player, CardDTO card, Long targetId) {
        state.addAction(GameAction.castSpell(player.getPlayerId(), card.getId(), card.getName()));

        if (card.getSpellDamage() != null && card.getSpellDamage() > 0) {
            int damage = card.getSpellDamage();
            GamePlayer opponent = state.getOpponent();

            if (targetId != null) {
                BoardCreature target = findCreatureOnBoard(opponent, targetId);
                if (target != null) {
                    target.takeDamage(damage);
                    state.addEvent(card.getName() + " 对 " + target.getCard().getName() + " 造成 " + damage + " 点伤害");

                    if (target.isDead()) {
                        opponent.removeFromBoard(target);
                        state.addEvent(target.getCard().getName() + " 被消灭了");
                    }
                }
            } else {
                opponent.takeDamage(damage);
                state.addEvent(card.getName() + " 对敌方英雄造成 " + damage + " 点伤害");
            }
        }

        if (card.getHealAmount() != null && card.getHealAmount() > 0) {
            player.heal(card.getHealAmount());
            state.addEvent(card.getName() + " 恢复了 " + card.getHealAmount() + " 点生命值");
        }

        if (card.getDrawCards() != null && card.getDrawCards() > 0) {
            for (int i = 0; i < card.getDrawCards(); i++) {
                player.drawCard();
            }
            state.addEvent(card.getName() + " 抽取了 " + card.getDrawCards() + " 张卡牌");
        }

        player.addToGraveyard(card);
        state.checkGameOver();
    }

    private void equipItem(GameState state, GamePlayer player, CardDTO card, Long targetId) {
        if (targetId == null) {
            throw new RuntimeException("Equipment needs a target creature");
        }

        BoardCreature target = findCreatureOnBoard(player, targetId);
        if (target == null) {
            throw new RuntimeException("Target creature not found");
        }

        if (card.getAttackBonus() != null) {
            target.buffAttack(card.getAttackBonus());
        }
        if (card.getHealthBonus() != null) {
            target.buffHealth(card.getHealthBonus());
        }

        player.addToGraveyard(card);
        state.addEvent(target.getCard().getName() + " 装备了 " + card.getName());
    }

    public void attack(GameState state, Long playerId, Long attackerId, Long targetId) {
        if (!state.isPlayerTurn(playerId)) {
            throw new RuntimeException("Not your turn");
        }

        GamePlayer player = state.getPlayerById(playerId);
        GamePlayer opponent = state.getOpponent();

        BoardCreature attacker = findCreatureOnBoard(player, attackerId);
        if (attacker == null) {
            throw new RuntimeException("Attacker not found");
        }

        if (!attacker.isCanAttack()) {
            throw new RuntimeException("Creature cannot attack");
        }

        if (targetId == null) {
            attackHero(state, player, opponent, attacker);
        } else {
            BoardCreature target = findCreatureOnBoard(opponent, targetId);
            if (target == null) {
                throw new RuntimeException("Target not found");
            }
            attackCreature(state, player, opponent, attacker, target);
        }

        attacker.attack();
        state.checkGameOver();
    }

    private void attackHero(GameState state, GamePlayer player, GamePlayer opponent, BoardCreature attacker) {
        boolean hasTaunt = opponent.getBoard().stream().anyMatch(BoardCreature::isHasTaunt);
        if (hasTaunt) {
            throw new RuntimeException("Must attack taunt creature first");
        }

        int damage = attacker.getAttack();
        opponent.takeDamage(damage);

        if (attacker.isHasLifesteal()) {
            player.heal(damage);
        }

        state.addAction(GameAction.attack(player.getPlayerId(), attacker.getInstanceId(), null, damage));
        state.addEvent(attacker.getCard().getName() + " 攻击了敌方英雄，造成 " + damage + " 点伤害");

        attacker.removeStealth();
    }

    private void attackCreature(GameState state, GamePlayer player, GamePlayer opponent,
                               BoardCreature attacker, BoardCreature target) {
        if (target.isHasStealth()) {
            throw new RuntimeException("Cannot attack stealth creature");
        }

        int attackerDamage = attacker.getAttack();
        int targetDamage = target.getAttack();

        if (attacker.isHasPoisonous()) {
            target.setHealth(0);
        } else {
            target.takeDamage(attackerDamage);
        }

        if (target.isHasPoisonous()) {
            attacker.setHealth(0);
        } else if (!attacker.isHasDivineShield()) {
            attacker.takeDamage(targetDamage);
        }

        if (attacker.isHasLifesteal()) {
            player.heal(attackerDamage);
        }

        state.addAction(GameAction.attack(player.getPlayerId(), attacker.getInstanceId(),
            target.getInstanceId(), attackerDamage));
        state.addEvent(attacker.getCard().getName() + " 攻击了 " + target.getCard().getName());

        if (target.isDead()) {
            opponent.removeFromBoard(target);
            state.addEvent(target.getCard().getName() + " 被消灭了");
        }

        if (attacker.isDead()) {
            player.removeFromBoard(attacker);
            state.addEvent(attacker.getCard().getName() + " 被消灭了");
        }

        attacker.removeStealth();
    }

    public void endTurn(GameState state, Long playerId) {
        if (!state.isPlayerTurn(playerId)) {
            throw new RuntimeException("Not your turn");
        }

        state.addAction(GameAction.endTurn(playerId));
        state.addEvent(state.getPlayerById(playerId).getUsername() + " 结束了回合");
        state.endTurn();
    }

    public void surrender(GameState state, Long playerId) {
        GamePlayer player = state.getPlayerById(playerId);
        GamePlayer opponent = state.getOpponent();

        state.setGameOver(true);
        state.setWinnerId(opponent.getPlayerId());
        state.setResult(playerId.equals(state.getPlayer1().getPlayerId()) ?
            BattleResult.PLAYER1_SURRENDER : BattleResult.PLAYER2_SURRENDER);

        state.addAction(GameAction.surrender(playerId));
        state.addEvent(player.getUsername() + " 投降了");
    }

    private CardDTO findCardInHand(GamePlayer player, Long cardId) {
        return player.getHand().stream()
            .filter(c -> c.getId().equals(cardId))
            .findFirst()
            .orElse(null);
    }

    private BoardCreature findCreatureOnBoard(GamePlayer player, Long instanceId) {
        return player.getBoard().stream()
            .filter(c -> c.getInstanceId().equals(instanceId))
            .findFirst()
            .orElse(null);
    }
}
