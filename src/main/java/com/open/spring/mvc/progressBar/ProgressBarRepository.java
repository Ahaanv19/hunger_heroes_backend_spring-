package com.open.spring.mvc.progressBar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressBarRepository extends JpaRepository<ProgressBar, Long> {
    
    // Find progress by userId (each user has one progress bar)
    ProgressBar findByUserId(String userId);
}