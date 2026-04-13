package com.magicsummoner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private Integer level;
    private Integer experience;
    private Integer gold;
    private Integer gems;
    private Integer rankPoints;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Double winRate;
    private Boolean isOnline;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}
