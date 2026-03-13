package com.open.spring.mvc.bathroom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class provides RESTful API endpoints for managing Person entities.
 * It includes endpoints for creating, retrieving, updating, and deleting Person entities.
 */
@RestController
@RequestMapping("/api")
public class TeacherAPIController {
    /*
    #### RESTful API REFERENCE ####
    Resource: https://spring.io/guides/gs/rest-service/
    */

    /**
     * Repository for accessing Teacher entities in the database.
     */
    @Autowired
    private TeacherJpaRepository repository;

    /**
     * Retrieves a Teacher entity by current user of JWT token.
     * @return A ResponseEntity containing the Person entity if found, or a NOT_FOUND status if not found.
     */

    /**
     * Retrieves all the Person entities in the database, people
     * @return A ResponseEntity containing a list for Person entities 
     */
    @GetMapping("/teachers")
    public ResponseEntity<List<Teacher>> getTeacher() {
        return new ResponseEntity<>( repository.findAllByOrderByFirstnameAsc(), HttpStatus.OK);
    }
}
