package com.open.spring.mvc.plant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String uid; // User identifier (matching your existing system)

    @Column(nullable = false)
    private int currentStage; // Plant growth stage (1-6)

    @Column(nullable = false)
    private int totalLessonsCompleted; // Total lessons completed by user

    // Custom constructor for creating new plant
    public Plant(String uid) {
        this.uid = uid;
        this.currentStage = 1; // Start at stage 1
        this.totalLessonsCompleted = 0;
    }

    // Method to advance to next stage
    public void advanceStage() {
        if (this.currentStage < 6) {
            this.currentStage++;
        }
        this.totalLessonsCompleted++;
    }

    // Check if plant is fully grown
    public boolean isFullyGrown() {
        return this.currentStage == 6;
    }
}