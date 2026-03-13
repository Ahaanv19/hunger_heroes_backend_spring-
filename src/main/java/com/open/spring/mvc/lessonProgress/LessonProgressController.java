package com.open.spring.mvc.lessonProgress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lesson-progress")
@CrossOrigin(
    origins = {"http://127.0.0.1:4500", "http://localhost:4500"},
    allowCredentials = "true"
)
public class LessonProgressController {

    @Autowired
    private LessonProgressRepository progressRepo;

    // --- CREATE ---
    @PostMapping
    public ResponseEntity<LessonProgress> createProgress(@RequestBody LessonProgress progress, Authentication authentication) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        
        // Set the userId from authentication (ignore any userId in the request body)
        progress.setUserId(userId);
        
        return ResponseEntity.ok(progressRepo.save(progress));
    }

    // --- READ ---
    @GetMapping("/{lessonKey}")
    public ResponseEntity<LessonProgress> getProgress(@PathVariable String lessonKey, Authentication authentication) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername(); // This is the UID/email from Person entity
        
        Optional<LessonProgress> progress = progressRepo.findByUserIdAndLessonKey(userId, lessonKey);
        LessonProgress result = progress.orElseGet(() -> {
            LessonProgress newProgress = new LessonProgress();
            newProgress.setUserId(userId);
            newProgress.setLessonKey(lessonKey);
            newProgress.setCompleted(false); // Initialize as not completed
            newProgress.setTotalTimeMs(0L); // Initialize time
            return progressRepo.save(newProgress);
        });
        
        return ResponseEntity.ok(result);
    }

    // --- UPDATE ---
    @PutMapping("/{id}")
    public ResponseEntity<LessonProgress> updateProgress(@PathVariable Long id, @RequestBody LessonProgress updatedProgress, Authentication authentication) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();
        
        return progressRepo.findById(id).map(existing -> {
            // Verify this progress belongs to the authenticated user
            if (!existing.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<LessonProgress>build();
            }
            
            existing.setTotalTimeMs(updatedProgress.getTotalTimeMs());
            existing.setLastVisited(updatedProgress.getLastVisited());
            existing.setCompleted(updatedProgress.getCompleted());
            existing.setBadges(updatedProgress.getBadges());
            existing.setReflectionText(updatedProgress.getReflectionText());
            existing.setCurrentFlashcardIndex(updatedProgress.getCurrentFlashcardIndex());
            return ResponseEntity.ok(progressRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- LIST (for admin/testing) ---
    @GetMapping
    public List<LessonProgress> getAll() {
        return progressRepo.findAll();
    }
}
