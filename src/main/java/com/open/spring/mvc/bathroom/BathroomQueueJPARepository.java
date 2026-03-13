package com.open.spring.mvc.bathroom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;


public interface BathroomQueueJPARepository extends JpaRepository<BathroomQueue, Long> {
    Optional<BathroomQueue> findByTeacherEmail(String teacherEmail);

    @Modifying
    @Transactional
    @Query("DELETE FROM BathroomQueue")
    void deleteAllRowsInBulk();
}
