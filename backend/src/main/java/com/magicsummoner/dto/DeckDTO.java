package com.magicsummoner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckDTO {
    private Long id;
    private String name;
    private String description;
    private Integer cardCount;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeckCardDTO> cards;
}
