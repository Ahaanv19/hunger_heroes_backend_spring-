package com.open.spring.mvc.automaticFRQFeedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "frq_feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long submissionId;

    private Long submitterId;

    private Integer frqYear;

    private Integer questionNumber;

    private Integer attemptNumber;

    private Double overallScore;

    private Integer maxScore;

    @Column(columnDefinition = "TEXT")
    private String scoreBreakdownJson;

    @Column(columnDefinition = "TEXT")
    private String overallFeedbackJson;

    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(columnDefinition = "TEXT")
    private String areasForImprovementJson;

    @Column(columnDefinition = "TEXT")
    private String rawGeminiResponse;

    private Long createdAt;

    public Feedback(Long submissionId, Long submitterId, Integer frqYear, Integer questionNumber,
                    Integer attemptNumber, Double overallScore, Integer maxScore,
                    String scoreBreakdownJson, String overallFeedbackJson, String strengthsJson,
                    String areasForImprovementJson, String rawGeminiResponse) {
        this.submissionId = submissionId;
        this.submitterId = submitterId;
        this.frqYear = frqYear;
        this.questionNumber = questionNumber;
        this.attemptNumber = attemptNumber;
        this.overallScore = overallScore;
        this.maxScore = maxScore;
        this.scoreBreakdownJson = scoreBreakdownJson;
        this.overallFeedbackJson = overallFeedbackJson;
        this.strengthsJson = strengthsJson;
        this.areasForImprovementJson = areasForImprovementJson;
        this.rawGeminiResponse = rawGeminiResponse;
        this.createdAt = System.currentTimeMillis();
    }
}
