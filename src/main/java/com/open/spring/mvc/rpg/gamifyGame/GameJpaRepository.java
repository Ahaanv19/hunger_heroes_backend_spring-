package com.open.spring.mvc.rpg.gamifyGame;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GameJpaRepository extends JpaRepository<Game, Long> {
    Game findByName(String name);
}