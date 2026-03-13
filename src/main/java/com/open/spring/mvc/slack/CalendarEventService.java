package com.open.spring.mvc.slack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private SlackService slackService;

    private static final int MAX_DAYS_AHEAD = 365;

    // Save a new event
    public CalendarEvent saveEvent(CalendarEvent event) {
        CalendarEvent savedEvent = calendarEventRepository.save(event);
        return savedEvent;
    }

    // Find event by title and date (for duplicate detection)
    public CalendarEvent findByTitleAndDate(String title, LocalDate date) {
        return calendarEventRepository.findByTitleAndDate(title, date).orElse(null);
    }

    // Create a calendar event
    public void createCalendarEvent(String title, LocalDate eventDate, String description, String type, String period) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(title);
        event.setDate(eventDate);
        event.setDescription(description);
        event.setType(type);
        event.setPeriod(period);
        calendarEventRepository.save(event);
    }

    // Get events by a specific date
    public List<CalendarEvent> getEventsByDate(LocalDate date) {
        return calendarEventRepository.findByDate(date);
    }

    // Update event by id
    public boolean updateEventById(int id, String newTitle, String description, LocalDate date, String period) {
        CalendarEvent event = getEventById(id);
        if (event != null) {
            event.setTitle(newTitle);
            event.setDescription(description);
            event.setDate(date);
            event.setPeriod(period);
            calendarEventRepository.save(event);
            return true;
        }
        return false;
    }

    // Update event by id with appointment fields
    public boolean updateEventById(int id, String newTitle, String description, LocalDate date, String period,
                                   String type, String classPeriod, String groupName, String individual) {
        CalendarEvent event = getEventById(id);
        if (event != null) {
            event.setTitle(newTitle);
            event.setDescription(description);
            event.setDate(date);
            event.setPeriod(period);
            event.setType(type != null ? type : "event");
            event.setClassPeriod(classPeriod);
            event.setGroupName(groupName);
            event.setIndividual(individual);
            calendarEventRepository.save(event);
            return true;
        }
        return false;
    }

    // Count appointments by date and classPeriod
    public long countAppointmentsByDateAndClassPeriod(LocalDate date, String classPeriod) {
        return calendarEventRepository.countAppointmentsByDateAndClassPeriod(date, classPeriod);
    }

    // Delete event by id
    public boolean deleteEventById(int id) {
        CalendarEvent event = getEventById(id);
        if (event != null) {
            calendarEventRepository.delete(event);
            return true;
        }
        return false;
    }

    // Delete event by title
    public boolean deleteEventByTitle(String title) {
        List<CalendarEvent> allEvents = calendarEventRepository.findAll(); 
        List<CalendarEvent> eventsToDelete = allEvents.stream()
                .filter(event -> event.getTitle().equals(title))
                .toList();

        if (!eventsToDelete.isEmpty()) {
            eventsToDelete.forEach(calendarEventRepository::delete);
            return true;
        }
        return false;
    }

    // Retrieve all events
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public List<CalendarEvent> getEventsWithinDateRange(LocalDate startDate, LocalDate endDate) {
        return calendarEventRepository.findByDateBetween(startDate, endDate);
    }

    // Get event by id
    public CalendarEvent getEventById(int id) {
        return calendarEventRepository.findById((long) id).orElse(null);
    }

    // Parse Slack message and create events
    public void parseSlackMessage(Map<String, String> jsonMap, LocalDate weekStartDate) {
        List<CalendarEvent> events = extractEventsFromText(jsonMap, weekStartDate);
        for (CalendarEvent event : events) {
            saveEvent(event);
        }
    }

    private final String CSP_CHANNEL_ID = "CUS8E3M6Z";
    private final String CSA_CHANNEL_ID = "CRRJL1F1D";
    private final String CSSE_CHANNEL_ID = "C05MNRWC2A1";

    private List<CalendarEvent> extractEventsFromText(Map<String, String> jsonMap, LocalDate weekStartDate) {
        String text = jsonMap.get("text");
        // Use SlackService to determine the correct week start date from the message
        LocalDate parsedWeekStartDate = null;
        try {
            java.lang.reflect.Method method = SlackService.class.getDeclaredMethod("getWeekStartDateFromMessage", String.class);
            method.setAccessible(true);
            parsedWeekStartDate = (LocalDate) method.invoke(slackService, text);
        } catch (Exception e) {
            // Handle exception or log as needed
        }
        if (parsedWeekStartDate != null) {
            weekStartDate = parsedWeekStartDate;
        }
        List<CalendarEvent> events = new ArrayList<>();
        Pattern dayPattern = Pattern.compile("\\[(Mon|Tue|Wed|Thu|Fri|Sat|Sun)(?: - (Mon|Tue|Wed|Thu|Fri|Sat|Sun))?\\]:\\s*(\\*\\*|\\*)?\\s*(.+)");
        Pattern descriptionPattern = Pattern.compile("(\\*\\*|\\*)?\\s*\\u2022\\s*(.+)");
        String[] lines = text.split("\\n");
        CalendarEvent lastEvent = null;
        List<CalendarEvent> lastEventRange = new ArrayList<>();

        for (String line : lines) {
            Matcher dayMatcher = dayPattern.matcher(line);

            if (dayMatcher.find()) {
                String startDay = dayMatcher.group(1);
                String endDay = dayMatcher.group(2) != null ? dayMatcher.group(2) : startDay;
                String asterisks = dayMatcher.group(3);
                String currentTitle = dayMatcher.group(4).trim();
                String period = "0";
                switch(jsonMap.get("channel")) {
                    case CSP_CHANNEL_ID:
                        period = "CSP";
                        break;
                    case CSA_CHANNEL_ID:
                        period = "CSA";
                        break;
                    case CSSE_CHANNEL_ID:
                        period = "CSSE";
                        break;
                }

                String type = "daily plan";
                if ("*".equals(asterisks)) {
                    type = "check-in";
                } else if ("**".equals(asterisks)) {
                    type = "grade";
                }

                // Find description for this line (if any)
                Matcher descMatcher = descriptionPattern.matcher(line);
                String description = "";
                if (descMatcher.find()) {
                    description = descMatcher.group(2).trim();
                }

                lastEventRange.clear(); // Clear previous range
                for (LocalDate date : getDatesInRange(startDay, endDay, weekStartDate)) {
                    CalendarEvent event = new CalendarEvent(date, currentTitle, description, type, period);
                    events.add(event);
                    lastEvent = event; // Update lastEvent to the current event
                    lastEventRange.add(event); // Add to current range
                }
            } else {
                Matcher descMatcher = descriptionPattern.matcher(line);
                if (descMatcher.find() && !lastEventRange.isEmpty()) {
                    String description = descMatcher.group(2).trim();
                    String asterisks = descMatcher.group(1);

                    String type = lastEvent.getType();
                    if ("*".equals(asterisks)) {
                        type = "check-in";
                    } else if ("**".equals(asterisks)) {
                        type = "grade";
                    }

                    for (CalendarEvent event : lastEventRange) {
                        event.setDescription(event.getDescription() +
                                (event.getDescription().isEmpty() ? "" : ", ") +
                                description);
                        event.setType(type);
                    }
                }
            }
        }

        // Log the events and their descriptions
        for (CalendarEvent event : events) {
            System.out.println("Event: " + event.getDate() + ", Title: " + event.getTitle() + ", Description: " + event.getDescription());
        }

        return events;
    }

    private List<LocalDate> getDatesInRange(String startDay, String endDay, LocalDate weekStartDate) {
        List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        int startIndex = days.indexOf(startDay);
        int endIndex = days.indexOf(endDay);

        List<LocalDate> dateRange = new ArrayList<>();
        if (startIndex != -1 && endIndex != -1) {
            for (int i = startIndex; i <= endIndex; i++) {
                dateRange.add(weekStartDate.plusDays(i - weekStartDate.getDayOfWeek().getValue() + 1));
            }
        }
        return dateRange;
    }

    // ==================== BREAK-RELATED METHODS ====================

    /**
     * Create a break for a specific date.
     * Moves all regular events from that date to the next non-break day.
     * 
     * @param date The date of the break
     * @param name The name of the break
     * @param description Description of the break
     * @param moveToNextNonBreakDay Whether to move events to next non-break day
     * @return The created break CalendarEvent
     */
    @Transactional
    public CalendarEvent createBreak(LocalDate date, String name, String description, boolean moveToNextNonBreakDay) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            name = "Break";
        }
        if (description == null) {
            description = "";
        }

        // Check if a break already exists for this date
        List<CalendarEvent> existingBreaks = calendarEventRepository.findByIsBreakAndDate(true, date);
        if (!existingBreaks.isEmpty()) {
            // Break already exists, just return it
            return existingBreaks.get(0);
        }

        // Move all regular events from this date if requested
        if (moveToNextNonBreakDay) {
            List<CalendarEvent> eventsOnDate = calendarEventRepository.findByDate(date);
            LocalDate nextNonBreakDay = findNextNonBreakDay(date);

            for (CalendarEvent event : eventsOnDate) {
                if (!event.isBreak()) { // Only move regular events, not breaks
                    event.setDate(nextNonBreakDay);
                    calendarEventRepository.save(event);
                }
            }
        }

        // Create and save the break as a CalendarEvent
        CalendarEvent breakEvent = new CalendarEvent(date, name, description, true);
        return calendarEventRepository.save(breakEvent);
    }

    /**
     * Create a break with name and description.
     * 
     * @param date The date of the break
     * @param name The name of the break
     * @param description Description of the break
     * @return The created break CalendarEvent
     */
    public CalendarEvent createBreak(LocalDate date, String name, String description) {
        return createBreak(date, name, description, false);
    }

    /**
     * Create a break with a simple name.
     * 
     * @param date The date of the break
     * @param name The name of the break
     * @return The created break CalendarEvent
     */
    public CalendarEvent createBreak(LocalDate date, String name) {
        return createBreak(date, name, "", false);
    }

    /**
     * Create a break with default name.
     * 
     * @param date The date of the break
     * @return The created break CalendarEvent
     */
    public CalendarEvent createBreak(LocalDate date) {
        return createBreak(date, "Break", "", false);
    }

    /**
     * Find the next non-break day starting from the given date.
     * Skips the break day itself and checks subsequent days.
     * 
     * @param date The starting date
     * @return The first date that doesn't have a break
     */
    public LocalDate findNextNonBreakDay(LocalDate date) {
        LocalDate currentDate = date.plusDays(1); // Start from the next day
        int daysChecked = 0;

        while (daysChecked < MAX_DAYS_AHEAD) {
            if (!isBreakDay(currentDate)) {
                return currentDate;
            }
            currentDate = currentDate.plusDays(1);
            daysChecked++;
        }

        // Fallback: return a date 365 days from now if no non-break day found
        return date.plusDays(MAX_DAYS_AHEAD);
    }

    /**
     * Update a break's name and description.
     * 
     * @param id The ID of the break to update
     * @param name The new name
     * @param description The new description
     * @return The updated break or null if not found
     */
    @Transactional
    public CalendarEvent updateBreak(Long id, String name, String description) {
        CalendarEvent breakEvent = getEventById(Math.toIntExact(id));
        if (breakEvent != null && breakEvent.isBreak()) {
            if (name != null && !name.trim().isEmpty()) {
                breakEvent.setName(name);
                breakEvent.setTitle(name);
            }
            if (description != null) {
                breakEvent.setDescription(description);
            }
            return calendarEventRepository.save(breakEvent);
        }
        return null;
    }

    /**
     * Delete a break by its ID.
     * Does NOT move events back (they stay on their rescheduled dates).
     * 
     * @param id The ID of the break to delete
     * @return true if the break was deleted, false if not found
     */
    @Transactional
    public boolean deleteBreakById(Long id) {
        CalendarEvent breakEvent = getEventById(Math.toIntExact(id));
        if (breakEvent != null && breakEvent.isBreak()) {
            calendarEventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Delete a break by date.
     * 
     * @param date The date of the break to delete
     * @return true if a break was deleted, false if not found
     */
    @Transactional
    public boolean deleteBreakByDate(LocalDate date) {
        List<CalendarEvent> breaks = calendarEventRepository.findByIsBreakAndDate(true, date);
        if (!breaks.isEmpty()) {
            for (CalendarEvent breakEvent : breaks) {
                calendarEventRepository.delete(breakEvent);
            }
            return true;
        }
        return false;
    }

    /**
     * Get all breaks for a specific date.
     * 
     * @param date The date to check
     * @return List of breaks for that date
     */
    public List<CalendarEvent> getBreaksByDate(LocalDate date) {
        return calendarEventRepository.findByIsBreakAndDate(true, date);
    }

    /**
     * Get all breaks.
     * 
     * @return List of all breaks
     */
    public List<CalendarEvent> getAllBreaks() {
        return calendarEventRepository.findByIsBreak(true);
    }

    /**
     * Check if a date is a break day.
     * 
     * @param date The date to check
     * @return true if there is a break on this date, false otherwise
     */
    public boolean isBreakDay(LocalDate date) {
        List<CalendarEvent> breaks = calendarEventRepository.findByIsBreakAndDate(true, date);
        return !breaks.isEmpty();
    }

    /**
     * Get a break by ID.
     * 
     * @param id The ID of the break
     * @return The break if found, null otherwise
     */
    public CalendarEvent getBreakById(Long id) {
        CalendarEvent event = getEventById(Math.toIntExact(id));
        if (event != null && event.isBreak()) {
            return event;
        }
        return null;
    }
}
