package com.magicsummoner.repository;

import com.magicsummoner.entity.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerCardRepository extends JpaRepository<PlayerCard, Long> {
    
    List<PlayerCard> findByPlayerId(Long playerId);
    
    Optional<PlayerCard> findByPlayerIdAndCardId(Long playerId, Long cardId);
    
    @Query("SELECT pc FROM PlayerCard pc JOIN FETCH pc.card WHERE pc.player.id = :playerId")
    List<PlayerCard> findByPlayerIdWithCard(@Param("playerId") Long playerId);
    
    @Query("SELECT SUM(pc.quantity) FROM PlayerCard pc WHERE pc.player.id = :playerId")
    Long countTotalCardsByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT pc FROM PlayerCard pc WHERE pc.player.id = :playerId AND pc.isFavorite = true")
    List<PlayerCard> findFavoriteCardsByPlayerId(@Param("playerId") Long playerId);
    
    boolean existsByPlayerIdAndCardId(Long playerId, Long cardId);
}
