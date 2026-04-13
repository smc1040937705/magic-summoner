package com.magicsummoner.enums;

public enum ElementType {
    FIRE("火元素", "#FF4444"),
    WATER("水元素", "#4444FF"),
    EARTH("土元素", "#8B4513"),
    WIND("风元素", "#44FF44"),
    LIGHT("光元素", "#FFD700"),
    DARK("暗元素", "#800080"),
    NEUTRAL("中立", "#808080");
    
    private final String displayName;
    private final String color;
    
    ElementType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public boolean isStrongAgainst(ElementType other) {
        return switch (this) {
            case FIRE -> other == WIND || other == EARTH;
            case WATER -> other == FIRE || other == EARTH;
            case EARTH -> other == WATER || other == WIND;
            case WIND -> other == EARTH || other == FIRE;
            case LIGHT -> other == DARK;
            case DARK -> other == LIGHT;
            default -> false;
        };
    }
}
