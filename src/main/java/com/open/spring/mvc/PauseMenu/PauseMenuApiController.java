package com.open.spring.mvc.PauseMenu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * API Controller for Pause Menu Score Management
 */
@RestController
@RequestMapping("/api/pausemenu/score")
public class PauseMenuApiController {

    @Autowired
    private ScorePauseMenuRepo scoreRepository;

    /**
     * DTO for receiving score data from the frontend
     */
    @Data
    public static class ScorePauseMenuRequest {
        private String user;
        private int score;
    }

    /**
     * Save a new score
     * POST /api/pausemenu/score/save
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveScore(@RequestBody ScorePauseMenuRequest request) {
        try {
            ScoreCounter newScore = new ScoreCounter();
            // default to "guest" when user is missing or blank
            String user = request.getUser();
            if (user == null || user.trim().isEmpty()) {
                user = "guest";
            }
            newScore.setUser(user);
            newScore.setScore(request.getScore());

            ScoreCounter saved = scoreRepository.save(newScore);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", saved.getId());
            response.put("message", "Score saved successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error saving score: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all scores
     * GET /api/pausemenu/score/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<ScoreCounter>> getAllScores() {
        List<ScoreCounter> scores = scoreRepository.findAll();
        return ResponseEntity.ok(scores);
    }

    /**
     * Get scores for a specific user
     * GET /api/pausemenu/score/user/{user}
     */
    @GetMapping("/user/{user}")
    public ResponseEntity<List<ScoreCounter>> getScoresByUser(@PathVariable String user) {
        List<ScoreCounter> scores = scoreRepository.findByUser(user);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get a specific score by ID
     * GET /api/pausemenu/score/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getScoreById(@PathVariable Long id) {
        return scoreRepository.findById(id)
            .map(score -> ResponseEntity.ok((Object) score))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Score not found")));
    }

    /**
     * Delete a score
     * DELETE /api/pausemenu/score/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteScore(@PathVariable Long id) {
        if (scoreRepository.existsById(id)) {
            scoreRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Score deleted"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Score not found"));
    }

    /**
     * Alternate endpoint used by frontend
     * POST /api/gamer/score
     * Accepts JSON: { "score": number, "user": string? }
     * If user is missing, defaults to "guest"
     */
    @PostMapping(path = "/api/gamer/score", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> saveGamerScore(@RequestBody Map<String, Object> payload) {
        try {
            int score = 0;
            Object scoreObj = payload.get("score");
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).intValue();
            }

            String user = (payload.get("user") instanceof String) ? (String) payload.get("user") : null;
            if (user == null || user.trim().isEmpty()) {
                user = "guest";
            }

            ScoreCounter newScore = new ScoreCounter();
            newScore.setUser(user);
            newScore.setScore(score);

            ScoreCounter saved = scoreRepository.save(newScore);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", saved.getId());
            response.put("message", "Score saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error saving score: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
