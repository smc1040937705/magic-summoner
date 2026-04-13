package com.magicsummoner.repository;

import com.magicsummoner.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    
    List<Deck> findByPlayerId(Long playerId);
    
    List<Deck> findByPlayerIdAndIsActiveTrue(Long playerId);
    
    Optional<Deck> findByIdAndPlayerId(Long id, Long playerId);
    
    @Query("SELECT d FROM Deck d LEFT JOIN FETCH d.deckCards dc LEFT JOIN FETCH dc.card WHERE d.id = :deckId")
    Optional<Deck> findByIdWithCards(@Param("deckId") Long deckId);
    
    @Query("SELECT d FROM Deck d WHERE d.player.id = :playerId AND d.isActive = true ORDER BY d.wins DESC")
    List<Deck> findBestDecksByPlayerId(@Param("playerId") Long playerId);
    
    long countByPlayerId(Long playerId);
    
    @Query("SELECT d FROM Deck d WHERE d.isActive = true ORDER BY (d.wins * 1.0 / NULLIF(d.wins + d.losses, 0)) DESC")
    List<Deck> findTopDecksByWinRate(org.springframework.data.domain.Pageable pageable);
}
