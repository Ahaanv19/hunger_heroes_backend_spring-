package com.open.spring.mvc.challengeSubmission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing code runner challenge submissions.
 * Provides endpoints for submitting individual challenges or entire lessons.
 */
@RestController
@RequestMapping("/api/challenge-submission")
@CrossOrigin(origins = { "http://127.0.0.1:4500", "http://localhost:4500",
        "https://pages.opencodingsociety.com" }, allowCredentials = "true")
public class ChallengeSubmissionController {

    @Autowired
    private ChallengeSubmissionRepository submissionRepo;

    /**
     * Submit ALL challenges for a lesson at once.
     * 
     * Request body:
     * {
     * "lessonKey": "csa-frqs-2019-3",
     * "challenges": {
     * "csa-frqs-2019-3-0": "public class Delimiters {...}",
     * "csa-frqs-2019-3-1": "// solution code..."
     * }
     * }
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitLesson(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String user_id = userDetails.getUsername();

        // Validate request body
        String lessonKey = (String) body.get("lessonKey");
        @SuppressWarnings("unchecked")
        Map<String, String> challenges = (Map<String, String>) body.get("challenges");

        if (lessonKey == null || lessonKey.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "lessonKey is required"));
        }

        if (challenges == null || challenges.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "challenges map is required and cannot be empty"));
        }

        // Find existing or create new submission
        ChallengeSubmission submission = submissionRepo
                .findByUser_idAndLessonKey(user_id, lessonKey)
                .orElseGet(() -> {
                    ChallengeSubmission newSubmission = new ChallengeSubmission();
                    newSubmission.setUser_id(user_id);
                    newSubmission.setLessonKey(lessonKey);
                    return newSubmission;
                });

        // Submit all challenges (overwrites existing)
        submission.submitAllChallenges(challenges);

        ChallengeSubmission saved = submissionRepo.save(submission);

        return ResponseEntity.ok(Map.of(
                "message", "Lesson submitted successfully",
                "submission", saved));
    }

    /**
     * Submit a SINGLE challenge within a lesson.
     * 
     * Request body:
     * {
     * "lessonKey": "csa-frqs-2019-3",
     * "challengeId": "csa-frqs-2019-3-0",
     * "code": "public class Delimiters {...}"
     * }
     */
    @PostMapping("/submit-challenge")
    public ResponseEntity<?> submitChallenge(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String user_id = userDetails.getUsername();

        // Validate request body
        String lessonKey = body.get("lessonKey");
        String challengeId = body.get("challengeId");
        String code = body.get("code");

        if (lessonKey == null || lessonKey.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "lessonKey is required"));
        }

        if (challengeId == null || challengeId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "challengeId is required"));
        }

        if (code == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "code is required"));
        }

        // Find existing or create new submission
        ChallengeSubmission submission = submissionRepo
                .findByUser_idAndLessonKey(user_id, lessonKey)
                .orElseGet(() -> {
                    ChallengeSubmission newSubmission = new ChallengeSubmission();
                    newSubmission.setUser_id(user_id);
                    newSubmission.setLessonKey(lessonKey);
                    newSubmission.setChallenges(new HashMap<>());
                    return newSubmission;
                });

        // Submit single challenge
        submission.submitChallenge(challengeId, code);

        ChallengeSubmission saved = submissionRepo.save(submission);

        return ResponseEntity.ok(Map.of(
                "message", "Challenge submitted successfully",
                "challengeId", challengeId,
                "submission", saved));
    }

    /**
     * Get submission for a specific lesson.
     * Returns the user's submission if it exists, or creates a new empty one.
     */
    @GetMapping("/{lessonKey}")
    public ResponseEntity<?> getSubmission(
            @PathVariable String lessonKey,
            Authentication authentication) {

        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String user_id = userDetails.getUsername();

        Optional<ChallengeSubmission> submission = submissionRepo
                .findByUser_idAndLessonKey(user_id, lessonKey);

        if (submission.isPresent()) {
            return ResponseEntity.ok(submission.get());
        } else {
            // Return empty response indicating no submission exists yet
            return ResponseEntity.ok(Map.of(
                    "user_id", user_id,
                    "lessonKey", lessonKey,
                    "challenges", Map.of(),
                    "exists", false));
        }
    }

    /**
     * Get all submissions for the authenticated user.
     */
    @GetMapping("/my-submissions")
    public ResponseEntity<?> getMySubmissions(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String user_id = userDetails.getUsername();

        List<ChallengeSubmission> submissions = submissionRepo.findByUser_id(user_id);

        return ResponseEntity.ok(Map.of(
                "user_id", user_id,
                "submissions", submissions,
                "count", submissions.size()));
    }

    /**
     * Admin endpoint: Get all submissions for a lesson.
     * TODO: Add ROLE_ADMIN check
     */
    @GetMapping("/lesson/{lessonKey}/all")
    public ResponseEntity<?> getAllSubmissionsForLesson(@PathVariable String lessonKey) {
        List<ChallengeSubmission> submissions = submissionRepo.findByLessonKey(lessonKey);

        return ResponseEntity.ok(Map.of(
                "lessonKey", lessonKey,
                "submissions", submissions,
                "count", submissions.size()));
    }
}
