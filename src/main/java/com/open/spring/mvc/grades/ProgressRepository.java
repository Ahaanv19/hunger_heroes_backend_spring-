package com.open.spring.mvc.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByStudentId(String studentId);
    List<Progress> findBySubject(String subject);
    List<Progress> findByStatus(String status);
}


