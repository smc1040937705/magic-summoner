package com.magicsummoner.service;

import com.magicsummoner.dto.*;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    private final DtoMapper dtoMapper;
    
    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        return dtoMapper.toPlayerDTO(player);
    }
    
    public PlayerDTO getPlayerByUsername(String username) {
        Player player = playerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        return dtoMapper.toPlayerDTO(player);
    }
    
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
            .map(dtoMapper::toPlayerDTO)
            .collect(Collectors.toList());
    }
    
    public List<PlayerDTO> getOnlinePlayers() {
        return playerRepository.findByIsOnlineTrue().stream()
            .map(dtoMapper::toPlayerDTO)
            .collect(Collectors.toList());
    }
    
    public List<PlayerDTO> getTopPlayers(int limit) {
        return playerRepository.findTopPlayersByRank(PageRequest.of(0, limit))
            .stream()
            .map(dtoMapper::toPlayerDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public PlayerDTO updatePlayer(Long id, PlayerDTO playerDTO) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        if (playerDTO.getDisplayName() != null) {
            player.setDisplayName(playerDTO.getDisplayName());
        }
        if (playerDTO.getAvatarUrl() != null) {
            player.setAvatarUrl(playerDTO.getAvatarUrl());
        }
        
        Player updatedPlayer = playerRepository.save(player);
        return dtoMapper.toPlayerDTO(updatedPlayer);
    }
    
    @Transactional
    public void updatePlayerOnlineStatus(Long playerId, boolean isOnline) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setIsOnline(isOnline);
        if (isOnline) {
            player.setLastLogin(LocalDateTime.now());
        }
        playerRepository.save(player);
    }
    
    @Transactional
    public void addGold(Long playerId, int amount) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setGold(player.getGold() + amount);
        playerRepository.save(player);
    }
    
    @Transactional
    public void addGems(Long playerId, int amount) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setGems(player.getGems() + amount);
        playerRepository.save(player);
    }
    
    @Transactional
    public void addExperience(Long playerId, int exp) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.addExperience(exp);
        playerRepository.save(player);
    }
    
    @Transactional
    public void updateRankPoints(Long playerId, int points) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setRankPoints(Math.max(0, player.getRankPoints() + points));
        playerRepository.save(player);
    }
    
    public int getPlayerRank(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        Long higherRanked = playerRepository.countPlayersWithHigherRank(player.getRankPoints());
        return higherRanked.intValue() + 1;
    }
}
