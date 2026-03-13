package com.open.spring.mvc.sprintDates;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for SprintDate entity.
 * Provides CRUD operations and custom query methods for sprint dates.
 */
public interface SprintDateJpaRepository extends JpaRepository<SprintDate, Long> {

    /**
     * Find a specific sprint's dates by course and sprint key
     * @param course The course code (e.g., "csa", "csp", "csse")
     * @param sprintKey The sprint identifier (e.g., "Sprint1", "Sprint2")
     * @return Optional containing SprintDate if found
     */
    Optional<SprintDate> findByCourseAndSprintKey(String course, String sprintKey);

    /**
     * Find all sprint dates for a specific course
     * @param course The course code
     * @return List of SprintDate entries for that course
     */
    List<SprintDate> findByCourse(String course);

    /**
     * Find all sprint dates ordered by course and sprint key
     * @return List of all SprintDate entries
     */
    List<SprintDate> findAllByOrderByCourseAscSprintKeyAsc();

    /**
     * Check if a sprint date exists for a course and sprint key
     * @param course The course code
     * @param sprintKey The sprint identifier
     * @return true if exists
     */
    boolean existsByCourseAndSprintKey(String course, String sprintKey);

    /**
     * Delete a sprint date by course and sprint key
     * @param course The course code
     * @param sprintKey The sprint identifier
     */
    void deleteByCourseAndSprintKey(String course, String sprintKey);

    /**
     * Find all sprint dates for a course, ordered by start week
     * @param course The course code
     * @return List of SprintDate entries ordered by start week
     */
    List<SprintDate> findByCourseOrderByStartWeekAsc(String course);
}
