package com.open.spring.mvc.generator;

import org.springframework.beans.factory.annotation.Autowired; // Enables dependency injection
import org.springframework.web.bind.annotation.*; // REST API annotations

import java.util.Set;

@RestController // Defines this class as a RESTful controller
@RequestMapping("/save-question") // Base URL path for this controller
public class SaveQuestionController {

    @Autowired // Automatically injects an instance of the repository
    private GeneratedQuestionRepository questionRepository;

    @PostMapping // Maps POST requests to this method
    public void saveQuestion(@RequestBody GeneratedQuestion question) {
        // Save the received question entity into the database
        questionRepository.save(question);
    }

    @PutMapping("/{id}/tags")
    public GeneratedQuestion updateTags(@PathVariable Long id, @RequestBody Set<String> tags) {
        return questionRepository.findById(id).map(q -> {
            q.setTags(tags);
            return questionRepository.save(q);
        }).orElseThrow(() -> new RuntimeException("Question not found"));
    }
}