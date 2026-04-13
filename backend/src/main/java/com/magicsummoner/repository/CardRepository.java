package com.magicsummoner.repository;

import com.magicsummoner.entity.Card;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    List<Card> findByIsActiveTrue();
    
    List<Card> findByCardTypeAndIsActiveTrue(CardType cardType);
    
    List<Card> findByElementTypeAndIsActiveTrue(ElementType elementType);
    
    List<Card> findByRarityAndIsActiveTrue(CardRarity rarity);
    
    List<Card> findByManaCostLessThanEqualAndIsActiveTrue(Integer manaCost);
    
    @Query("SELECT c FROM Card c WHERE c.isActive = true AND c.isCollectible = true")
    List<Card> findAllCollectibleCards();
    
    @Query("SELECT c FROM Card c WHERE c.isActive = true AND " +
           "(:cardType IS NULL OR c.cardType = :cardType) AND " +
           "(:elementType IS NULL OR c.elementType = :elementType) AND " +
           "(:rarity IS NULL OR c.rarity = :rarity) AND " +
           "(:minMana IS NULL OR c.manaCost >= :minMana) AND " +
           "(:maxMana IS NULL OR c.manaCost <= :maxMana)")
    Page<Card> findByFilters(@Param("cardType") CardType cardType,
                            @Param("elementType") ElementType elementType,
                            @Param("rarity") CardRarity rarity,
                            @Param("minMana") Integer minMana,
                            @Param("maxMana") Integer maxMana,
                            Pageable pageable);
    
    @Query("SELECT c FROM Card c WHERE c.name LIKE %:keyword% AND c.isActive = true")
    Page<Card> searchByName(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT c FROM Card c WHERE c.isActive = true ORDER BY RANDOM() LIMIT :count")
    List<Card> findRandomCards(@Param("count") int count);
    
    @Query("SELECT c FROM Card c WHERE c.rarity = :rarity AND c.isActive = true")
    List<Card> findByRarityForPackOpening(@Param("rarity") CardRarity rarity);
}
