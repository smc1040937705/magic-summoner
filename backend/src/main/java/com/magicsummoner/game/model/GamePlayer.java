package com.magicsummoner.game.model;

import com.magicsummoner.dto.CardDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePlayer {
    private Long playerId;
    private String username;
    private int health;
    private int maxHealth;
    private int mana;
    private int maxMana;
    private List<CardDTO> deck;
    private List<CardDTO> hand;
    private List<BoardCreature> board;
    private List<CardDTO> graveyard;
    private int cardsDrawnThisTurn;
    private boolean hasPlayedCardThisTurn;
    private boolean hasAttackedThisTurn;
    
    public static GamePlayer create(Long playerId, String username, List<CardDTO> deck) {
        return GamePlayer.builder()
            .playerId(playerId)
            .username(username)
            .health(30)
            .maxHealth(30)
            .mana(0)
            .maxMana(0)
            .deck(new ArrayList<>(deck))
            .hand(new ArrayList<>())
            .board(new ArrayList<>())
            .graveyard(new ArrayList<>())
            .cardsDrawnThisTurn(0)
            .hasPlayedCardThisTurn(false)
            .hasAttackedThisTurn(false)
            .build();
    }
    
    public void drawCard() {
        if (!deck.isEmpty() && hand.size() < 10) {
            CardDTO card = deck.remove(0);
            hand.add(card);
            cardsDrawnThisTurn++;
        }
    }
    
    public void drawInitialCards(int count) {
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            drawCard();
        }
    }
    
    public void startTurn() {
        maxMana = Math.min(maxMana + 1, 10);
        mana = maxMana;
        cardsDrawnThisTurn = 0;
        hasPlayedCardThisTurn = false;
        hasAttackedThisTurn = false;
        drawCard();
        
        for (BoardCreature creature : board) {
            creature.setCanAttack(true);
            creature.setHasAttacked(false);
        }
    }
    
    public boolean canPlayCard(CardDTO card) {
        return mana >= card.getManaCost() && hand.contains(card);
    }
    
    public void playCard(CardDTO card) {
        if (canPlayCard(card)) {
            mana -= card.getManaCost();
            hand.remove(card);
            hasPlayedCardThisTurn = true;
        }
    }
    
    public void summonCreature(CardDTO card, BoardCreature creature) {
        if (board.size() < 5) {
            board.add(creature);
        }
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }
    
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public void addToGraveyard(CardDTO card) {
        graveyard.add(card);
    }
    
    public void removeFromBoard(BoardCreature creature) {
        board.remove(creature);
        if (creature.getCard() != null) {
            addToGraveyard(creature.getCard());
        }
    }
}
