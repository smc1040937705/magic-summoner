package com.magicsummoner.controller;

import com.magicsummoner.dto.AuthResponse;
import com.magicsummoner.dto.LoginRequest;
import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.dto.RegisterRequest;
import com.magicsummoner.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestAttribute Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<PlayerDTO> getCurrentUser(@RequestAttribute Long userId) {
        return ResponseEntity.ok(authService.getCurrentPlayer(userId));
    }
}
