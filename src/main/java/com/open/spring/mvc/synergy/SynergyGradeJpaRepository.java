package com.open.spring.mvc.synergy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.open.spring.mvc.assignments.Assignment;
import com.open.spring.mvc.person.Person;

@Repository
public interface SynergyGradeJpaRepository extends JpaRepository<SynergyGrade, Long> {
    
    SynergyGrade findByAssignmentAndStudent(Assignment assignment, Person student);

    List<SynergyGrade> findByStudent(Person student);

    List<SynergyGrade> findByAssignment(Assignment assignment);

    List<SynergyGrade> findByAssignmentId(Long assignmentId);

    Optional<SynergyGrade> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT DISTINCT g.assignment.id FROM SynergyGrade g")
    List<Integer> findAllAssignmentIds();
}
