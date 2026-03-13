package com.open.spring.mvc.sprintDates;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SprintDate entity stores sprint date assignments for courses.
 * Each sprint in each course can only have one date entry (unique constraint on course + sprintKey).
 * When sprint dates are set, the system automatically creates calendar events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sprint_dates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course", "sprint_key"})
})
public class SprintDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Course code like "csa", "csp", "csse" */
    @Column(nullable = false, length = 20)
    private String course;

    /** Sprint identifier like "Sprint1", "Sprint2", etc. */
    @Column(name = "sprint_key", nullable = false, length = 50)
    private String sprintKey;

    /** Human-readable sprint title like "Tools & Setup" */
    @Column(length = 100)
    private String sprintTitle;

    /** Start date of the sprint */
    @Column(nullable = false)
    private LocalDate startDate;

    /** End date of the sprint */
    @Column(nullable = false)
    private LocalDate endDate;

    /** First week number in the sprint (e.g., 0, 1, 2) */
    @Column(nullable = false)
    private Integer startWeek;

    /** Last week number in the sprint (e.g., 2, 3, 5) */
    @Column(nullable = false)
    private Integer endWeek;

    /** 
     * JSON string storing week numbers and their assignments.
     * Example: {"0": ["Tools Setup", "GitHub Pages"], "1": ["JavaScript Basics"]}
     */
    @Column(columnDefinition = "TEXT")
    private String weekAssignments;

    /** 
     * JSON array of all created calendar event IDs for cleanup.
     * Example: [45, 46, 47, 48, 49]
     */
    @Column(columnDefinition = "TEXT")
    private String calendarEventIds;

    /** Who created/updated this sprint date entry (optional) */
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private Person createdBy;

    /** Timestamp of creation */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of last update */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new SprintDate
     */
    public SprintDate(String course, String sprintKey, String sprintTitle, 
                      LocalDate startDate, LocalDate endDate,
                      Integer startWeek, Integer endWeek) {
        this.course = course;
        this.sprintKey = sprintKey;
        this.sprintTitle = sprintTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates that the sprint date configuration is valid
     * @return true if valid
     * @throws IllegalArgumentException if invalid
     */
    public boolean validate() {
        if (course == null || course.trim().isEmpty()) {
            throw new IllegalArgumentException("Course cannot be null or empty");
        }
        if (sprintKey == null || sprintKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Sprint key cannot be null or empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (startWeek == null || endWeek == null) {
            throw new IllegalArgumentException("Start week and end week are required");
        }
        if (startWeek > endWeek) {
            throw new IllegalArgumentException("Start week must be less than or equal to end week");
        }
        return true;
    }

    /**
     * Gets the total number of weeks in this sprint
     */
    public int getTotalWeeks() {
        return endWeek - startWeek + 1;
    }
}
