package com.open.spring.mvc.slack;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Registering JPA Repository and table contents
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByDate(LocalDate date); // Method to find calendar events by date
    List<CalendarEvent> findByDateBetween(LocalDate startDate, LocalDate endDate); // Method to find all calendar events between dates
    List<CalendarEvent> findAll();
    Optional<CalendarEvent> findByTitle(String title); // Optional is fine here
    Optional<CalendarEvent> findByTitleAndDate(String title, LocalDate date); // Find duplicate by title and date

    // Count appointments by date and classPeriod for validation
    @Query("SELECT COUNT(e) FROM CalendarEvent e WHERE e.date = :date AND e.classPeriod = :classPeriod AND e.type = 'appointment'")
    long countAppointmentsByDateAndClassPeriod(@Param("date") LocalDate date, @Param("classPeriod") String classPeriod);
    
    // Break-specific query methods
    List<CalendarEvent> findByIsBreakAndDate(boolean isBreak, LocalDate date); // Find breaks by date
    List<CalendarEvent> findByIsBreak(boolean isBreak); // Find all breaks
}
