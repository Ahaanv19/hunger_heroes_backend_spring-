package com.open.spring.mvc.slack;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "slack_messages")
public class SlackMessage {
    @Id
    private LocalDateTime timestamp;

    @Lob
    private String messageBlob;

    // Default constructor
    public SlackMessage() {
    }

    // Constructor with timestamp and messageBlob
    public SlackMessage(LocalDateTime timestamp, String messageBlob) {
        this.timestamp = timestamp;
        this.messageBlob = messageBlob;
    }

    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageBlob() {
        return messageBlob;
    }

    public void setMessageBlob(String messageBlob) {
        this.messageBlob = messageBlob;
    }
}
