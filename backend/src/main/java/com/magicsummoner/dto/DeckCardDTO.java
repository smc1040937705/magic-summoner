package com.magicsummoner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckCardDTO {
    private Long id;
    private CardDTO card;
    private Integer quantity;
}
