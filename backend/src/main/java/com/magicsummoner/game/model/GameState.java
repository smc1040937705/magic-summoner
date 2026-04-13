package com.magicsummoner.game.model;

import com.magicsummoner.enums.BattleResult;
import com.magicsummoner.enums.ElementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    private Long roomId;
    private GamePlayer player1;
    private GamePlayer player2;
    private Long currentPlayerId;
    private int turnNumber;
    private LocalDateTime turnStartTime;
    private int turnTimeLimit;
    private boolean isGameOver;
    private Long winnerId;
    private BattleResult result;
    private List<GameAction> actionHistory;
    private List<String> eventLog;
    
    public static GameState create(Long roomId, GamePlayer p1, GamePlayer p2, int turnTimeLimit) {
        GameState state = GameState.builder()
            .roomId(roomId)
            .player1(p1)
            .player2(p2)
            .currentPlayerId(p1.getPlayerId())
            .turnNumber(1)
            .turnStartTime(LocalDateTime.now())
            .turnTimeLimit(turnTimeLimit)
            .isGameOver(false)
            .actionHistory(new ArrayList<>())
            .eventLog(new ArrayList<>())
            .build();
        
        p1.drawInitialCards(3);
        p2.drawInitialCards(4);
        p1.startTurn();
        
        state.addEvent("游戏开始！" + p1.getUsername() + " 先手");
        
        return state;
    }
    
    public GamePlayer getCurrentPlayer() {
        return currentPlayerId.equals(player1.getPlayerId()) ? player1 : player2;
    }
    
    public GamePlayer getOpponent() {
        return currentPlayerId.equals(player1.getPlayerId()) ? player2 : player1;
    }
    
    public GamePlayer getPlayerById(Long playerId) {
        return player1.getPlayerId().equals(playerId) ? player1 : player2;
    }
    
    public void endTurn() {
        GamePlayer current = getCurrentPlayer();
        GamePlayer opponent = getOpponent();
        
        currentPlayerId = opponent.getPlayerId();
        turnNumber++;
        turnStartTime = LocalDateTime.now();
        
        opponent.startTurn();
        
        addEvent(opponent.getUsername() + " 的回合开始");
    }
    
    public void checkGameOver() {
        if (player1.isDead()) {
            isGameOver = true;
            winnerId = player2.getPlayerId();
            result = BattleResult.PLAYER2_WIN;
            addEvent(player2.getUsername() + " 获胜！");
        } else if (player2.isDead()) {
            isGameOver = true;
            winnerId = player1.getPlayerId();
            result = BattleResult.PLAYER1_WIN;
            addEvent(player1.getUsername() + " 获胜！");
        }
    }
    
    public void addEvent(String event) {
        eventLog.add(event);
    }
    
    public void addAction(GameAction action) {
        actionHistory.add(action);
    }
    
    public boolean isPlayerTurn(Long playerId) {
        return currentPlayerId.equals(playerId);
    }
    
    public int getRemainingTime() {
        if (turnStartTime == null) return turnTimeLimit;
        int elapsed = (int) java.time.Duration.between(turnStartTime, LocalDateTime.now()).getSeconds();
        return Math.max(0, turnTimeLimit - elapsed);
    }
    
    public boolean isTimeUp() {
        return getRemainingTime() <= 0;
    }
}
