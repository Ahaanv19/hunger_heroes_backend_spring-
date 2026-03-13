package com.open.spring.mvc.bathroom;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "approval_requests")
public class ApprovalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teacherEmail;
    private String studentName;
    private String timeIn;

    public ApprovalRequest(String teacherEmail, String studentName, String timeIn) {
        this.teacherEmail = teacherEmail;
        this.studentName = studentName;
        this.timeIn = timeIn;
    }
}
