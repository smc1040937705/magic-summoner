package com.magicsummoner.enums;

public enum BattleResult {
    PLAYER1_WIN("玩家1胜利"),
    PLAYER2_WIN("玩家2胜利"),
    DRAW("平局"),
    PLAYER1_SURRENDER("玩家1投降"),
    PLAYER2_SURRENDER("玩家2投降"),
    TIMEOUT("超时"),
    DISCONNECT("断开连接");
    
    private final String displayName;
    
    BattleResult(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
