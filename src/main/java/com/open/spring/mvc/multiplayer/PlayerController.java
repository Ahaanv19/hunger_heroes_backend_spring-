package com.open.spring.mvc.multiplayer;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@CrossOrigin
public class PlayerController {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PersonJpaRepository personRepository;

    // Get current logged-in user and add to player table
    @GetMapping("/current")
    public Player getCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        Person person = personRepository.findByUid(currentUsername);
        
        if (person == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if player already exists
        Player player = playerRepository.findByUid(person.getUid())
            .orElse(new Player());
        
        // Update or create player with just username
        player.setUid(person.getUid());
        player.setStatus("online");
        player.setLastActive(LocalDateTime.now());
        if (player.getConnectedAt() == null) {
            player.setConnectedAt(LocalDateTime.now());
        }
        
        return playerRepository.save(player);
    }

    @PostMapping("/connect")
    public Player connect(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        
        Person person = personRepository.findByUid(uid);
        if (person == null) {
            throw new RuntimeException("User not found");
        }
        
        Player player = playerRepository.findByUid(uid)
            .orElse(new Player());
        
        player.setUid(uid);
        player.setStatus("online");
        player.setLastActive(LocalDateTime.now());
        if (player.getConnectedAt() == null) {
            player.setConnectedAt(LocalDateTime.now());
        }
        
        return playerRepository.save(player);
    }

    @GetMapping("/online")
    public Map<String, Object> getOnlinePlayers() {
        List<Player> onlinePlayers = playerRepository.findByStatus("online");
        return Map.of("players", onlinePlayers);
    }

    @PutMapping("/status")
    public Player updateStatus(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        String status = request.get("status");
        
        Player player = playerRepository.findByUid(uid)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setStatus(status);
        player.setLastActive(LocalDateTime.now());
        
        return playerRepository.save(player);
    }

    @PostMapping("/disconnect")
    public void disconnect(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        
        Player player = playerRepository.findByUid(uid)
            .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setStatus("offline");
        playerRepository.save(player);
    }

    @PutMapping("/location")
    public Player updateLocation(@RequestBody Map<String, Object> request) {
        String uid = (String) request.get("uid");
        double x = (double) request.get("x");
        double y = (double) request.get("y");

        Player player = playerRepository.findByUid(uid)
            .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setX(x);
        player.setY(y);
        player.setLastActive(LocalDateTime.now());

        return playerRepository.save(player);
    }

    @PutMapping("/level")
    public Player updateLevel(@RequestBody Map<String, Object> request) {
        String uid = (String) request.get("uid");
        int level = (int) request.get("level");

        Player player = playerRepository.findByUid(uid)
            .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setLevel(level);
        player.setLastActive(LocalDateTime.now());

        return playerRepository.save(player);
    }

    @GetMapping("/locations")
    public Map<String, Object> getPlayerLocations() {
        List<Player> players = playerRepository.findByStatus("online");

        return Map.of("players", players.stream().map(p -> Map.of(
                "uid", p.getUid(),
                "x", p.getX(),
                "y", p.getY(),
                "level", p.getLevel()
        )).toList());
    }
}


