package com.open.spring.mvc.sprintDates;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import lombok.Getter;
import lombok.Setter;

/**
 * REST API Controller for managing Sprint Dates.
 * Handles CRUD operations and automatic calendar event synchronization.
 */
@RestController
@RequestMapping("/api/sprint-dates")
public class SprintDateApiController {

    @Autowired
    private SprintDateJpaRepository sprintDateRepository;

    @Autowired
    private SprintDateService sprintDateService;

    @Autowired
    private PersonJpaRepository personRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * DTO for receiving sprint date data from the frontend
     */
    @Getter
    @Setter
    public static class SprintDateDto {
        private String course;
        private String sprintKey;
        private String sprintTitle;
        private String startDate;
        private String endDate;
        private Integer startWeek;
        private Integer endWeek;
        private Object weekAssignments; // Can be Map or JSON string
    }

    /**
     * GET /api/sprint-dates
     * Returns all sprint dates for all courses.
     * 
     * @return ResponseEntity containing list of all sprint dates
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllSprintDates() {
        try {
            List<SprintDate> sprintDates = sprintDateRepository.findAllByOrderByCourseAscSprintKeyAsc();
            return new ResponseEntity<>(sprintDates, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error fetching sprint dates: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * GET /api/sprint-dates/{course}
     * Returns all sprint dates for a specific course.
     * 
     * @param course The course code (e.g., "csa", "csp", "csse")
     * @return ResponseEntity containing list of sprint dates for that course
     */
    @GetMapping(value = "/{course}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSprintDatesByCourse(@PathVariable String course) {
        try {
            List<SprintDate> sprintDates = sprintDateRepository.findByCourseOrderByStartWeekAsc(course.toLowerCase());
            return new ResponseEntity<>(sprintDates, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error fetching sprint dates: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * GET /api/sprint-dates/{course}/{sprintKey}
     * Returns a specific sprint's dates.
     * 
     * @param course The course code
     * @param sprintKey The sprint identifier (e.g., "Sprint1")
     * @return ResponseEntity containing the sprint date or 404
     */
    @GetMapping(value = "/{course}/{sprintKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSprintDate(
            @PathVariable String course,
            @PathVariable String sprintKey) {
        try {
            Optional<SprintDate> sprintDate = sprintDateRepository.findByCourseAndSprintKey(
                course.toLowerCase(), 
                sprintKey
            );
            
            if (sprintDate.isPresent()) {
                return new ResponseEntity<>(sprintDate.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                    Map.of("error", "Sprint date not found for " + course + "/" + sprintKey),
                    HttpStatus.NOT_FOUND
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error fetching sprint date: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * POST /api/sprint-dates
     * Creates a new sprint date entry and automatically creates calendar events.
     * 
     * @param userDetails The authenticated user (optional)
     * @param dto The sprint date data
     * @return ResponseEntity containing the created sprint date
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> createSprintDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SprintDateDto dto) {
        try {
            // Validate input
            String validationError = validateDto(dto);
            if (validationError != null) {
                return new ResponseEntity<>(
                    Map.of("error", validationError),
                    HttpStatus.BAD_REQUEST
                );
            }

            String course = dto.getCourse().toLowerCase();
            String sprintKey = dto.getSprintKey();

            // Check if sprint date already exists
            if (sprintDateRepository.existsByCourseAndSprintKey(course, sprintKey)) {
                return new ResponseEntity<>(
                    Map.of("error", "Sprint date already exists for " + course + "/" + sprintKey + ". Use PUT to update."),
                    HttpStatus.CONFLICT
                );
            }

            // Create the sprint date entity
            SprintDate sprintDate = new SprintDate();
            sprintDate.setCourse(course);
            sprintDate.setSprintKey(sprintKey);
            sprintDate.setSprintTitle(dto.getSprintTitle());
            sprintDate.setStartDate(LocalDate.parse(dto.getStartDate()));
            sprintDate.setEndDate(LocalDate.parse(dto.getEndDate()));
            sprintDate.setStartWeek(dto.getStartWeek());
            sprintDate.setEndWeek(dto.getEndWeek());
            
            // Handle week assignments (can be Map or JSON string)
            String weekAssignmentsJson = convertWeekAssignmentsToJson(dto.getWeekAssignments());
            sprintDate.setWeekAssignments(weekAssignmentsJson);

            // Set created by if authenticated
            if (userDetails != null) {
                Person person = personRepository.findByUid(userDetails.getUsername());
                if (person != null) {
                    sprintDate.setCreatedBy(person);
                }
            }

            // Validate the entity
            sprintDate.validate();

            // Save first to get ID
            SprintDate savedSprintDate = sprintDateRepository.save(sprintDate);

            // Create calendar events
            List<Long> eventIds = sprintDateService.createSprintCalendarEvents(savedSprintDate);
            savedSprintDate.setCalendarEventIds(sprintDateService.eventIdsToJson(eventIds));
            
            // Save again with event IDs
            savedSprintDate = sprintDateRepository.save(savedSprintDate);

            // Build response with event count
            Map<String, Object> response = buildResponse(savedSprintDate);
            response.put("eventsCreated", eventIds.size());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                Map.of("error", e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error creating sprint date: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * PUT /api/sprint-dates/{course}/{sprintKey}
     * Updates an existing sprint date entry.
     * Deletes old calendar events and creates new ones.
     * 
     * @param userDetails The authenticated user (optional)
     * @param course The course code
     * @param sprintKey The sprint identifier
     * @param dto The updated sprint date data
     * @return ResponseEntity containing the updated sprint date
     */
    @PutMapping(value = "/{course}/{sprintKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> updateSprintDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String course,
            @PathVariable String sprintKey,
            @RequestBody SprintDateDto dto) {
        try {
            String normalizedCourse = course.toLowerCase();

            // Find existing sprint date
            Optional<SprintDate> existingOpt = sprintDateRepository.findByCourseAndSprintKey(
                normalizedCourse, 
                sprintKey
            );

            SprintDate sprintDate;
            boolean isNew = false;

            if (existingOpt.isPresent()) {
                sprintDate = existingOpt.get();
                
                // Delete old calendar events
                sprintDateService.deleteSprintCalendarEvents(sprintDate);
            } else {
                // Create new if doesn't exist
                sprintDate = new SprintDate();
                sprintDate.setCourse(normalizedCourse);
                sprintDate.setSprintKey(sprintKey);
                isNew = true;
            }

            // Update fields from DTO
            if (dto.getSprintTitle() != null) {
                sprintDate.setSprintTitle(dto.getSprintTitle());
            }
            if (dto.getStartDate() != null) {
                sprintDate.setStartDate(LocalDate.parse(dto.getStartDate()));
            }
            if (dto.getEndDate() != null) {
                sprintDate.setEndDate(LocalDate.parse(dto.getEndDate()));
            }
            if (dto.getStartWeek() != null) {
                sprintDate.setStartWeek(dto.getStartWeek());
            }
            if (dto.getEndWeek() != null) {
                sprintDate.setEndWeek(dto.getEndWeek());
            }
            if (dto.getWeekAssignments() != null) {
                String weekAssignmentsJson = convertWeekAssignmentsToJson(dto.getWeekAssignments());
                sprintDate.setWeekAssignments(weekAssignmentsJson);
            }

            // Update created by if authenticated
            if (userDetails != null) {
                Person person = personRepository.findByUid(userDetails.getUsername());
                if (person != null) {
                    sprintDate.setCreatedBy(person);
                }
            }

            // Validate
            sprintDate.validate();

            // Save
            SprintDate savedSprintDate = sprintDateRepository.save(sprintDate);

            // Create new calendar events
            List<Long> eventIds = sprintDateService.createSprintCalendarEvents(savedSprintDate);
            savedSprintDate.setCalendarEventIds(sprintDateService.eventIdsToJson(eventIds));
            
            // Save again with event IDs
            savedSprintDate = sprintDateRepository.save(savedSprintDate);

            // Build response
            Map<String, Object> response = buildResponse(savedSprintDate);
            response.put("eventsCreated", eventIds.size());

            return new ResponseEntity<>(response, isNew ? HttpStatus.CREATED : HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                Map.of("error", e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error updating sprint date: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * DELETE /api/sprint-dates/{course}/{sprintKey}
     * Deletes a sprint date entry and its associated calendar events.
     * 
     * @param course The course code
     * @param sprintKey The sprint identifier
     * @return ResponseEntity with success message
     */
    @DeleteMapping(value = "/{course}/{sprintKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> deleteSprintDate(
            @PathVariable String course,
            @PathVariable String sprintKey) {
        try {
            String normalizedCourse = course.toLowerCase();

            // Find existing sprint date
            Optional<SprintDate> existingOpt = sprintDateRepository.findByCourseAndSprintKey(
                normalizedCourse, 
                sprintKey
            );

            if (existingOpt.isEmpty()) {
                return new ResponseEntity<>(
                    Map.of("error", "Sprint date not found for " + course + "/" + sprintKey),
                    HttpStatus.NOT_FOUND
                );
            }

            SprintDate sprintDate = existingOpt.get();

            // Delete associated calendar events
            sprintDateService.deleteSprintCalendarEvents(sprintDate);

            // Delete the sprint date
            sprintDateRepository.delete(sprintDate);

            return new ResponseEntity<>(
                Map.of(
                    "message", "Sprint date deleted successfully",
                    "course", course,
                    "sprintKey", sprintKey
                ),
                HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("error", "Error deleting sprint date: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Validate the DTO for required fields
     */
    private String validateDto(SprintDateDto dto) {
        if (dto.getCourse() == null || dto.getCourse().trim().isEmpty()) {
            return "Course is required";
        }
        if (dto.getSprintKey() == null || dto.getSprintKey().trim().isEmpty()) {
            return "Sprint key is required";
        }
        if (dto.getStartDate() == null || dto.getStartDate().trim().isEmpty()) {
            return "Start date is required";
        }
        if (dto.getEndDate() == null || dto.getEndDate().trim().isEmpty()) {
            return "End date is required";
        }
        if (dto.getStartWeek() == null) {
            return "Start week is required";
        }
        if (dto.getEndWeek() == null) {
            return "End week is required";
        }

        // Validate date format
        try {
            LocalDate.parse(dto.getStartDate());
        } catch (Exception e) {
            return "Invalid start date format. Use YYYY-MM-DD";
        }
        try {
            LocalDate.parse(dto.getEndDate());
        } catch (Exception e) {
            return "Invalid end date format. Use YYYY-MM-DD";
        }

        return null; // No error
    }

    /**
     * Convert weekAssignments from various formats to JSON string
     */
    @SuppressWarnings("unchecked")
    private String convertWeekAssignmentsToJson(Object weekAssignments) {
        if (weekAssignments == null) {
            return "{}";
        }
        if (weekAssignments instanceof String) {
            return (String) weekAssignments;
        }
        if (weekAssignments instanceof Map) {
            try {
                return objectMapper.writeValueAsString(weekAssignments);
            } catch (JsonProcessingException e) {
                return "{}";
            }
        }
        return "{}";
    }

    /**
     * Build response map from SprintDate entity
     */
    private Map<String, Object> buildResponse(SprintDate sprintDate) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", sprintDate.getId());
        response.put("course", sprintDate.getCourse());
        response.put("sprintKey", sprintDate.getSprintKey());
        response.put("sprintTitle", sprintDate.getSprintTitle());
        response.put("startDate", sprintDate.getStartDate().toString());
        response.put("endDate", sprintDate.getEndDate().toString());
        response.put("startWeek", sprintDate.getStartWeek());
        response.put("endWeek", sprintDate.getEndWeek());
        response.put("weekAssignments", sprintDate.getWeekAssignments());
        response.put("calendarEventIds", sprintDate.getCalendarEventIds());
        response.put("createdAt", sprintDate.getCreatedAt() != null ? sprintDate.getCreatedAt().toString() : null);
        response.put("updatedAt", sprintDate.getUpdatedAt() != null ? sprintDate.getUpdatedAt().toString() : null);
        return response;
    }
}
