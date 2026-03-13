package com.open.spring.mvc.rpg.games;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameJpaRepository extends JpaRepository<Game, Long> {
    List<Game> findByPersonId(Long personId);
    List<Game> findByType(String type);
}
