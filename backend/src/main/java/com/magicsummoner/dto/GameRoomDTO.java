package com.magicsummoner.dto;

import com.magicsummoner.enums.GameRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomDTO {
    private Long id;
    private String roomCode;
    private String name;
    private PlayerDTO host;
    private PlayerDTO guest;
    private GameRoomStatus status;
    private Integer maxSpectators;
    private Integer currentSpectators;
    private Boolean isPrivate;
    private Integer turnTimeLimit;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private Boolean isFull;
}
