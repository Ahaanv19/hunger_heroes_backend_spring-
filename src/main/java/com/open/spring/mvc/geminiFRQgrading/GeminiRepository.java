package com.open.spring.mvc.geminiFRQgrading;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeminiRepository extends JpaRepository<Gemini, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
