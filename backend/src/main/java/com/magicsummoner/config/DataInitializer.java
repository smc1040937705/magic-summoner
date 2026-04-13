package com.magicsummoner.config;

import com.magicsummoner.entity.Card;
import com.magicsummoner.entity.Player;
import com.magicsummoner.enums.CardRarity;
import com.magicsummoner.enums.CardType;
import com.magicsummoner.enums.ElementType;
import com.magicsummoner.repository.CardRepository;
import com.magicsummoner.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final PlayerRepository playerRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (playerRepository.count() == 0) {
            initializePlayers();
        }
        
        if (cardRepository.count() == 0) {
            initializeCards();
        }
        
        log.info("Data initialization completed");
    }
    
    private void initializePlayers() {
        Player player1 = Player.builder()
            .username("player1")
            .password(passwordEncoder.encode("123456"))
            .email("player1@example.com")
            .displayName("玩家一")
            .gold(500)
            .gems(100)
            .build();
        playerRepository.save(player1);
        
        Player player2 = Player.builder()
            .username("player2")
            .password(passwordEncoder.encode("123456"))
            .email("player2@example.com")
            .displayName("玩家二")
            .gold(500)
            .gems(100)
            .build();
        playerRepository.save(player2);
        
        log.info("Created demo players: player1, player2");
    }
    
    private void initializeCards() {
        List<Card> cards = Arrays.asList(
            Card.builder()
                .name("火焰精灵")
                .description("身材小巧的火元素生物")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.COMMON)
                .manaCost(1)
                .attack(2)
                .health(1)
                .maxHealth(1)
                .goldCost(50)
                .dustCost(20)
                .build(),
            Card.builder()
                .name("烈焰凤凰")
                .description("强大的火凤凰，具有重生能力")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.LEGENDARY)
                .manaCost(5)
                .attack(4)
                .health(4)
                .maxHealth(4)
                .hasTaunt(false)
                .hasCharge(true)
                .hasLifesteal(true)
                .goldCost(2500)
                .dustCost(1000)
                .build(),
            Card.builder()
                .name("水元素")
                .description("由水构成的元素生物")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.WATER)
                .rarity(CardRarity.COMMON)
                .manaCost(2)
                .attack(2)
                .health(3)
                .maxHealth(3)
                .goldCost(50)
                .dustCost(20)
                .build(),
            Card.builder()
                .name("冰霜巨龙")
                .description("掌控冰霜之力的巨兽")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.WATER)
                .rarity(CardRarity.LEGENDARY)
                .manaCost(7)
                .attack(8)
                .health(8)
                .maxHealth(8)
                .hasTaunt(true)
                .hasWindfury(true)
                .goldCost(2500)
                .dustCost(1000)
                .build(),
            Card.builder()
                .name("岩甲巨兽")
                .description("披着厚重岩石铠甲的巨兽")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.EARTH)
                .rarity(CardRarity.RARE)
                .manaCost(4)
                .attack(3)
                .health(6)
                .maxHealth(6)
                .hasTaunt(true)
                .goldCost(500)
                .dustCost(200)
                .build(),
            Card.builder()
                .name("风之鹰")
                .description("迅捷的风之使者")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.WIND)
                .rarity(CardRarity.UNCOMMON)
                .manaCost(3)
                .attack(4)
                .health(2)
                .maxHealth(2)
                .hasCharge(true)
                .goldCost(200)
                .dustCost(100)
                .build(),
            Card.builder()
                .name("圣光骑士")
                .description("代表光明正义的圣殿骑士")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.LIGHT)
                .rarity(CardRarity.RARE)
                .manaCost(4)
                .attack(4)
                .health(4)
                .maxHealth(4)
                .hasDivineShield(true)
                .goldCost(500)
                .dustCost(200)
                .build(),
            Card.builder()
                .name("暗影刺客")
                .description("潜伏在阴影中的杀手")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.DARK)
                .rarity(CardRarity.EPIC)
                .manaCost(5)
                .attack(6)
                .health(3)
                .maxHealth(3)
                .hasStealth(true)
                .hasPoisonous(true)
                .goldCost(1000)
                .dustCost(400)
                .build(),
            Card.builder()
                .name("火球术")
                .description("释放炽热的火球攻击敌人")
                .cardType(CardType.SPELL)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.COMMON)
                .manaCost(2)
                .spellDamage(3)
                .goldCost(50)
                .dustCost(20)
                .build(),
            Card.builder()
                .name("冰封术")
                .description("冰冻敌人")
                .cardType(CardType.SPELL)
                .elementType(ElementType.WATER)
                .rarity(CardRarity.UNCOMMON)
                .manaCost(3)
                .spellDamage(4)
                .goldCost(200)
                .dustCost(100)
                .build(),
            Card.builder()
                .name("治疗术")
                .description("恢复生命值")
                .cardType(CardType.SPELL)
                .elementType(ElementType.LIGHT)
                .rarity(CardRarity.COMMON)
                .manaCost(2)
                .healAmount(5)
                .goldCost(50)
                .dustCost(20)
                .build(),
            Card.builder()
                .name("暗影箭")
                .description("暗影能量的箭矢")
                .cardType(CardType.SPELL)
                .elementType(ElementType.DARK)
                .rarity(CardRarity.RARE)
                .manaCost(4)
                .spellDamage(6)
                .goldCost(500)
                .dustCost(200)
                .build(),
            Card.builder()
                .name("抽牌")
                .description("抽取额外卡牌")
                .cardType(CardType.SPELL)
                .elementType(ElementType.NEUTRAL)
                .rarity(CardRarity.UNCOMMON)
                .manaCost(1)
                .drawCards(2)
                .goldCost(200)
                .dustCost(100)
                .build(),
            Card.builder()
                .name("召唤石")
                .description("从卡牌中随机召唤一个生物")
                .cardType(CardType.SPELL)
                .elementType(ElementType.NEUTRAL)
                .rarity(CardRarity.EPIC)
                .manaCost(5)
                .summonCount(2)
                .goldCost(1000)
                .dustCost(400)
                .build(),
            Card.builder()
                .name("火焰之剑")
                .description("附有火焰的武器")
                .cardType(CardType.EQUIPMENT)
                .elementType(ElementType.FIRE)
                .rarity(CardRarity.UNCOMMON)
                .manaCost(3)
                .attackBonus(3)
                .healthBonus(0)
                .equipmentSlot("weapon")
                .goldCost(200)
                .dustCost(100)
                .build(),
            Card.builder()
                .name("神圣护盾")
                .description("神圣力量加持的护盾")
                .cardType(CardType.EQUIPMENT)
                .elementType(ElementType.LIGHT)
                .rarity(CardRarity.RARE)
                .manaCost(4)
                .attackBonus(0)
                .healthBonus(4)
                .equipmentSlot("armor")
                .goldCost(500)
                .dustCost(200)
                .build(),
            Card.builder()
                .name("精灵龙")
                .description("强大的精灵龙")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.NEUTRAL)
                .rarity(CardRarity.MYTHIC)
                .manaCost(8)
                .attack(10)
                .health(10)
                .maxHealth(10)
                .hasTaunt(true)
                .hasCharge(true)
                .hasDivineShield(true)
                .hasLifesteal(true)
                .goldCost(5000)
                .dustCost(2500)
                .build(),
            Card.builder()
                .name("小精灵")
                .description("最小的元素生物")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.NEUTRAL)
                .rarity(CardRarity.COMMON)
                .manaCost(0)
                .attack(1)
                .health(1)
                .maxHealth(1)
                .goldCost(50)
                .dustCost(20)
                .build(),
            Card.builder()
                .name("大地之熊")
                .description(" Sturdy earth creature")
                .cardType(CardType.CREATURE)
                .elementType(ElementType.EARTH)
                .rarity(CardRarity.UNCOMMON)
                .manaCost(3)
                .attack(3)
                .health(4)
                .maxHealth(4)
                .hasTaunt(true)
                .goldCost(200)
                .dustCost(100)
                .build(),
            Card.builder()
                .name("闪电风暴")
                .description(" powerful storm spell")
                .cardType(CardType.SPELL)
                .elementType(ElementType.WIND)
                .rarity(CardRarity.EPIC)
                .manaCost(6)
                .spellDamage(5)
                .drawCards(1)
                .goldCost(1000)
                .dustCost(400)
                .build()
        );
        
        cardRepository.saveAll(cards);
        log.info("Created {} cards", cards.size());
    }
}
