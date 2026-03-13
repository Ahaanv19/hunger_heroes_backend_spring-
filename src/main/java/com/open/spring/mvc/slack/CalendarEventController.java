package com.open.spring.mvc.slack;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/calendar")
public class CalendarEventController {

    @Autowired
    private CalendarEventService calendarEventService;

    /**
     * DTO for bulk event creation request
     */
    @Getter
    @Setter
    public static class BulkEventsRequest {
        private List<Map<String, String>> events;
    }

    /**
     * DTO for bulk event creation response
     */
    @Getter
    @Setter
    public static class BulkEventsResponse {
        private boolean success;
        private int created;
        private int updated;
        private int failed;
        private List<CalendarEvent> events;
        private List<String> errors;

        public BulkEventsResponse() {
            this.events = new ArrayList<>();
            this.errors = new ArrayList<>();
        }
    }

    /**
     * POST /api/calendar/add_events
     * Bulk create calendar events - accepts { events: [...] } format
     * Returns detailed response with created/updated/failed counts
     */
    @PostMapping("/add_events")
    public ResponseEntity<BulkEventsResponse> addEvents(@RequestBody BulkEventsRequest request) {
        BulkEventsResponse response = new BulkEventsResponse();
        
        if (request.getEvents() == null || request.getEvents().isEmpty()) {
            response.setSuccess(false);
            response.getErrors().add("No events provided");
            return ResponseEntity.badRequest().body(response);
        }

        for (Map<String, String> eventMap : request.getEvents()) {
            try {
                String title = eventMap.get("title");
                String dateStr = eventMap.get("date");
                String description = eventMap.getOrDefault("description", "");
                String type = eventMap.getOrDefault("type", "event");
                String period = eventMap.get("period");
                String classPeriod = eventMap.getOrDefault("classPeriod", "");
                String groupName = eventMap.getOrDefault("groupName", "");
                String individual = eventMap.getOrDefault("individual", "");

                if (title == null || title.trim().isEmpty() || dateStr == null || dateStr.trim().isEmpty()) {
                    response.setFailed(response.getFailed() + 1);
                    response.getErrors().add("Missing title or date for event");
                    continue;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    response.setFailed(response.getFailed() + 1);
                    response.getErrors().add("Invalid date format for: " + title);
                    continue;
                }

                // Check for duplicate (same title and date)
                CalendarEvent existingEvent = calendarEventService.findByTitleAndDate(title.trim(), date);
                if (existingEvent != null) {
                    // Update existing event
                    existingEvent.setDescription(description);
                    existingEvent.setType(type);
                    existingEvent.setPeriod(period);
                    existingEvent.setClassPeriod(classPeriod);
                    existingEvent.setGroupName(groupName);
                    existingEvent.setIndividual(individual);
                    CalendarEvent updatedEvent = calendarEventService.saveEvent(existingEvent);
                    response.getEvents().add(updatedEvent);
                    response.setUpdated(response.getUpdated() + 1);
                } else {
                    // Create new event
                    CalendarEvent event = new CalendarEvent(date, title.trim(), description, type, period, classPeriod, groupName, individual);
                    CalendarEvent savedEvent = calendarEventService.saveEvent(event);
                    response.getEvents().add(savedEvent);
                    response.setCreated(response.getCreated() + 1);
                }
            } catch (Exception e) {
                response.setFailed(response.getFailed() + 1);
                response.getErrors().add("Error processing event: " + e.getMessage());
            }
        }

        response.setSuccess(response.getFailed() == 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public void addEventsFromSlackMessage(@RequestBody Map<String, String> jsonMap) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate weekStartDate = LocalDate.of(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        calendarEventService.parseSlackMessage(jsonMap, weekStartDate);
    }

    @PostMapping("/add_bulk")
    public void addBulkEvents(@RequestBody List<Map<String, String>> events) {
        for (Map<String, String> eventMap : events) {
            String dateStr = eventMap.get("date");
            String title = eventMap.get("title");
            String description = eventMap.get("description");
            String type = eventMap.getOrDefault("type", "event");
            String period = eventMap.get("period");
            String classPeriod = eventMap.getOrDefault("classPeriod", "");
            String groupName = eventMap.getOrDefault("groupName", "");
            String individual = eventMap.getOrDefault("individual", "");

            LocalDate date = LocalDate.parse(dateStr);
            CalendarEvent event = new CalendarEvent(date, title, description, type, period, classPeriod, groupName, individual);
            calendarEventService.saveEvent(event);
        }
    }

    @PostMapping("/add_event")
    public ResponseEntity<Object> addEvent(@RequestBody Map<String, String> jsonMap,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> errorResponse = new HashMap<>();
        try {
            String title = jsonMap.get("title");
            String dateStr = jsonMap.get("date");

            if (title == null || title.trim().isEmpty()) {
                errorResponse.put("message", "Invalid input: 'title' cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (dateStr == null || dateStr.trim().isEmpty()) {
                errorResponse.put("message", "Invalid input: 'date' cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                errorResponse.put("message", "Invalid date format. Use YYYY-MM-DD.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String description = jsonMap.getOrDefault("description", "");
            String type = jsonMap.getOrDefault("type", "event");
            String period = jsonMap.get("period"); // Might be null
            String classPeriod = jsonMap.getOrDefault("classPeriod", "");
            String groupName = jsonMap.getOrDefault("groupName", "");
            String individual = jsonMap.getOrDefault("individual", "");

            // Validation for appointments
            if ("appointment".equals(type)) {
                // Require authentication for appointments
                if (userDetails == null) {
                    errorResponse.put("message", "Must be logged in to create appointments");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                }

                // Check appointment limit per classPeriod per date
                if (classPeriod != null && !classPeriod.isEmpty()) {
                    long count = calendarEventService.countAppointmentsByDateAndClassPeriod(date, classPeriod);
                    if (count >= 4) {
                        errorResponse.put("message", "Maximum 4 appointments allowed per period per day");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }
                }
            }

            // Check for duplicate (same title and date)
            CalendarEvent existingEvent = calendarEventService.findByTitleAndDate(title.trim(), date);
            if (existingEvent != null) {
                // Update existing event instead of creating duplicate
                existingEvent.setDescription(description);
                existingEvent.setType(type);
                existingEvent.setPeriod(period);
                existingEvent.setClassPeriod(classPeriod);
                existingEvent.setGroupName(groupName);
                existingEvent.setIndividual(individual);
                CalendarEvent updatedEvent = calendarEventService.saveEvent(existingEvent);
                return ResponseEntity.ok(updatedEvent);
            }

            CalendarEvent event = new CalendarEvent(date, title, description, type, period, classPeriod, groupName, individual);
            CalendarEvent savedEvent = calendarEventService.saveEvent(event);

            // Return the full event object with id
            return ResponseEntity.ok(savedEvent);
        } catch (Exception e) {
            errorResponse.put("message", "Error adding event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/events/{date}")
    public List<CalendarEvent> getEventsByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return calendarEventService.getEventsByDate(localDate);
    }

    @PutMapping("/edit/{id}")
    @CrossOrigin(origins = {"http://127.0.0.1:4500","https://pages.opencodingsociety.com"}, allowCredentials = "true")
    public ResponseEntity<Object> editEvent(@PathVariable int id, @RequestBody Map<String, String> payload,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> errorResponse = new HashMap<>();
        try {
            String newTitle = payload.get("newTitle");
            String description = payload.get("description");
            String dateStr = payload.get("date");
            String period = payload.get("period");
            String type = payload.getOrDefault("type", "event");
            String classPeriod = payload.getOrDefault("classPeriod", "");
            String groupName = payload.getOrDefault("groupName", "");
            String individual = payload.getOrDefault("individual", "");

            if (newTitle == null || newTitle.trim().isEmpty()) {
                errorResponse.put("message", "New title cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (description == null || description.trim().isEmpty()) {
                errorResponse.put("message", "Description cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (dateStr == null || dateStr.trim().isEmpty()) {
                errorResponse.put("message", "Date cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (period == null || period.trim().isEmpty()) {
                errorResponse.put("message", "Period cannot be null or empty.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                errorResponse.put("message", "Invalid date format. Use YYYY-MM-DD.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validation for appointments
            if ("appointment".equals(type)) {
                // Require authentication for appointments
                if (userDetails == null) {
                    errorResponse.put("message", "Must be logged in to create appointments");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                }

                // Check appointment limit per classPeriod per date (exclude current event)
                if (classPeriod != null && !classPeriod.isEmpty()) {
                    CalendarEvent existingEvent = calendarEventService.getEventById(id);
                    // Only check limit if classPeriod or date changed
                    boolean needsLimitCheck = existingEvent == null 
                        || !classPeriod.equals(existingEvent.getClassPeriod())
                        || !date.equals(existingEvent.getDate())
                        || !"appointment".equals(existingEvent.getType());
                    
                    if (needsLimitCheck) {
                        long count = calendarEventService.countAppointmentsByDateAndClassPeriod(date, classPeriod);
                        if (count >= 4) {
                            errorResponse.put("message", "Maximum 4 appointments allowed per period per day");
                            return ResponseEntity.badRequest().body(errorResponse);
                        }
                    }
                }
            }

            boolean updated = calendarEventService.updateEventById(id, newTitle.trim(), description.trim(), date, 
                    period.trim(), type, classPeriod, groupName, individual);
            
            if (updated) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "Event updated successfully.");
                return ResponseEntity.ok(successResponse);
            } else {
                errorResponse.put("message", "Event with the given id not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            errorResponse.put("message", "An error occurred while updating the event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/events")
    public List<CalendarEvent> getAllEvents() {
        return calendarEventService.getAllEvents();
    }

    @GetMapping("/events/range")
    public List<CalendarEvent> getEventsWithinDateRange(@RequestParam String start, @RequestParam String end) {
        return calendarEventService.getEventsWithinDateRange(LocalDate.parse(start), LocalDate.parse(end));
    }

    @GetMapping("/events/next-day")
    public List<CalendarEvent> getNextDayEvents() {
        return calendarEventService.getEventsByDate(LocalDate.now().plusDays(1));
    }

    /**
     * GET /api/calendar/appointments/count
     * Returns the count of appointments for a specific date and classPeriod.
     * Allows frontend to check availability before submission.
     * 
     * @param date The date in YYYY-MM-DD format
     * @param classPeriod The class period (P1, P2, P3, P4, P5)
     * @return ResponseEntity containing the count
     */
    @GetMapping("/appointments/count")
    public ResponseEntity<Object> getAppointmentCount(@RequestParam String date, @RequestParam String classPeriod) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            long count = calendarEventService.countAppointmentsByDateAndClassPeriod(parsedDate, classPeriod);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Invalid date format. Use YYYY-MM-DD.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/delete/{id}")
    @CrossOrigin(origins = {"http://127.0.0.1:4500","https://pages.opencodingsociety.com/"}, allowCredentials = "true")
    public ResponseEntity<String> deleteEvent(@PathVariable int id) {
        System.out.println("Attempting to delete event...");
        try {
            boolean deleted = calendarEventService.deleteEventById(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with the given id not found.");
            }
            return ResponseEntity.ok("Event deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/events/bulk")
    public ResponseEntity<List<CalendarEvent>> bulkExtractEvents(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<CalendarEvent> events;
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                events = calendarEventService.getEventsWithinDateRange(start, end);
            } else {
                events = calendarEventService.getAllEvents();
            }
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * DTO for bulk delete request
     */
    @Getter
    @Setter
    public static class BulkDeleteRequest {
        private List<String> titles;
    }

    /**
     * DTO for single delete by title request
     */
    @Getter
    @Setter
    public static class DeleteByTitleRequest {
        private String title;
    }

    /**
     * DTO for bulk delete response
     */
    @Getter
    @Setter
    public static class BulkDeleteResponse {
        private boolean success;
        private int deleted;
        private int notFound;
        private List<String> errors;

        public BulkDeleteResponse() {
            this.errors = new ArrayList<>();
        }
    }

    /**
     * DELETE /api/calendar/delete_events
     * Bulk delete calendar events by titles
     * Accepts { titles: ["...", "..."] }
     */
    @DeleteMapping("/delete_events")
    public ResponseEntity<BulkDeleteResponse> deleteEvents(@RequestBody BulkDeleteRequest request) {
        BulkDeleteResponse response = new BulkDeleteResponse();

        if (request.getTitles() == null || request.getTitles().isEmpty()) {
            response.setSuccess(false);
            response.getErrors().add("No titles provided");
            return ResponseEntity.badRequest().body(response);
        }

        for (String title : request.getTitles()) {
            try {
                if (title == null || title.trim().isEmpty()) {
                    response.setNotFound(response.getNotFound() + 1);
                    continue;
                }

                boolean deleted = calendarEventService.deleteEventByTitle(title.trim());
                if (deleted) {
                    response.setDeleted(response.getDeleted() + 1);
                } else {
                    response.setNotFound(response.getNotFound() + 1);
                }
            } catch (Exception e) {
                response.getErrors().add("Error deleting: " + title + " - " + e.getMessage());
            }
        }

        response.setSuccess(response.getErrors().isEmpty());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/calendar/delete_event
     * Delete a single calendar event by title
     * Accepts { title: "..." }
     */
    @DeleteMapping("/delete_event")
    public ResponseEntity<Map<String, Object>> deleteEventByTitle(@RequestBody DeleteByTitleRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Title cannot be null or empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean deleted = calendarEventService.deleteEventByTitle(request.getTitle().trim());
            if (deleted) {
                response.put("success", true);
                response.put("message", "Event deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Event not found with title: " + request.getTitle());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== BREAK-RELATED ENDPOINTS ====================

    /**
     * POST /api/calendar/breaks/create
     * Create a new break for a specific date.
     * When a break is created, all regular events on that day can be moved to the next non-break day.
     */
    @PostMapping("/breaks/create")
    public ResponseEntity<?> createBreak(@RequestBody Map<String, Object> payload) {
        try {
            String dateStr = (String) payload.get("date");
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");
            Boolean moveToNextNonBreakDay = (Boolean) payload.getOrDefault("moveToNextNonBreakDay", true);

            // Validate date
            if (dateStr == null || dateStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Date is required"));
            }

            // Validate name
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Break name is required"));
            }

            LocalDate date = LocalDate.parse(dateStr);

            // Handle null description
            if (description == null) {
                description = "";
            }

            CalendarEvent breakEvent = calendarEventService.createBreak(date, name, description, moveToNextNonBreakDay);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Break created successfully",
                "break", breakEvent
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid date format. Use YYYY-MM-DD"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to create break: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/calendar/breaks
     * Get all breaks.
     */
    @GetMapping("/breaks")
    public ResponseEntity<?> getAllBreaks() {
        try {
            List<CalendarEvent> breaks = calendarEventService.getAllBreaks();
            return ResponseEntity.ok(breaks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to retrieve breaks: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/calendar/breaks/by-date?date=2026-02-01
     * Get breaks for a specific date.
     */
    @GetMapping("/breaks/by-date")
    public ResponseEntity<?> getBreaksByDate(
            @RequestParam("date") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<CalendarEvent> breaks = calendarEventService.getBreaksByDate(date);
            return ResponseEntity.ok(breaks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to retrieve breaks: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/calendar/breaks/is-break-day?date=2026-02-01
     * Check if a date is a break day.
     */
    @GetMapping("/breaks/is-break-day")
    public ResponseEntity<?> isBreakDay(
            @RequestParam("date") String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            boolean isBreak = calendarEventService.isBreakDay(date);
            return ResponseEntity.ok(Map.of("isBreakDay", isBreak));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to check break day: " + e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/calendar/breaks/{id}
     * Edit a break's name and description.
     */
    @PutMapping("/breaks/{id}")
    public ResponseEntity<?> editBreak(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");

            // Validate name if provided
            if (name != null && name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Break name cannot be empty"));
            }

            CalendarEvent updatedBreak = calendarEventService.updateBreak(id, name, description);

            if (updatedBreak == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Break not found with ID: " + id
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Break updated successfully",
                "break", updatedBreak
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to update break: " + e.getMessage()
            ));
        }
    }

    /**
     * DELETE /api/calendar/breaks/{id}
     * Delete a break by its ID.
     */
    @DeleteMapping("/breaks/{id}")
    public ResponseEntity<?> deleteBreak(@PathVariable Long id) {
        try {
            boolean deleted = calendarEventService.deleteBreakById(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Break deleted successfully"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Break not found with ID: " + id
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to delete break: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/calendar/breaks/{id}
     * Get a break by its ID.
     */
    @GetMapping("/breaks/{id}")
    public ResponseEntity<?> getBreakById(@PathVariable Long id) {
        try {
            CalendarEvent breakEvent = calendarEventService.getBreakById(id);
            if (breakEvent != null) {
                return ResponseEntity.ok(breakEvent);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Break not found with ID: " + id
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to retrieve break: " + e.getMessage()
            ));
        }
    }
}
