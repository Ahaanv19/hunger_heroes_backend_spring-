package com.open.spring.mvc.PauseMenu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight controller to handle gamer score submissions from the frontend.
 * Exposed at /api/gamer/score (public, no auth required).
 */
@RestController
@RequestMapping("/api/pausemenu")
public class GamerScoreController {

    @Autowired
    private ScorePauseMenuRepo scoreRepository;

    public static class GamerScoreRequest {
        public String user;
        public Integer score;
        public String gameName;
        public String variableName;
    }

    @PostMapping("/score")
    public ResponseEntity<Map<String, Object>> saveGamerScore(@RequestBody GamerScoreRequest payload) {
        try {
            int score = payload != null && payload.score != null ? payload.score : 0;
            String user = payload != null ? payload.user : null;
            if (user == null || user.trim().isEmpty()) {
                user = "guest";
            }
            String gameName = payload != null ? payload.gameName : null;
            if (gameName == null || gameName.trim().isEmpty()) {
                gameName = "unknown";
            }
            String variableName = payload != null ? payload.variableName : null;
            if (variableName == null || variableName.trim().isEmpty()) {
                variableName = "unknown";
            }

            ScoreCounter newScore = new ScoreCounter();
            newScore.setUser(user);
            newScore.setScore(score);
            newScore.setGameName(gameName);
            newScore.setVariableName(variableName);

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
