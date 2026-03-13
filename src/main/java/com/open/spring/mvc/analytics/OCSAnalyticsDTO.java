package com.open.spring.mvc.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for receiving analytics data from frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OCSAnalyticsDTO {
    
    private Long personId;
    private String uid;  // Frontend sends UID to identify user
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private Long sessionDurationSeconds;
    
    // Content info
    private String questName;
    private String moduleName;
    private String lessonNumber;
    private String pageTitle;
    private String pageUrl;
    
    // User actions
    private Integer lessonsViewed;
    private Integer lessonsCompleted;
    private Integer modulesViewed;
    private Integer videosWatched;
    private Integer videosCompleted;
    private Integer codeExecutions;
    private Integer copyPasteAttempts;
    private Integer questionsAnswered;
    private Integer questionsCorrect;
    private Integer exercisesAttempted;
    private Integer exercisesCompleted;
    private Integer assessmentsAttempted;
    private Double assessmentAverageScore;
    
    // Progression
    private Boolean moduleCompleted;
    private Integer progressPercentage;
    
    // Engagement
    private Integer scrollDepthPercentage;
    private Double interactionPercentage;
    private Integer hoverEventsCount;
    private Integer keyboardInputEvents;
    private Integer mouseClicksCount;
    
    // Performance
    private Double pageLoadTimeMs;
    private Integer timeoutErrors;
    private Integer validationErrors;
    
    // Metadata
    private String userAgent;
    private String referrer;
}
