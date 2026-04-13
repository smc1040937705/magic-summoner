package com.magicsummoner.controller;

import com.magicsummoner.dto.PlayerDTO;
import com.magicsummoner.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {
    
    private final PlayerService playerService;
    
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<PlayerDTO> getPlayerByUsername(@PathVariable String username) {
        return ResponseEntity.ok(playerService.getPlayerByUsername(username));
    }
    
    @GetMapping("/online")
    public ResponseEntity<List<PlayerDTO>> getOnlinePlayers() {
        return ResponseEntity.ok(playerService.getOnlinePlayers());
    }
    
    @GetMapping("/top")
    public ResponseEntity<List<PlayerDTO>> getTopPlayers(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(playerService.getTopPlayers(limit));
    }
    
    @GetMapping("/rank/{id}")
    public ResponseEntity<Integer> getPlayerRank(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerRank(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PlayerDTO> updatePlayer(@PathVariable Long id, @RequestBody PlayerDTO playerDTO) {
        return ResponseEntity.ok(playerService.updatePlayer(id, playerDTO));
    }
    
    @PostMapping("/{id}/gold")
    public ResponseEntity<Void> addGold(@PathVariable Long id, @RequestParam int amount) {
        playerService.addGold(id, amount);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/gems")
    public ResponseEntity<Void> addGems(@PathVariable Long id, @RequestParam int amount) {
        playerService.addGems(id, amount);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/experience")
    public ResponseEntity<Void> addExperience(@PathVariable Long id, @RequestParam int amount) {
        playerService.addExperience(id, amount);
        return ResponseEntity.ok().build();
    }
}
