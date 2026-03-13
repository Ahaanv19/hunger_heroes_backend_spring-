package com.open.spring.mvc.generator;

import jakarta.persistence.ElementCollection;
// Imports for JPA entity and annotations
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

// Lombok annotations to reduce boilerplate code
import lombok.AllArgsConstructor;  // Generates a constructor with all fields
import lombok.Data;               // Generates getters, setters, toString, equals, and hashCode
import lombok.NoArgsConstructor;  // Generates a no-argument constructor

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Marks this class as a JPA entity
@Entity
@Data  // Lombok: auto-generates getters, setters, and more
@NoArgsConstructor  // Lombok: creates a no-arg constructor
@AllArgsConstructor  // Lombok: creates an all-args constructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedQuestion {

    // Primary key with auto-incremented ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Field to store the question text
    private String question;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tags;
}