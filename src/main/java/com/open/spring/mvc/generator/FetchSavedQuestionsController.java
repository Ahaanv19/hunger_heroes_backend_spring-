package com.open.spring.mvc.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/saved-questions")
public class FetchSavedQuestionsController {

    @Autowired
    private GeneratedQuestionRepository questionRepository;

    // Modified: Return full GeneratedQuestion objects instead of just question strings
    @GetMapping
    public List<GeneratedQuestion> getSavedQuestions() {
        return questionRepository.findAll();
    }

    // New: Fetch questions filtered by a specific tag
    @GetMapping("/by-tag")
    public List<GeneratedQuestion> getQuestionsByTag(@RequestParam String tag) {
        return questionRepository.findByTagsContaining(tag);
    }
}
