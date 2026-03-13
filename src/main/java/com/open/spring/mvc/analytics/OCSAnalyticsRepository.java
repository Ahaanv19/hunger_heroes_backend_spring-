package com.open.spring.mvc.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.open.spring.mvc.person.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OCSAnalyticsRepository extends JpaRepository<OCSAnalytics, Long> {

    /**
     * Find all analytics records for a specific person
     */
    List<OCSAnalytics> findByPersonOrderBySessionStartTimeDesc(Person person);

    /**
     * Find analytics for a person within a date range
     */
    List<OCSAnalytics> findByPersonAndSessionStartTimeGreaterThanAndSessionStartTimeLessThan(
            Person person, LocalDateTime start, LocalDateTime end);

    /**
     * Find analytics for a specific quest/module
     */
    List<OCSAnalytics> findByPersonAndQuestName(Person person, String questName);

    /**
     * Find analytics for a specific lesson
     */
    List<OCSAnalytics> findByPersonAndQuestNameAndModuleName(
            Person person, String questName, String moduleName);

    /**
     * Get total time spent by user on the platform
     */
    @Query("SELECT SUM(a.sessionDurationSeconds) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Long> getTotalTimeSpentSeconds(@Param("person") Person person);

    /**
     * Get average session duration for a person
     */
    @Query("SELECT AVG(a.sessionDurationSeconds) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Double> getAverageSessionDuration(@Param("person") Person person);

    /**
     * Get total lessons viewed by person
     */
    @Query("SELECT SUM(a.lessonsViewed) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Integer> getTotalLessonsViewed(@Param("person") Person person);

    /**
     * Get total lessons completed by person
     */
    @Query("SELECT SUM(a.lessonsCompleted) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Integer> getTotalLessonsCompleted(@Param("person") Person person);

    /**
     * Get total modules viewed by person
     */
    @Query("SELECT SUM(a.modulesViewed) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Integer> getTotalModulesViewed(@Param("person") Person person);

    /**
     * Get total copy-paste attempts by person
     */
    @Query("SELECT SUM(a.copyPasteAttempts) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Integer> getTotalCopyPasteAttempts(@Param("person") Person person);

    /**
     * Get completion status for a specific quest
     */
    @Query("SELECT COUNT(a) > 0 FROM OCSAnalytics a WHERE a.person = :person AND a.questName = :questName AND a.moduleCompleted = true")
    boolean isQuestCompleted(@Param("person") Person person, @Param("questName") String questName);

    /**
     * Get all quests a person has engaged with
     */
    @Query("SELECT DISTINCT a.questName FROM OCSAnalytics a WHERE a.person = :person AND a.questName IS NOT NULL")
    List<String> getEngagedQuests(@Param("person") Person person);

    /**
     * Get average accuracy across all assessments
     */
    @Query("SELECT AVG(a.accuracyPercentage) FROM OCSAnalytics a WHERE a.person = :person AND a.questionsAnswered > 0")
    Optional<Double> getAverageAccuracy(@Param("person") Person person);

    /**
     * Get total copy-paste attempts for a specific quest
     */
    @Query("SELECT SUM(a.copyPasteAttempts) FROM OCSAnalytics a WHERE a.person = :person AND a.questName = :questName")
    Optional<Integer> getCopyPasteAttemptsForQuest(@Param("person") Person person, @Param("questName") String questName);

    /**
     * Get total code executions by person
     */
    @Query("SELECT SUM(a.codeExecutions) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Integer> getTotalCodeExecutions(@Param("person") Person person);

    /**
     * Get average interaction percentage
     */
    @Query("SELECT AVG(a.interactionPercentage) FROM OCSAnalytics a WHERE a.person = :person AND a.interactionPercentage > 0")
    Optional<Double> getAverageInteractionPercentage(@Param("person") Person person);

    /**
     * Get average scroll depth
     */
    @Query("SELECT AVG(a.scrollDepthPercentage) FROM OCSAnalytics a WHERE a.person = :person")
    Optional<Double> getAverageScrollDepth(@Param("person") Person person);}