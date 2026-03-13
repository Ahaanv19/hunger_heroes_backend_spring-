package com.open.spring.mvc.grades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress")
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private Double completionPercentage;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public Progress() {}

    public Progress(String studentId, String subject, Double completionPercentage, String status) {
        this.studentId = studentId;
        this.subject = subject;
        this.completionPercentage = completionPercentage;
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}


