package com.open.spring.mvc.media;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediaJpaRepository extends JpaRepository<Score, Long> {

    // Retrieve all scores in ascending order
    @Query("SELECT s FROM Score s ORDER BY s.score ASC")
    List<Score> findAllByScoreInc();

    // Retrieve all scores in descending order
    @Query("SELECT s FROM Score s ORDER BY s.score DESC")
    List<Score> findAllByScoreDec();

    // Retrieve scores for a specific person by ID
    List<Score> findByPersonName(String personName);

    @Override
    @Query("SELECT s FROM Score s")
    List<Score> findAll();
    // Retrieve the top score (first place)
    // @Query("SELECT s FROM Score s ORDER BY s.score DESC LIMIT 1")
    // Score findFirstPlace();
}