package com.open.spring.mvc.geminiUserPreferences;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesAIRepository extends JpaRepository<UserPrefernecesAI, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
