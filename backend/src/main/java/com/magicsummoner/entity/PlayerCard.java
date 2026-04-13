package com.magicsummoner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
    
    @Column(name = "is_favorite")
    @Builder.Default
    private Boolean isFavorite = false;
    
    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;
    
    @PrePersist
    protected void onCreate() {
        acquiredAt = LocalDateTime.now();
    }
}
