package com.open.spring.mvc.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@Validated
public class QuizApiController {

    @Autowired
    private QuizScoreRepository repository;

    // Save a user's quiz score
    @PostMapping("/score")
    public ResponseEntity<QuizScore> saveScore(@Valid @RequestBody QuizScoreRequest request) {
        // Create entity and save
        QuizScore entity = new QuizScore();
        entity.setUsername(request.getUsername());
        entity.setScore(request.getScore());
        QuizScore saved = repository.save(entity);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Return top scores (all by default, or limit via ?limit=10)
    @GetMapping("/top")
    public ResponseEntity<List<QuizScore>> topScores(@RequestParam(name = "limit", required = false) Integer limit) {
        List<QuizScore> all = repository.findAllOrderByScoreDesc();
        List<QuizScore> out = all;
        if (limit != null && limit > 0) {
            out = all.stream().limit(limit).collect(Collectors.toList());
        }
        return new ResponseEntity<>(out, HttpStatus.OK);
    }

    // Optional: get scores for a specific user
    @GetMapping("/user/{username}")
    public ResponseEntity<List<QuizScore>> scoresForUser(@PathVariable String username) {
        return new ResponseEntity<>(repository.findByUsernameIgnoreCaseOrderByScoreDesc(username), HttpStatus.OK);
    }
}
