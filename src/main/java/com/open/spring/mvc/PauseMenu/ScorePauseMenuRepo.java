package com.open.spring.mvc.PauseMenu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for ScoreCounter entity
 */
@Repository
public interface ScorePauseMenuRepo extends JpaRepository<ScoreCounter, Long> {

    /**
     * Find all scores for a specific user
     */
    List<ScoreCounter> findByUser(String user);
}
