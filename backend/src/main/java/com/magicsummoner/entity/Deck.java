package com.magicsummoner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "wins", nullable = false)
    @Builder.Default
    private Integer wins = 0;
    
    @Column(name = "losses", nullable = false)
    @Builder.Default
    private Integer losses = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DeckCard> deckCards = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public int getCardCount() {
        return deckCards.stream().mapToInt(DeckCard::getQuantity).sum();
    }
    
    public void addCard(Card card, int quantity) {
        DeckCard existingCard = deckCards.stream()
            .filter(dc -> dc.getCard().getId().equals(card.getId()))
            .findFirst()
            .orElse(null);
        
        if (existingCard != null) {
            existingCard.setQuantity(Math.min(existingCard.getQuantity() + quantity, 2));
        } else {
            DeckCard deckCard = DeckCard.builder()
                .deck(this)
                .card(card)
                .quantity(quantity)
                .build();
            deckCards.add(deckCard);
        }
    }
    
    public void removeCard(Long cardId) {
        deckCards.removeIf(dc -> dc.getCard().getId().equals(cardId));
    }
    
    public boolean isValid() {
        int totalCards = getCardCount();
        return totalCards >= 30 && totalCards <= 40;
    }
}
