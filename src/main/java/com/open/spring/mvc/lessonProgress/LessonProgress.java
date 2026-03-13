package com.open.spring.mvc.lessonProgress;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lesson_progress")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String lessonKey;

    // Time tracking
    @Column(nullable = false)
    private Long totalTimeMs = 0L;

    @Column
    private LocalDateTime lastVisited;

    // Progress tracking
    @Column(nullable = false)
    private Boolean completed = false;

    // Badges - FIXED: Use EAGER fetch to avoid lazy loading issues
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lesson_badges", joinColumns = @JoinColumn(name = "lesson_progress_id"))
    @Column(name = "badge_name")
    private List<String> badges = new ArrayList<>();

    // Current flashcard index (optional)
    @Column
    private Integer currentFlashcardIndex = 1;

    // Reflection / Notes
    @Column(columnDefinition = "TEXT")
    private String reflectionText;

    // Metadata
    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}