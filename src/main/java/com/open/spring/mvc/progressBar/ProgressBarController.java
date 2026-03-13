package com.open.spring.mvc.progressBar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressBarController {

    @Autowired
    private ProgressBarRepository progressBarRepository;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressRequest {
        private String userId;
        private int completedLessons;
    }

    // GET - Get user's progress
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable String userId) {
        ProgressBar progress = progressBarRepository.findByUserId(userId);
        
        if (progress == null) {
            // Return 0 if no progress exists yet
            return new ResponseEntity<>(
                Map.of(
                    "userId", userId,
                    "completedLessons", 0
                ),
                HttpStatus.OK
            );
        }
        
        return new ResponseEntity<>(
            Map.of(
                "userId", progress.getUserId(),
                "completedLessons", progress.getCompletedLessons()
            ),
            HttpStatus.OK
        );
    }

    // POST - Update user's progress
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProgress(@RequestBody ProgressRequest request) {
        String userId = request.getUserId();
        int completedLessons = request.getCompletedLessons();
        
        // Find existing or create new
        ProgressBar progress = progressBarRepository.findByUserId(userId);
        if (progress == null) {
            progress = new ProgressBar(userId);
        }
        
        progress.setCompletedLessons(completedLessons);
        ProgressBar savedProgress = progressBarRepository.save(progress);
        
        return new ResponseEntity<>(
            Map.of(
                "status", "success",
                "userId", savedProgress.getUserId(),
                "completedLessons", savedProgress.getCompletedLessons()
            ),
            HttpStatus.OK
        );
    }
}