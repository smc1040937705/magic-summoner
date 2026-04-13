package com.magicsummoner.enums;

public enum GameRoomStatus {
    WAITING("等待中"),
    PLAYING("游戏中"),
    PAUSED("暂停"),
    FINISHED("已结束"),
    ABANDONED("已放弃");
    
    private final String displayName;
    
    GameRoomStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
