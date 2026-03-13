package com.open.spring.mvc.challengeSubmission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ChallengeSubmission is a JPA Entity that stores student code submissions
 * from interactive code runners in lesson notebooks.
 * 
 * Each row represents a single user's submissions for a single lesson.
 * The challenges are stored as a JSON map where:
 * - Key: challenge ID (e.g., "csa-frqs-2019-3-0")
 * - Value: the submitted code string
 */
@Entity
@Table(name = "challenge_submissions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "lessonKey" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ChallengeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The authenticated user's UID (from Person entity) */
    @Column(nullable = false)
    private String user_id;

    /** Lesson identifier derived from permalink (e.g., "csa-frqs-2019-3") */
    @Column(nullable = false)
    private String lessonKey;

    /**
     * JSON column storing all challenge submissions for this lesson.
     * Structure: {"challengeId": "submitted code", ...}
     * Example: {"csa-frqs-2019-3-0": "public class Delimiters {...}"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> challenges = new HashMap<>();

    /** Timestamp when the submission was first created */
    @Column
    private LocalDateTime submittedAt;

    /** Timestamp when the submission was last updated */
    @Column
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Convenience method to add or update a single challenge submission
     * 
     * @param challengeId The challenge identifier
     * @param code        The submitted code
     */
    public void submitChallenge(String challengeId, String code) {
        if (this.challenges == null) {
            this.challenges = new HashMap<>();
        }
        this.challenges.put(challengeId, code);
    }

    /**
     * Convenience method to submit multiple challenges at once
     * 
     * @param challengeMap Map of challengeId -> code
     */
    public void submitAllChallenges(Map<String, String> challengeMap) {
        if (this.challenges == null) {
            this.challenges = new HashMap<>();
        }
        this.challenges.putAll(challengeMap);
    }
}
