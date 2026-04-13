package com.magicsummoner.enums;

public enum CardType {
    CREATURE("生物卡"),
    SPELL("法术卡"),
    EQUIPMENT("装备卡");
    
    private final String displayName;
    
    CardType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
