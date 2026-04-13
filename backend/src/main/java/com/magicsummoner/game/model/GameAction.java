package com.magicsummoner.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameAction {
    private Long id;
    private Long playerId;
    private ActionType type;
    private Long cardId;
    private Long targetId;
    private Long attackerId;
    private Integer damage;
    private Integer heal;
    private String description;
    private LocalDateTime timestamp;
    
    public enum ActionType {
        PLAY_CARD,
        ATTACK,
        USE_ABILITY,
        END_TURN,
        SURRENDER,
        SUMMON_CREATURE,
        CAST_SPELL,
        EQUIP_ITEM,
        DRAW_CARD
    }
    
    public static GameAction playCard(Long playerId, Long cardId, String cardName) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.PLAY_CARD)
            .cardId(cardId)
            .description("打出卡牌: " + cardName)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static GameAction attack(Long playerId, Long attackerId, Long targetId, int damage) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.ATTACK)
            .attackerId(attackerId)
            .targetId(targetId)
            .damage(damage)
            .description("造成 " + damage + " 点伤害")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static GameAction summonCreature(Long playerId, Long cardId, String creatureName) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.SUMMON_CREATURE)
            .cardId(cardId)
            .description("召唤生物: " + creatureName)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static GameAction castSpell(Long playerId, Long cardId, String spellName) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.CAST_SPELL)
            .cardId(cardId)
            .description("施放法术: " + spellName)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static GameAction endTurn(Long playerId) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.END_TURN)
            .description("结束回合")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static GameAction surrender(Long playerId) {
        return GameAction.builder()
            .playerId(playerId)
            .type(ActionType.SURRENDER)
            .description("投降")
            .timestamp(LocalDateTime.now())
            .build();
    }
}
