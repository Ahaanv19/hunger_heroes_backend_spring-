package com.open.spring.mvc.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.open.spring.mvc.assignments.Assignment;
import com.open.spring.mvc.assignments.AssignmentJpaRepository;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
import com.open.spring.mvc.synergy.SynergyGrade;
import com.open.spring.mvc.synergy.SynergyGradeJpaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:8080")  // Enable CORS for the frontend URL
public class AnalyticsApiController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsApiController.class);

    @Autowired
    private PersonJpaRepository personRepository;

    @Autowired
    private AssignmentJpaRepository assignmentJpaRepository;

    @Autowired
    private SynergyGradeJpaRepository gradeJpaRepository;

    @Autowired
    private PersonJpaRepository personJpaRepository;

    // Get all analytics records
    // Get all analytics records
    @GetMapping("/")
    public ResponseEntity<List<AnalyticsGradeDto>> getAllAnalytics() {
        List<SynergyGrade> gradeList = gradeJpaRepository.findAll();  // Fetch all grade records from database
        if (gradeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // No records found
        }

        List<AnalyticsGradeDto> dtoList = new ArrayList<>();
        for (SynergyGrade grade : gradeList) {
            dtoList.add(AnalyticsGradeDto.from(grade));
        }

        return new ResponseEntity<>(dtoList, HttpStatus.OK); // Return found records
    }

    public static class AnalyticsGradeDto {
        private Long id;
        private Double grade;
        private Long assignmentId;
        private String assignmentName;
        private Long studentId;
        private String studentUid;
        private String studentName;

        public static AnalyticsGradeDto from(SynergyGrade grade) {
            AnalyticsGradeDto dto = new AnalyticsGradeDto();
            if (grade == null) {
                return dto;
            }
            dto.id = grade.getId();
            dto.grade = grade.getGrade();
            if (grade.getAssignment() != null) {
                dto.assignmentId = grade.getAssignment().getId();
                dto.assignmentName = grade.getAssignment().getName();
            }
            if (grade.getStudent() != null) {
                dto.studentId = grade.getStudent().getId();
                dto.studentUid = grade.getStudent().getUid();
                dto.studentName = grade.getStudent().getName();
            }
            return dto;
        }

        public Long getId() {
            return id;
        }

        public Double getGrade() {
            return grade;
        }

        public Long getAssignmentId() {
            return assignmentId;
        }

        public String getAssignmentName() {
            return assignmentName;
        }

        public Long getStudentId() {
            return studentId;
        }

        public String getStudentUid() {
            return studentUid;
        }

        public String getStudentName() {
            return studentName;
        }
    }

    
    @GetMapping("/assignments")
    public List<Integer> getAssignments() {
        List<Integer> assignmentIds = gradeJpaRepository.findAllAssignmentIds(); // Fetch all unique assignment IDs
        return assignmentIds;  // Return list of assignment IDs
    }




    // Fetch grades by assignment ID
    @GetMapping("/assignment/{assignment_id}/grades")
    public ResponseEntity<GradeStatistics> getGradesByAssignment(
        @PathVariable("assignment_id") Long assignmentId,  
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Fetch grades associated with the assignment ID from the database
        Optional<Assignment> assignment = assignmentJpaRepository.findById(assignmentId);
        if (!assignment.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such assignment exists");
        }

        List<SynergyGrade> grades = gradeJpaRepository.findByAssignment(assignment.get());

        // Extract grades from the list of Grade objects
        List<Double> gradeValues = new ArrayList<>();
        for (SynergyGrade grade : grades) {
            gradeValues.add(grade.getGrade());
        }

        // Convert list to array for statistical calculations
        double[] gradesArray = gradeValues.stream().mapToDouble(i -> i).toArray();

        // Calculate statistical values
        double mean = calculateMean(gradesArray);
        double stdDev = calculateStandardDeviation(gradesArray, mean);
        double median = calculateMedian(gradeValues);
        double q1 = calculateQuartile(gradeValues, 25);
        double q3 = calculateQuartile(gradeValues, 75);

        // Create and return GradeStatistics object
        GradeStatistics stats = new GradeStatistics(gradesArray, mean, stdDev, median, q1, q3);
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }


    @GetMapping("/assignment/{assignment_id}/student/grade")
    public ResponseEntity<Double> getStudentGradeForAssignment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable("assignment_id") Long assignmentId
    ) {
    // Create a logger instance
    Logger logger = LoggerFactory.getLogger(getClass());

    // Log the user details
    logger.info("Request received for user: {}", userDetails.getUsername());

    String uid = userDetails.getUsername();
    Person user = personRepository.findByUid(uid);

    if (user == null) {
        logger.error("User not found with email: {}", uid);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with uid: " + uid);
    }

    // Log the user ID
    logger.info("User found with ID: {}", user.getId());

    // Get the student ID
    Long studentId = user.getId();

    Optional<Assignment> assignment = assignmentJpaRepository.findById(assignmentId);
    if (assignment.isEmpty()) {
        logger.error("Assignment not found with ID: {}", assignmentId);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
    }

    // Log the assignment details
    logger.info("Assignment found with ID: {}", assignmentId);

    // Find the grade using the gradeJpaRepository (SynergyGradeJpaRepository)
    Optional<SynergyGrade> synergyGrade = gradeJpaRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    if (synergyGrade.isEmpty()) {
        logger.error("No grade found for student ID: {} on assignment ID: {}", studentId, assignmentId);
        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No grade found for this student on this assignment.");
    }

    // Log the grade found
    logger.info("Grade for student ID: {} on assignment ID: {} is {}", studentId, assignmentId, synergyGrade.get().getGrade());

    return new ResponseEntity<>(synergyGrade.get().getGrade(), HttpStatus.OK);
}



    // Helper method to calculate mean
    private double calculateMean(double[] grades) {
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        return sum / grades.length;
    }

    // Helper method to calculate standard deviation
    private double calculateStandardDeviation(double[] grades, double mean) {
        double sum = 0;
        for (double grade : grades) {
            sum += Math.pow(grade - mean, 2);
        }
        return Math.sqrt(sum / grades.length);
    }

    // Helper method to calculate median
    private double calculateMedian(List<Double> gradesList) {
        Collections.sort(gradesList);
        int size = gradesList.size();
        if (size % 2 == 0) {
            return (gradesList.get(size / 2 - 1) + gradesList.get(size / 2)) / 2.0;
        } else {
            return gradesList.get(size / 2);
        }
    }


    // Helper method to calculate quartiles (Q1, Q3)
    // Helper method to calculate quartiles
    private double calculateQuartile(List<Double> gradesList, int percentile) {
        Collections.sort(gradesList);
        int index = (int) Math.ceil(percentile / 100.0 * gradesList.size()) - 1;
        return gradesList.get(Math.max(index, 0));
    }


    // GradeStatistics class to hold all statistical data
    public static class GradeStatistics {
        private double[] grades;
        private double mean;
        private double standardDeviation;
        private double median;
        private double q1;
        private double q3;

        public GradeStatistics(double[] grades, double mean, double standardDeviation, double median, double q1, double q3) {
            this.grades = grades;
            this.mean = mean;
            this.standardDeviation = standardDeviation;
            this.median = median;
            this.q1 = q1;
            this.q3 = q3;
        }

        // Getters for the JSON response
        public double[] getGrades() {
            return grades;
        }

        public double getMean() {
            return mean;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public double getMedian() {
            return median;
        }

        public double getQ1() {
            return q1;
        }

        public double getQ3() {
            return q3;
        }
    }
}
