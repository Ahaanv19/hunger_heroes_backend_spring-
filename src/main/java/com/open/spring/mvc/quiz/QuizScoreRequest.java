package com.open.spring.mvc.quiz;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class QuizScoreRequest {
    @NotBlank
    private String username;

    @Min(0)
    private int score;
}
