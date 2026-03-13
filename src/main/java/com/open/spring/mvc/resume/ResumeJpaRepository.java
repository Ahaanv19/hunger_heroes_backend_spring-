package com.open.spring.mvc.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResumeJpaRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByUsername(String username);
}
