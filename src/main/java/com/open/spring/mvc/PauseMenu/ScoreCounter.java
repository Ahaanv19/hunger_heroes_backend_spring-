package com.open.spring.mvc.PauseMenu;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ScoreCounter Entity - Stores game scores
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "score_counter")
public class ScoreCounter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String user;

    @Column(nullable = false)
    private int score;

    @Column(nullable = true)
    private String gameName;

    @Column(nullable = true)
    private String variableName;
}
