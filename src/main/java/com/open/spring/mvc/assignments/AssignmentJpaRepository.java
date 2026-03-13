package com.open.spring.mvc.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.open.spring.mvc.person.Person;

@Repository
public interface AssignmentJpaRepository extends JpaRepository<Assignment, Long> {
    Assignment findByName(String name);
    List<Assignment> findByAssignedGraders(Person grader);
    // hello this is a test commit
}