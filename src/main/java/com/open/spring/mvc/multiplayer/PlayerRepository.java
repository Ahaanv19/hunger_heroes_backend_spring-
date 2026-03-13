package com.open.spring.mvc.multiplayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUid(String uid);
    List<Player> findByStatus(String status);
    List<Player> findByLevel(int level);
    List<Player> findByXBetweenAndYBetween(double x1, double x2, double y1, double y2);
}