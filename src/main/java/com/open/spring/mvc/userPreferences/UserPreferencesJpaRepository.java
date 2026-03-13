package com.open.spring.mvc.userPreferences;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.open.spring.mvc.person.Person;

/**
 * JPA Repository for UserPreferences entity.
 * Provides CRUD operations and custom query methods for user preferences.
 */
public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferences, Long> {

    /**
     * Find preferences by Person entity
     * @param person The Person entity
     * @return Optional containing UserPreferences if found
     */
    Optional<UserPreferences> findByPerson(Person person);

    /**
     * Find preferences by Person ID
     * @param personId The ID of the Person
     * @return Optional containing UserPreferences if found
     */
    Optional<UserPreferences> findByPersonId(Long personId);

    /**
     * Check if preferences exist for a Person
     * @param person The Person entity
     * @return true if preferences exist
     */
    boolean existsByPerson(Person person);

    /**
     * Delete preferences by Person entity
     * @param person The Person entity
     */
    void deleteByPerson(Person person);
}
