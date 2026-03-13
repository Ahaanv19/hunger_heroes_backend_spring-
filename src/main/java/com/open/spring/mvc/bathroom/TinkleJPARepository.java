package com.open.spring.mvc.bathroom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional; 

/**
 * Repository interface for managing Tinkle entities in the database.
 * Handles bathroom request records, tracking student requests, approvals, and history.
 */
public interface TinkleJPARepository extends JpaRepository<Tinkle, Long> {
    
    /**
     * Query method defined by Spring Data JPA naming conventions.
     * Finds a bathroom request by the person's name who made the request.
     * 
     * @param personName The name of the person whose bathroom request should be retrieved
     * @return Optional<Tinkle> containing the request if found, or empty if not found
     */
    Optional<Tinkle> findByPersonName(String personName);

    /**
     * Query method defined by Spring Data JPA naming conventions.
     * Finds a bathroom request by the person's sid who made the request.
     * 
     * @param sid The sid of the person whose bathroom request should be retrieved
     * @return Optional<Tinkle> containing the request if found, or empty if not found
     */
    Optional<Tinkle> findBySid(String sid);


    /**
     * Custom bulk delete operation to remove all Tinkle records from the database.
     * Used for administrative purposes like clearing bathroom request history.
     * 
     * @Modifying indicates this query modifies the database
     * @Transactional ensures the operation runs within a transaction
     * 
     * Warning: This operation is irreversible and permanently deletes all records.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Tinkle")
    void deleteAllRowsInBulk();
}