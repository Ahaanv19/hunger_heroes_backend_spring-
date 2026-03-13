package com.open.spring.mvc.challengeSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ChallengeSubmission entity.
 * Provides database operations for code runner submissions.
 */
public interface ChallengeSubmissionRepository extends JpaRepository<ChallengeSubmission, Long> {

    /**
     * Find a submission by user and lesson
     * 
     * @param user_id    The user's UID
     * @param lessonKey The lesson identifier
     * @return Optional containing the submission if found
     */
    @Query("SELECT c FROM ChallengeSubmission c WHERE c.user_id = :user_id AND c.lessonKey = :lessonKey")
    Optional<ChallengeSubmission> findByUser_idAndLessonKey(@Param("user_id") String user_id, @Param("lessonKey") String lessonKey);

    /**
     * Find all submissions for a specific user
     * 
     * @param user_id The user's UID
     * @return List of all submissions by this user
     */
    @Query("SELECT c FROM ChallengeSubmission c WHERE c.user_id = :user_id")
    List<ChallengeSubmission> findByUser_id(@Param("user_id") String user_id);

    /**
     * Find all submissions for a specific lesson
     * 
     * @param lessonKey The lesson identifier
     * @return List of all submissions for this lesson
     */
    List<ChallengeSubmission> findByLessonKey(String lessonKey);

    /**
     * Check if a submission exists for a user and lesson
     * 
     * @param user_id    The user's UID
     * @param lessonKey The lesson identifier
     * @return true if submission exists
     */
    @Query("SELECT COUNT(c) > 0 FROM ChallengeSubmission c WHERE c.user_id = :user_id AND c.lessonKey = :lessonKey")
    boolean existsByuser_idAndLessonKey(@Param("user_id") String user_id, @Param("lessonKey") String lessonKey);
}
