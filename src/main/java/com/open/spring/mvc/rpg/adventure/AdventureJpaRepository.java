package com.open.spring.mvc.rpg.adventure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdventureJpaRepository extends JpaRepository<Adventure, Long> {
    List<Adventure> findByPersonId(Long personId);
    List<Adventure> findByPersonIdAndQuestionCategory(Long personId, String category);
    List<Adventure> findByQuestionCategory(String category);
}
