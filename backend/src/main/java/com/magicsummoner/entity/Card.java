package com.magicsummoner.entity;

import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false)
    private ElementType elementType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private CardRarity rarity;
    
    @Column(name = "mana_cost", nullable = false)
    @Builder.Default
    private Integer manaCost = 0;
    
    // Creature card attributes
    @Column(name = "attack")
    private Integer attack;
    
    @Column(name = "health")
    private Integer health;
    
    @Column(name = "max_health")
    private Integer maxHealth;
    
    @Column(name = "has_taunt")
    @Builder.Default
    private Boolean hasTaunt = false;
    
    @Column(name = "has_charge")
    @Builder.Default
    private Boolean hasCharge = false;
    
    @Column(name = "has_stealth")
    @Builder.Default
    private Boolean hasStealth = false;
    
    @Column(name = "has_divine_shield")
    @Builder.Default
    private Boolean hasDivineShield = false;
    
    @Column(name = "has_windfury")
    @Builder.Default
    private Boolean hasWindfury = false;
    
    @Column(name = "has_lifesteal")
    @Builder.Default
    private Boolean hasLifesteal = false;
    
    @Column(name = "has_poisonous")
    @Builder.Default
    private Boolean hasPoisonous = false;
    
    // Spell card attributes
    @Column(name = "spell_damage")
    private Integer spellDamage;
    
    @Column(name = "heal_amount")
    private Integer healAmount;
    
    @Column(name = "draw_cards")
    private Integer drawCards;
    
    @Column(name = "summon_creature_id")
    private Long summonCreatureId;
    
    @Column(name = "summon_count")
    private Integer summonCount;
    
    // Equipment card attributes
    @Column(name = "attack_bonus")
    private Integer attackBonus;
    
    @Column(name = "health_bonus")
    private Integer healthBonus;
    
    @Column(name = "equipment_slot")
    private String equipmentSlot;
    
    @Column(name = "effect_description", length = 500)
    private String effectDescription;
    
    @Column(name = "gold_cost", nullable = false)
    @Builder.Default
    private Integer goldCost = 100;
    
    @Column(name = "dust_cost", nullable = false)
    @Builder.Default
    private Integer dustCost = 40;
    
    @Column(name = "is_collectible", nullable = false)
    @Builder.Default
    private Boolean isCollectible = true;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ElementCollection
    @CollectionTable(name = "card_keywords", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isCreature() {
        return cardType == CardType.CREATURE;
    }
    
    public boolean isSpell() {
        return cardType == CardType.SPELL;
    }
    
    public boolean isEquipment() {
        return cardType == CardType.EQUIPMENT;
    }
}
