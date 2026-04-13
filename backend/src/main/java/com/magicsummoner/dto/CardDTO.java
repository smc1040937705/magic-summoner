package com.magicsummoner.dto;

import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private CardType cardType;
    private ElementType elementType;
    private CardRarity rarity;
    private Integer manaCost;
    private Integer attack;
    private Integer health;
    private Integer maxHealth;
    private Boolean hasTaunt;
    private Boolean hasCharge;
    private Boolean hasStealth;
    private Boolean hasDivineShield;
    private Boolean hasWindfury;
    private Boolean hasLifesteal;
    private Boolean hasPoisonous;
    private Integer spellDamage;
    private Integer healAmount;
    private Integer drawCards;
    private Long summonCreatureId;
    private Integer summonCount;
    private Integer attackBonus;
    private Integer healthBonus;
    private String equipmentSlot;
    private String effectDescription;
    private Integer goldCost;
    private Integer dustCost;
    private List<String> keywords;
}
