package com.open.spring.mvc.synergy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SynergyGradeRequestJpaRepository extends JpaRepository<SynergyGradeRequest, Long> {
    List<SynergyGradeRequest> findByStudentId(Long studentId);

}
