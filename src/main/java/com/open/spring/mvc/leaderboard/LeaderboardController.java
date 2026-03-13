package com.open.spring.mvc.leaderboard;

import com.open.spring.mvc.PauseMenu.ScoreCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * API Controller for Leaderboard (reads from score_counter table)
 * CORS configured for public access without authentication
 * 
 * Endpoints:
 * - GET /api/leaderboard - Main leaderboard endpoint (all scores)
 * - GET /api/pausemenu/score/leaderboard - Alias for compatibility with PauseMenu
 * - GET /api/leaderboard/top/{limit} - Top N scores
 * - GET /api/leaderboard/game/{gameName} - Scores for specific game
 * - GET /api/leaderboard/user/{user} - Scores for specific user
 */
@RestController
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {
        org.springframework.web.bind.annotation.RequestMethod.GET,
        org.springframework.web.bind.annotation.RequestMethod.OPTIONS
    },
    allowCredentials = "false"
)
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    /**
     * READ - Get all leaderboard entries ordered by score
     * This pulls directly from the score_counter table
     * GET /api/leaderboard (primary endpoint - used by frontend)
     * GET /api/pausemenu/score/leaderboard (alias to match PauseMenu path structure)
     */
    @GetMapping({"/api/leaderboard", "/api/pausemenu/score/leaderboard"})
    public ResponseEntity<List<ScoreCounter>> getAllEntries() {
        try {
            List<ScoreCounter> entries = leaderboardService.getAllEntriesByScore();
            // Always return a valid JSON array, even if empty
            if (entries == null) {
                entries = List.of();
            }
            System.out.println("Leaderboard: Returning " + entries.size() + " entries");
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            System.err.println("Error fetching leaderboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // Return empty array on error
        }
    }
    
    /**
     * READ - Get top N scores
     * GET /api/leaderboard/top/{limit}
     */
    @GetMapping("/api/leaderboard/top/{limit}")
    public ResponseEntity<List<ScoreCounter>> getTopScores(@PathVariable int limit) {
        List<ScoreCounter> entries = leaderboardService.getTopScores(limit);
        return ResponseEntity.ok(entries != null ? entries : List.of());
    }
    
    /**
     * READ - Get leaderboard entries for a specific game
     * GET /api/leaderboard/game/{gameName}
     */
    @GetMapping("/api/leaderboard/game/{gameName}")
    public ResponseEntity<List<ScoreCounter>> getEntriesByGame(@PathVariable String gameName) {
        List<ScoreCounter> entries = leaderboardService.getEntriesByGame(gameName);
        return ResponseEntity.ok(entries != null ? entries : List.of());
    }
    
    /**
     * READ - Get leaderboard entries for a specific user
     * GET /api/leaderboard/user/{user}
     */
    @GetMapping("/api/leaderboard/user/{user}")
    public ResponseEntity<List<ScoreCounter>> getUserEntries(@PathVariable String user) {
        List<ScoreCounter> entries = leaderboardService.getUserEntries(user);
        return ResponseEntity.ok(entries != null ? entries : List.of());
    }
    
    /**
     * READ - Get entries for a specific user and game
     * GET /api/leaderboard/user/{user}/game/{gameName}
     */
    @GetMapping("/api/leaderboard/user/{user}/game/{gameName}")
    public ResponseEntity<List<ScoreCounter>> getUserGameEntries(
            @PathVariable String user, 
            @PathVariable String gameName) {
        List<ScoreCounter> entries = leaderboardService.getUserGameEntries(user, gameName);
        return ResponseEntity.ok(entries != null ? entries : List.of());
    }
}