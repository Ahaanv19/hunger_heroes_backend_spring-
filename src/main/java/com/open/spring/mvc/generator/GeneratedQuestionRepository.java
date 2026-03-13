package com.open.spring.mvc.generator;

// Repository interface for database operations
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Extends JpaRepository to provide CRUD operations for GeneratedQuestion entities
public interface GeneratedQuestionRepository extends JpaRepository<GeneratedQuestion, Long> {
    List<GeneratedQuestion> findByTagsContaining(String tag);
}
