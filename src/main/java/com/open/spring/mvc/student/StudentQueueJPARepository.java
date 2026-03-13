package com.open.spring.mvc.student;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentQueueJPARepository extends JpaRepository<StudentQueue, Long> {
    Optional<StudentQueue> findByTeacherEmail(String teacherEmail);
}


