package com.open.spring.mvc.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {
    // find top N scores across all users
    @Query("SELECT q FROM QuizScore q ORDER BY q.score DESC, q.createdAt ASC")
    List<QuizScore> findAllOrderByScoreDesc();

    List<QuizScore> findByUsernameIgnoreCaseOrderByScoreDesc(@Param("username") String username);
}
