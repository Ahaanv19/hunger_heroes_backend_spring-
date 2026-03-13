package com.open.spring.mvc.generic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmicEventRepository extends JpaRepository<AlgorithmicEvent, Long> {
    List<AlgorithmicEvent> findByType(EventType type);

    List<AlgorithmicEvent> findByUserIdAndType(String userId, EventType type);
}
