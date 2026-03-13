package com.open.spring.mvc.teamteach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;

/**
 * REST API Controller for managing Teacher Grade submissions.
 * Provides endpoints for CRUD operations on Teacher Grade submissions.
 */
@RestController
@RequestMapping("/api/teachergradingteamteach")
public class TeacherGradingTeamTeachAPIController {

    @Autowired
    private TeacherGradingTeamTeachJPA teacherGradingTeamTeachRepo;

    /**
     * Create a new Team Grade submission.
     * 
     * @param teamGrade the Team Grade object to be created
     * @return a ResponseEntity containing the TeacherGradingTeamTeach submission and HTTP status CREATED
     */
    @PostMapping("/submit")
    public ResponseEntity<TeacherGradingTeamTeach> createAssignment(@RequestBody TeacherGradingTeamTeach teamGrade) {
        teacherGradingTeamTeachRepo.save(teamGrade);

        // new Assignment(name, "Team Teach", description, 1, duedate);

        return new ResponseEntity<>(teamGrade, HttpStatus.CREATED);
    }

    /**
     * Get the grade for the team by ID.
     * 
     * @param TeacherGradingTeamTeachId the ID of the TeacherGradingTeamTeach to fetch
     * @return a ResponseEntity containing a TeacherGradingTeamTeach or an error 
     */
    @Transactional
    @GetMapping("/teamgrade/{TeacherGradingTeamTeachId}")
    public ResponseEntity<?> getTeacherGradeByAssignment(@PathVariable Long TeacherGradingTeamTeachId) {
        TeacherGradingTeamTeach teamgrade = teacherGradingTeamTeachRepo.findById(TeacherGradingTeamTeachId).orElse(null);
        if (teamgrade == null) {
            return new ResponseEntity<>(
                Collections.singletonMap("error", "Assignment not found"), 
                HttpStatus.NOT_FOUND
            );
        }
        return new ResponseEntity<>(teamgrade, HttpStatus.OK);
    }

    /**
     * Debug endpoint that returns all teacher grading records.
     * @return Information about all grades.
     */
    @GetMapping("/debug")
    public ResponseEntity<List<TeacherGradingTeamTeach>> debugTeacherGrades() {
        List<TeacherGradingTeamTeach> teacherGradingTeamTeachs = teacherGradingTeamTeachRepo.findAll();
        return new ResponseEntity<>(teacherGradingTeamTeachs, HttpStatus.OK);
    }

    /**
     * Create a new comment for grading.
     * @param comment the comment to be created
     * @return the created comment with HTTP status CREATED
     */
    @PostMapping("/comment")
    public ResponseEntity<TeacherGradingTeamTeach> createMessage(@RequestBody TeacherGradingTeamTeach comment) {
        if (comment.getComment() == null) {
            comment.setComment(""); // Ensure the field is initialized properly
        }
        TeacherGradingTeamTeach savedComment = teacherGradingTeamTeachRepo.save(comment);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    /**
     * Get a teacher grading record by ID.
     * @param id the record ID
     * @return the found record or 404 if not found
     */
    @GetMapping("comment/{id}")
    public ResponseEntity<TeacherGradingTeamTeach> getMessageById(@PathVariable Long id) {
        return teacherGradingTeamTeachRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
