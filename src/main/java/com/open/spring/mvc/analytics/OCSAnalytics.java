package com.open.spring.mvc.analytics;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.open.spring.mvc.person.Person;

import java.time.LocalDateTime;

/**
 * OCSAnalytics Entity - Tracks Open Coding Society learning platform engagement
 * 
 * Metrics tracked:
 * - Time spent on website per session
 * - Lessons/modules viewed
 * - Copy-paste attempts
 * - Video views
 * - Code executions
 * - Assessment attempts and scores
 * - Module/quest completion
 */
@Entity
@Table(name = "ocs_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OCSAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Person
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    @JsonIgnore
    private Person person;

    // Basic session info
    @Column(nullable = false)
    private LocalDateTime sessionStartTime;

    @Column(nullable = false)
    private LocalDateTime sessionEndTime;

    @Column(nullable = false)
    private Long sessionDurationSeconds; // Total time in seconds

    // Content tracking
    @Column
    private String questName; // e.g., "cs-portfolio-quest", "digital-famine"

    @Column
    private String moduleName; // e.g., "frontend", "cybersecurity"

    @Column
    private String lessonNumber; // e.g., "1", "2", etc.

    @Column
    private String pageTitle; // e.g., "Variables and Data Types"

    @Column
    private String pageUrl; // Full URL visited

    // User actions
    @Column
    private Integer lessonsViewed = 0; // Count of unique lessons viewed

    @Column
    private Integer lessonsCompleted = 0; // Count of lessons marked as complete

    @Column
    private Integer modulesViewed = 0; // Count of unique modules viewed

    @Column
    private Integer videosWatched = 0; // Count of videos viewed

    @Column
    private Integer videosCompleted = 0; // Count of videos fully watched

    @Column
    private Integer codeExecutions = 0; // Count of code runs/executions

    @Column
    private Integer copyPasteAttempts = 0; // Count of copy-paste events

    @Column
    private Integer questionsAnswered = 0; // Count of quiz questions answered

    @Column
    private Integer questionsCorrect = 0; // Count of correct answers

    @Column
    private Double accuracyPercentage = 0.0; // (correct/total) * 100

    @Column
    private Integer exercisesAttempted = 0; // Count of exercises started

    @Column
    private Integer exercisesCompleted = 0; // Count of exercises finished

    @Column
    private Integer assessmentsAttempted = 0; // Count of assessments

    @Column
    private Double assessmentAverageScore = 0.0; // Average score on assessments

    // Module/Quest progression
    @Column
    private Boolean moduleCompleted = false; // Is module/quest marked done?

    @Column
    private Integer progressPercentage = 0; // % of lesson/module completed

    @Column
    private Double estimatedGradeScore = 0.0; // Predicted grade based on activity

    // Engagement metrics
    @Column
    private Integer scrollDepthPercentage = 0; // How far down page user scrolled

    @Column
    private Double interactionPercentage = 0.0; // Percentage of time spent interacting vs idle

    @Column
    private Integer hoverEventsCount = 0; // Count of hover/focus interactions

    @Column
    private Integer keyboardInputEvents = 0; // Count of keyboard inputs

    @Column
    private Integer mouseClicksCount = 0; // Count of mouse clicks

    // Performance metrics
    @Column
    private Double pageLoadTimeMs = 0.0; // Page load time in ms

    @Column
    private Integer timeoutErrors = 0; // Count of timeout/API errors

    @Column
    private Integer validationErrors = 0; // Count of validation failures

    // Metadata
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private String userAgent; // Browser/device info

    @Column
    private String referrer; // Where user came from

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate accuracy percentage from correct/total answers
     */
    public void calculateAccuracy() {
        if (questionsAnswered > 0) {
            this.accuracyPercentage = (double) (questionsCorrect * 100) / questionsAnswered;
        }
    }

    /**
     * Calculate exercise completion rate
     */
    public Double getExerciseCompletionRate() {
        if (exercisesAttempted == 0) return 0.0;
        return (double) (exercisesCompleted * 100) / exercisesAttempted;
    }

    /**
     * Get session duration in human-readable format
     */
    public String getFormattedDuration() {
        long hours = sessionDurationSeconds / 3600;
        long minutes = (sessionDurationSeconds % 3600) / 60;
        long seconds = sessionDurationSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
