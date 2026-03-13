package com.open.spring.mvc.geminiUserPreferences;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserPrefernecesAI {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String gradingResult;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    public UserPrefernecesAI(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.createdAt = System.currentTimeMillis();
    }
}
