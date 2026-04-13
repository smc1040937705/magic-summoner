package com.magicsummoner.entity;

import com.magicsummoner.enums.BattleResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "battle_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    private Player player2;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Player winner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private GameRoom room;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private BattleResult result;
    
    @Column(name = "player1_health")
    private Integer player1Health;
    
    @Column(name = "player2_health")
    private Integer player2Health;
    
    @Column(name = "turns_count")
    private Integer turnsCount;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "player1_rank_change")
    private Integer player1RankChange;
    
    @Column(name = "player2_rank_change")
    private Integer player2RankChange;
    
    @Column(name = "player1_exp_gained")
    private Integer player1ExpGained;
    
    @Column(name = "player2_exp_gained")
    private Integer player2ExpGained;
    
    @Column(name = "player1_gold_gained")
    private Integer player1GoldGained;
    
    @Column(name = "player2_gold_gained")
    private Integer player2GoldGained;
    
    @Column(name = "replay_data", length = 10000)
    private String replayData;
    
    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
    
    @PrePersist
    protected void onCreate() {
        playedAt = LocalDateTime.now();
    }
}
