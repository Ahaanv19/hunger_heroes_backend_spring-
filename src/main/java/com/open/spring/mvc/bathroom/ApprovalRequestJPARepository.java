package com.open.spring.mvc.bathroom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestJPARepository extends JpaRepository<ApprovalRequest, Long> {
    Optional<ApprovalRequest> findByTeacherEmailAndStudentName(String teacherEmail, String studentName);
    List<ApprovalRequest> findByTeacherEmail(String teacherEmail);
}