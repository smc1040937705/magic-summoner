package com.magicsummoner.game.model;

import com.magicsummoner.dto.CardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreature {
    private Long instanceId;
    private CardDTO card;
    private int attack;
    private int health;
    private int maxHealth;
    private boolean canAttack;
    private boolean hasAttacked;
    private boolean hasTaunt;
    private boolean hasCharge;
    private boolean hasStealth;
    private boolean hasDivineShield;
    private boolean hasWindfury;
    private boolean hasLifesteal;
    private boolean hasPoisonous;
    private int attackCount;
    
    private static long nextInstanceId = 1;
    
    public static BoardCreature fromCard(CardDTO card) {
        return BoardCreature.builder()
            .instanceId(nextInstanceId++)
            .card(card)
            .attack(card.getAttack() != null ? card.getAttack() : 0)
            .health(card.getHealth() != null ? card.getHealth() : 0)
            .maxHealth(card.getMaxHealth() != null ? card.getMaxHealth() : card.getHealth())
            .canAttack(card.getHasCharge() != null ? card.getHasCharge() : false)
            .hasAttacked(false)
            .hasTaunt(card.getHasTaunt() != null ? card.getHasTaunt() : false)
            .hasCharge(card.getHasCharge() != null ? card.getHasCharge() : false)
            .hasStealth(card.getHasStealth() != null ? card.getHasStealth() : false)
            .hasDivineShield(card.getHasDivineShield() != null ? card.getHasDivineShield() : false)
            .hasWindfury(card.getHasWindfury() != null ? card.getHasWindfury() : false)
            .hasLifesteal(card.getHasLifesteal() != null ? card.getHasLifesteal() : false)
            .hasPoisonous(card.getHasPoisonous() != null ? card.getHasPoisonous() : false)
            .attackCount(0)
            .build();
    }
    
    public void takeDamage(int damage) {
        if (hasDivineShield) {
            hasDivineShield = false;
            return;
        }
        health -= damage;
    }
    
    public void buffAttack(int amount) {
        attack += amount;
    }
    
    public void buffHealth(int amount) {
        maxHealth += amount;
        health += amount;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public void attack() {
        attackCount++;
        if (!hasWindfury || attackCount >= 2) {
            hasAttacked = true;
            canAttack = false;
        }
    }
    
    public void resetAttack() {
        canAttack = true;
        hasAttacked = false;
        attackCount = 0;
        if (hasStealth) {
            hasStealth = false;
        }
    }
    
    public void removeStealth() {
        hasStealth = false;
    }
}
