package com.magicsummoner.repository;

import com.magicsummoner.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByUsername(String username);
    
    Optional<Player> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<Player> findByIsOnlineTrue();
    
    @Query("SELECT p FROM Player p WHERE p.isActive = true ORDER BY p.rankPoints DESC")
    List<Player> findTopPlayersByRank(Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.username LIKE %:keyword% AND p.isActive = true")
    Page<Player> searchByUsername(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.rankPoints > :rankPoints")
    Long countPlayersWithHigherRank(@Param("rankPoints") Integer rankPoints);
    
    @Query("SELECT p FROM Player p WHERE p.isOnline = true AND p.id != :playerId")
    List<Player> findOnlinePlayersExcluding(@Param("playerId") Long playerId);
}
