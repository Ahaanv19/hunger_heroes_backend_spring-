package com.open.spring.mvc.leaderboard;

import com.open.spring.mvc.PauseMenu.ScoreCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

// NOTE: This repository queries the existing score_counter table from PauseMenu
// No separate leaderboard table needed!

@Repository
public interface LeaderboardRepository extends JpaRepository<ScoreCounter, Long> {
    
    // Get all scores ordered by score descending
    @Query("SELECT s FROM ScoreCounter s ORDER BY s.score DESC")
    List<ScoreCounter> findAllByOrderByScoreDesc();
    
    // Get top N scores
    @Query("SELECT s FROM ScoreCounter s ORDER BY s.score DESC")
    List<ScoreCounter> findTopScores();
    
    // Get scores for a specific game
    @Query("SELECT s FROM ScoreCounter s WHERE s.gameName = :gameName ORDER BY s.score DESC")
    List<ScoreCounter> findByGameNameOrderByScoreDesc(@Param("gameName") String gameName);
    
    // Get scores for a specific user
    @Query("SELECT s FROM ScoreCounter s WHERE s.user = :user ORDER BY s.score DESC")
    List<ScoreCounter> findByUserOrderByScoreDesc(@Param("user") String user);
    
    // Get scores for a specific user and game
    @Query("SELECT s FROM ScoreCounter s WHERE s.user = :user AND s.gameName = :gameName ORDER BY s.score DESC")
    List<ScoreCounter> findByUserAndGameNameOrderByScoreDesc(@Param("user") String user, @Param("gameName") String gameName);
}