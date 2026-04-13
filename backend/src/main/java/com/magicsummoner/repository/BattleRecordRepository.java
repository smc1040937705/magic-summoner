package com.magicsummoner.repository;

import com.magicsummoner.entity.BattleRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BattleRecordRepository extends JpaRepository<BattleRecord, Long> {
    
    List<BattleRecord> findByPlayer1IdOrPlayer2IdOrderByPlayedAtDesc(Long player1Id, Long player2Id);
    
    Page<BattleRecord> findByPlayer1IdOrPlayer2IdOrderByPlayedAtDesc(Long player1Id, Long player2Id, Pageable pageable);
    
    @Query("SELECT br FROM BattleRecord br WHERE br.winner.id = :playerId ORDER BY br.playedAt DESC")
    List<BattleRecord> findWinsByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT br FROM BattleRecord br WHERE (br.player1.id = :playerId OR br.player2.id = :playerId) AND br.winner.id != :playerId ORDER BY br.playedAt DESC")
    List<BattleRecord> findLossesByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT COUNT(br) FROM BattleRecord br WHERE br.winner.id = :playerId")
    Long countWinsByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT COUNT(br) FROM BattleRecord br WHERE (br.player1.id = :playerId OR br.player2.id = :playerId) AND br.winner.id != :playerId")
    Long countLossesByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT br FROM BattleRecord br WHERE br.playedAt >= :startDate ORDER BY br.playedAt DESC")
    List<BattleRecord> findRecentBattles(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT br FROM BattleRecord br WHERE br.room.id = :roomId ORDER BY br.playedAt DESC")
    List<BattleRecord> findByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT AVG(br.durationSeconds) FROM BattleRecord br WHERE br.player1.id = :playerId OR br.player2.id = :playerId")
    Double calculateAverageBattleDuration(@Param("playerId") Long playerId);
}
