package com.open.spring.mvc.plant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plant")
public class PlantController {

    @Autowired
    private PlantRepository plantRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlantRequest {
        private String uid;

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
    }

    // Get all plants (for admin/debugging)
    @GetMapping("/all")
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();
        return new ResponseEntity<>(plants, HttpStatus.OK);
    }

    // Get specific user's plant progress
    @GetMapping("/user/{uid}")
    public ResponseEntity<?> getUserPlant(@PathVariable String uid) {
        Plant plant = plantRepository.findByUid(uid);
        
        if (plant == null) {
            return new ResponseEntity<>(
                Map.of("error", "No plant found for uid: " + uid),
                HttpStatus.NOT_FOUND
            );
        }
        
        return new ResponseEntity<>(plant, HttpStatus.OK);
    }

    // Create/Add new plant for user
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addPlant(@RequestBody PlantRequest plantRequest) {
        String uid = plantRequest.getUid();
        
        // Check if user already has a plant
        Plant existingPlant = plantRepository.findByUid(uid);
        if (existingPlant != null) {
            return new ResponseEntity<>(
                Map.of(
                    "error", "User already has a plant at stage " + existingPlant.getCurrentStage(),
                    "currentStage", existingPlant.getCurrentStage(),
                    "totalLessons", existingPlant.getTotalLessonsCompleted()
                ),
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Create new plant starting at stage 1
        Plant newPlant = new Plant(uid);
        Plant savedPlant = plantRepository.save(newPlant);
        
        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "message", "Plant created for user: " + uid,
                "currentStage", savedPlant.getCurrentStage(),
                "totalLessons", savedPlant.getTotalLessonsCompleted()
            ),
            HttpStatus.OK
        );
    }

    // Advance plant to next stage (when user completes lesson)
    @PostMapping("/user/{uid}/next")
    public ResponseEntity<Map<String, Object>> advancePlant(@PathVariable String uid) {
        Plant plant = plantRepository.findByUid(uid);
        
        if (plant == null) {
            // Auto-create plant if it doesn't exist
            plant = new Plant(uid);
        }
        
        int previousStage = plant.getCurrentStage();
        plant.advanceStage();
        plantRepository.save(plant);
        
        String message = "Plant advanced from stage " + previousStage + " to stage " + plant.getCurrentStage();
        if (plant.isFullyGrown()) {
            message += " - Plant is fully grown!";
        }
        
        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "uid", uid,
                "currentStage", plant.getCurrentStage(),
                "totalLessons", plant.getTotalLessonsCompleted(),
                "message", message,
                "fullyGrown", plant.isFullyGrown()
            ),
            HttpStatus.OK
        );
    }

    // Reset plant to stage 1 (if needed)
    @PostMapping("/user/{uid}/reset")
    public ResponseEntity<Map<String, Object>> resetPlant(@PathVariable String uid) {
        Plant plant = plantRepository.findByUid(uid);
        
        if (plant == null) {
            return new ResponseEntity<>(
                Map.of("error", "No plant found for uid: " + uid),
                HttpStatus.NOT_FOUND
            );
        }
        
        plant.setCurrentStage(1);
        plant.setTotalLessonsCompleted(0);
        plantRepository.save(plant);
        
        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "message", "Plant reset to stage 1 for user: " + uid,
                "currentStage", plant.getCurrentStage(),
                "totalLessons", plant.getTotalLessonsCompleted()
            ),
            HttpStatus.OK
        );
    }

    // Get plant stage only (lightweight endpoint for frontend)
    @GetMapping("/stage/{uid}")
    public ResponseEntity<Map<String, Object>> getPlantStage(@PathVariable String uid) {
        Plant plant = plantRepository.findByUid(uid);
        
        if (plant == null) {
            // Return stage 1 if no plant exists yet
            return new ResponseEntity<>(
                Map.of(
                    "uid", uid,
                    "currentStage", 1,
                    "totalLessons", 0,
                    "fullyGrown", false
                ),
                HttpStatus.OK
            );
        }
        
        return new ResponseEntity<>(
            Map.of(
                "uid", uid,
                "currentStage", plant.getCurrentStage(),
                "totalLessons", plant.getTotalLessonsCompleted(),
                "fullyGrown", plant.isFullyGrown()
            ),
            HttpStatus.OK
        );
    }
}