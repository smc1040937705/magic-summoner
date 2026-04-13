package com.magicsummoner.enums;

public enum CardRarity {
    COMMON(1, "普通", "#9E9E9E", 100, 40),
    UNCOMMON(2, "优秀", "#4CAF50", 200, 100),
    RARE(3, "稀有", "#2196F3", 500, 200),
    EPIC(4, "史诗", "#9C27B0", 1000, 400),
    LEGENDARY(5, "传说", "#FF9800", 2500, 1000),
    MYTHIC(6, "神话", "#F44336", 5000, 2500);
    
    private final int level;
    private final String displayName;
    private final String color;
    private final int goldCost;
    private final int dustCost;
    
    CardRarity(int level, String displayName, String color, int goldCost, int dustCost) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
        this.goldCost = goldCost;
        this.dustCost = dustCost;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public int getGoldCost() {
        return goldCost;
    }
    
    public int getDustCost() {
        return dustCost;
    }
    
    public double getDropRate() {
        return switch (this) {
            case COMMON -> 0.60;
            case UNCOMMON -> 0.25;
            case RARE -> 0.10;
            case EPIC -> 0.04;
            case LEGENDARY -> 0.009;
            case MYTHIC -> 0.001;
        };
    }
}
