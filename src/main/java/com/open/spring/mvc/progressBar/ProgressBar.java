package com.open.spring.mvc.progressBar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProgressBar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // User identifier

    @Column(nullable = false)
    private int completedLessons; // Number of lessons completed (0-6)

    // Custom constructor for creating new progress bar
    public ProgressBar(String userId) {
        this.userId = userId;
        this.completedLessons = 0;
    }

    // Method to increment progress
    public void incrementProgress() {
        if (this.completedLessons < 6) {
            this.completedLessons++;
        }
    }

    // Method to set progress to specific value
    public void setProgress(int lessons) {
        if (lessons >= 0 && lessons <= 6) {
            this.completedLessons = lessons;
        }
    }

    // Check if all lessons are completed
    public boolean isComplete() {
        return this.completedLessons == 6;
    }

    // Get progress as percentage
    public double getProgressPercentage() {
        return (this.completedLessons / 6.0) * 100;
    }
}