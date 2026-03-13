package com.open.spring.mvc.teamteach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicsController {

    @Autowired
    private TopicRepository topicRepository;

    @PostMapping("/add")
    public Topic addTopic(@RequestBody Topic topic) {
        return topicRepository.save(topic);
    }

    @GetMapping("/all")
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    @PutMapping("/{topicId}/signup")
    public Topic signUpStudent(@PathVariable Long topicId, @RequestParam String studentName) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Check if the student name is already in the list to avoid duplication
        if (topic.getStudents().isEmpty()) {
            topic.setStudents(studentName); // First student
        } else {
            String[] existingStudents = topic.getStudents().split(", ");
            // Check if the student already signed up
            for (String existingStudent : existingStudents) {
                if (existingStudent.equals(studentName)) {
                    throw new RuntimeException("Student already signed up for this topic");
                }
            }
            // Append student name to the list if not already signed up
            topic.setStudents(topic.getStudents() + ", " + studentName);
        }

        return topicRepository.save(topic); // Save the updated topic
    }
}
