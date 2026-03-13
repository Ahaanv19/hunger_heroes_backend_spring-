package com.open.spring.mvc.quiz;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "quiz_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizScore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    
    // Constructor without id and createdAt for initialization
    public QuizScore(String username, int score) {
        this.username = username;
        this.score = score;
        this.createdAt = Instant.now();
    }
    
    // Static initialization method for sample data
    public static QuizScore[] init() {
        return new QuizScore[] {
            new QuizScore("john_doe", 95),
            new QuizScore("jane_smith", 88),
            new QuizScore("bob_wilson", 92),
            new QuizScore("alice_jones", 78),
            new QuizScore("john_doe", 85)
        };
    }
}