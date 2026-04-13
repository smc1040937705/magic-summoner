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
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;
    
    @Column(name = "experience", nullable = false)
    @Builder.Default
    private Integer experience = 0;
    
    @Column(name = "gold", nullable = false)
    @Builder.Default
    private Integer gold = 500;
    
    @Column(name = "gems", nullable = false)
    @Builder.Default
    private Integer gems = 100;
    
    @Column(name = "rank_points", nullable = false)
    @Builder.Default
    private Integer rankPoints = 0;
    
    @Column(name = "wins", nullable = false)
    @Builder.Default
    private Integer wins = 0;
    
    @Column(name = "losses", nullable = false)
    @Builder.Default
    private Integer losses = 0;
    
    @Column(name = "draws", nullable = false)
    @Builder.Default
    private Integer draws = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private Boolean isOnline = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlayerCard> playerCards = new ArrayList<>();
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Deck> decks = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void addExperience(int exp) {
        this.experience += exp;
        int requiredExp = getRequiredExperienceForNextLevel();
        while (this.experience >= requiredExp) {
            this.experience -= requiredExp;
            this.level++;
            requiredExp = getRequiredExperienceForNextLevel();
        }
    }
    
    public int getRequiredExperienceForNextLevel() {
        return this.level * 100;
    }
    
    public double getWinRate() {
        int total = wins + losses;
        return total == 0 ? 0.0 : (double) wins / total * 100;
    }
}
