package com.open.spring.mvc.bathroom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Service class to provide statistical computations on Tinkle entries.
 * Includes duration calculations, formatting utilities, and threshold detection.
 */
@Service
public class TinkleStatisticsService {

    /**
     * Calculates the average weekly duration in seconds for each user.
     *
     * @param tinkleList List of Tinkle entries from the database.
     * @return A map where the key is the user's name and the value is their average weekly duration in seconds.
     */
    public Map<String, Long> calculateAverageWeeklyDurations(List<Tinkle> tinkleList) {
        // Group total durations (in seconds) by person name
        Map<String, List<Long>> userWeeklyDurations = tinkleList.stream()
            .filter(tinkle -> tinkle.getPersonName() != null) 
            .collect(Collectors.groupingBy(
                Tinkle::getPersonName,
                Collectors.mapping(
                    t -> calculateTotalDurationInSeconds(t.getTimeIn()),
                    Collectors.toList()
                )
            ));

        // Compute average duration per user
        Map<String, Long> averageWeeklyDurations = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : userWeeklyDurations.entrySet()) {
            String userName = entry.getKey();
            List<Long> durations = entry.getValue();
            long totalDuration = durations.stream().mapToLong(Long::longValue).sum();
            long averageDuration = durations.isEmpty() ? 0 : totalDuration / durations.size();
            averageWeeklyDurations.put(userName, averageDuration);
        }

        return averageWeeklyDurations;
    }

    /**
     * Calculates total duration in seconds from a timeIn string that contains comma-separated start--end datetime pairs.
     *
     * @param timeIn Example format: "2024-05-01 08:00:00--2024-05-01 08:10:00,2024-05-01 10:30:00--2024-05-01 10:45:00"
     * @return Total duration across all pairs in seconds.
     */
    public long calculateTotalDurationInSeconds(String timeIn) {
        if (timeIn == null || timeIn.isEmpty()) return 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return Arrays.stream(timeIn.split(","))
            .mapToLong(pair -> {
                String[] times = pair.split("--");
                if (times.length == 2) {
                    try {
                        LocalDateTime start = LocalDateTime.parse(times[0].trim(), formatter);
                        LocalDateTime end = LocalDateTime.parse(times[1].trim(), formatter);
                        return Duration.between(start, end).getSeconds();
                    } catch (Exception e) {
                        return 0; // Skip malformed time pairs
                    }
                }
                return 0;
            })
            .sum();
    }

    /**
     * Converts a duration in seconds to HH:mm:ss format.
     *
     * @param totalSeconds Total duration in seconds.
     * @return Formatted duration string.
     */
    public String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Calculates average weekly durations and formats the result as HH:mm:ss strings.
     *
     * @param tinkleList List of Tinkle entries.
     * @return Map of user names to formatted average durations.
     */
    public Map<String, String> calculateAverageWeeklyDurationsFormatted(List<Tinkle> tinkleList) {
        Map<String, Long> averageWeeklyDurations = calculateAverageWeeklyDurations(tinkleList);
        return averageWeeklyDurations.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> formatDuration(entry.getValue())
            ));
    }

    /**
     * Returns total formatted duration from a timeIn string.
     *
     * @param timeIn Raw datetime string.
     * @return Formatted total duration in HH:mm:ss.
     */
    public String calculateDurationFormatted(String timeIn) {
        long totalSeconds = calculateTotalDurationInSeconds(timeIn);
        return formatDuration(totalSeconds);
    }

    /**
     * Simplifies the timeIn string by returning HH:mm--HH:mm format for each entry.
     *
     * @param timeIn Raw datetime pairs.
     * @return A simplified, human-readable time range string.
     */
    public String formatTimeIn(String timeIn) {
        if (timeIn == null || timeIn.isEmpty()) return "No data";

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return Arrays.stream(timeIn.split(","))
                .map(pair -> {
                    String[] times = pair.split("--");
                    if (times.length == 2) {
                        try {
                            LocalDateTime start = LocalDateTime.parse(times[0].trim(), inputFormatter);
                            LocalDateTime end = LocalDateTime.parse(times[1].trim(), inputFormatter);
                            return String.format("%02d:%02d--%02d:%02d",
                                start.getHour(), start.getMinute(),
                                end.getHour(), end.getMinute());
                        } catch (Exception e) {
                            return "Invalid";
                        }
                    }
                    return "Invalid format";
                })
                .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "Format error";
        }
    }

    /**
     * Extracts the first date from the timeIn string and returns it in MM-DD format.
     *
     * @param timeIn Raw datetime string.
     * @return Formatted date string or error message.
     */
    public String extractDay(String timeIn) {
        if (timeIn == null || timeIn.isEmpty()) return "No date";

        try {
            String[] times = timeIn.split("--");
            if (times.length >= 1) {
                String firstTime = times[0].trim();
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(firstTime, inputFormatter);
                return String.format("%02d-%02d", dateTime.getMonthValue(), dateTime.getDayOfMonth());
            }
            return "Invalid";
        } catch (Exception e) {
            return "Date error";
        }
    }

    /**
     * Checks if a single time range exceeds a defined threshold in minutes.
     *
     * @param timeInPair A single time range in "start--end" format.
     * @param thresholdMinutes The duration threshold in minutes.
     * @return true if the duration exceeds the threshold; false otherwise.
     */
    public boolean isLongDuration(String timeInPair, int thresholdMinutes) {
        if (timeInPair == null || timeInPair.isEmpty()) return false;

        try {
            String[] times = timeInPair.split("--");
            if (times.length == 2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime start = LocalDateTime.parse(times[0].trim(), formatter);
                LocalDateTime end = LocalDateTime.parse(times[1].trim(), formatter);
                long durationMinutes = Duration.between(start, end).toMinutes();
                return durationMinutes > thresholdMinutes;
            }
        } catch (Exception e) {
            // Ignore malformed input
        }
        return false;
    }
}