package com.open.spring.mvc.automaticFRQFeedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/frq-feedback")
public class FeedbackRestController {

    @Autowired
    private GeminiFeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/submission/{submissionId}")
    public ResponseEntity<?> gradeSubmission(@PathVariable Long submissionId) {
        try {
            Feedback saved = feedbackService.evaluateAndPersistForSubmission(submissionId);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "feedbackId", saved.getId(),
                "submissionId", saved.getSubmissionId(),
                "createdAt", saved.getCreatedAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to generate feedback"));
        }
    }
}
