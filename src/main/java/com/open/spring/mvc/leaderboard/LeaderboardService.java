package com.open.spring.mvc.leaderboard;

import com.open.spring.mvc.PauseMenu.ScoreCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

// This service reads directly from the score_counter table (PauseMenu)
// No separate leaderboard table needed!

@Service
public class LeaderboardService {
    
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    
    /**
     * Get top N entries from pausemenu table
     */
    public List<ScoreCounter> getTopScores(int limit) {
        return leaderboardRepository.findTopScores()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all entries ordered by score (for the leaderboard widget)
     */
    public List<ScoreCounter> getAllEntriesByScore() {
        return leaderboardRepository.findAllByOrderByScoreDesc();
    }
    
    /**
     * Get entries for a specific game
     */
    public List<ScoreCounter> getEntriesByGame(String gameName) {
        return leaderboardRepository.findByGameNameOrderByScoreDesc(gameName);
    }
    
    /**
     * Get entries for a specific user
     */
    public List<ScoreCounter> getUserEntries(String user) {
        return leaderboardRepository.findByUserOrderByScoreDesc(user);
    }
    
    /**
     * Get entries for a specific user and game
     */
    public List<ScoreCounter> getUserGameEntries(String user, String gameName) {
        return leaderboardRepository.findByUserAndGameNameOrderByScoreDesc(user, gameName);
    }
}