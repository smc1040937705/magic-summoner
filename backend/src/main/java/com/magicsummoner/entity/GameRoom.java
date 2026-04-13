package com.magicsummoner.entity;

import com.magicsummoner.enums.GameRoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_code", unique = true, nullable = false)
    private String roomCode;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Player host;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Player guest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Player winner;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GameRoomStatus status = GameRoomStatus.WAITING;
    
    @Column(name = "max_spectators")
    @Builder.Default
    private Integer maxSpectators = 10;
    
    @Column(name = "current_spectators")
    @Builder.Default
    private Integer currentSpectators = 0;
    
    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "turn_time_limit")
    @Builder.Default
    private Integer turnTimeLimit = 120;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isFull() {
        return guest != null;
    }
    
    public boolean isWaiting() {
        return status == GameRoomStatus.WAITING;
    }
    
    public boolean isPlaying() {
        return status == GameRoomStatus.PLAYING;
    }
    
    public boolean isFinished() {
        return status == GameRoomStatus.FINISHED;
    }
}
