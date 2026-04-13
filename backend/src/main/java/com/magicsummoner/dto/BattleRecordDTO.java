package com.magicsummoner.dto;

import com.magicsummoner.enums.BattleResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRecordDTO {
    private Long id;
    private PlayerDTO player1;
    private PlayerDTO player2;
    private PlayerDTO winner;
    private BattleResult result;
    private Integer player1Health;
    private Integer player2Health;
    private Integer turnsCount;
    private Integer durationSeconds;
    private Integer player1RankChange;
    private Integer player2RankChange;
    private Integer player1ExpGained;
    private Integer player2ExpGained;
    private Integer player1GoldGained;
    private Integer player2GoldGained;
    private LocalDateTime playedAt;
}
