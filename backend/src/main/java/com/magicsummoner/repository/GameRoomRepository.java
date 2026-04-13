package com.magicsummoner.repository;

import com.magicsummoner.entity.GameRoom;
import com.magicsummoner.enums.GameRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    
    Optional<GameRoom> findByRoomCode(String roomCode);
    
    List<GameRoom> findByStatus(GameRoomStatus status);
    
    List<GameRoom> findByStatusAndIsPrivateFalse(GameRoomStatus status);
    
    List<GameRoom> findByHostIdOrGuestId(Long hostId, Long guestId);
    
    @Query("SELECT gr FROM GameRoom gr WHERE gr.status = 'WAITING' AND gr.isPrivate = false")
    List<GameRoom> findAvailablePublicRooms();
    
    @Query("SELECT gr FROM GameRoom gr WHERE (gr.host.id = :playerId OR gr.guest.id = :playerId) AND gr.status = 'PLAYING'")
    Optional<GameRoom> findActiveGameByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT gr FROM GameRoom gr WHERE gr.createdAt < :time AND gr.status = 'WAITING'")
    List<GameRoom> findStaleWaitingRooms(@Param("time") LocalDateTime time);
    
    @Query("SELECT gr FROM GameRoom gr WHERE gr.status = 'PLAYING' AND gr.startedAt < :time")
    List<GameRoom> findLongRunningGames(@Param("time") LocalDateTime time);
    
    boolean existsByRoomCode(String roomCode);
    
    long countByStatus(GameRoomStatus status);
}
