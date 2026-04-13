package com.magicsummoner.service;

import com.magicsummoner.dto.AuthResponse;
import com.magicsummoner.dto.LoginRequest;
import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.dto.RegisterRequest;
import com.magicsummoner.entity.Player;
import com.magicsummoner.repository.PlayerRepository;
import com.magicsummoner.security.JwtTokenProvider;
import com.magicsummoner.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DtoMapper dtoMapper;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (playerRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        Player player = Player.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
            .build();
        
        Player savedPlayer = playerRepository.save(player);
        
        String token = jwtTokenProvider.generateToken(savedPlayer.getId(), savedPlayer.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .player(dtoMapper.toPlayerDTO(savedPlayer))
            .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        Player player = playerRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), player.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        player.setIsOnline(true);
        player.setLastLogin(LocalDateTime.now());
        playerRepository.save(player);
        
        String token = jwtTokenProvider.generateToken(player.getId(), player.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .player(dtoMapper.toPlayerDTO(player))
            .build();
    }
    
    @Transactional
    public void logout(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setIsOnline(false);
        playerRepository.save(player);
    }
    
    public PlayerDTO getCurrentPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        return dtoMapper.toPlayerDTO(player);
    }
}
