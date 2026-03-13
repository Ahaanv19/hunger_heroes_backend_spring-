package com.open.spring.mvc.slack;

import java.time.*;
import java.util.regex.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Consolidated service for Slack integration.
 * Handles both message persistence and date parsing from Slack messages.
 */
@Service
public class SlackService {

    @Autowired
    private SlackMessageRepository messageRepository;

    // ========== MESSAGE PERSISTENCE (from MessageService) ==========

    /**
     * Save a Slack message to the database.
     * Creates a new SlackMessage entity with the current timestamp and message content.
     * 
     * @param messageContent The content of the Slack message to save
     */
    public void saveMessage(String messageContent) {
        // Create a new SlackMessage entity with the current timestamp and the message content as a blob
        SlackMessage message = new SlackMessage(LocalDateTime.now(), messageContent);
        // Save to the database
        messageRepository.save(message);
    }

    // ========== DATE PARSING UTILITIES ==========

    /**
     * Extract the week start date from a Slack message.
     * Supports multiple date formats:
     * - "Week of MM/DD/YYYY" (full date)
     * - "Week of MM/DD" (assumes current year)
     * - "Week of the Nth" (assumes current month and year)
     * 
     * @param message The Slack message text containing date information
     * @return The parsed week start date, or current week's Sunday if no date found
     */
    private LocalDate getWeekStartDateFromMessage(String message) {
        LocalDate today = LocalDate.now();
        Pattern fullDatePattern = Pattern.compile("Week of (\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Pattern monthDayPattern = Pattern.compile("Week of (\\d{1,2})/(\\d{1,2})");
        Pattern ordinalPattern = Pattern.compile("Week of the (\\d{1,2})(st|nd|rd|th)");

        Matcher m = fullDatePattern.matcher(message);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            return LocalDate.of(year, month, day);
        }
        m = monthDayPattern.matcher(message);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            int year = today.getYear();
            return LocalDate.of(year, month, day);
        }
        m = ordinalPattern.matcher(message);
        if (m.find()) {
            int day = Integer.parseInt(m.group(1));
            int month = today.getMonthValue();
            int year = today.getYear();
            return LocalDate.of(year, month, day);
        }
        // Default: use current week's Monday
        return today.with(DayOfWeek.SUNDAY);
    }
}