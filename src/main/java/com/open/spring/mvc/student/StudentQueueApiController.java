package com.open.spring.mvc.student;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

@RestController
@RequestMapping("/api/student_queue")
public class StudentQueueApiController {

    @Autowired
    private StudentQueueJPARepository repository;

    // DTO class for queue entries
    @Getter
    public static class QueueDto {
        private String teacherEmail;
        private String studentName;
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addToQueue(@RequestBody QueueDto queueDto) {
        // Check if a queue entry for the teacher already exists
        Optional<StudentQueue> existingQueue = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (existingQueue.isPresent()) {
            existingQueue.get().addStudent(queueDto.getStudentName());
            repository.save(existingQueue.get());
        }
        else {
            StudentQueue newQueue = new StudentQueue(queueDto.getTeacherEmail(), queueDto.getStudentName());
            repository.save(newQueue);
        }
        return new ResponseEntity<>(queueDto.getStudentName() + " was added to " + queueDto.getTeacherEmail(), HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "http://127.0.0.1:4500")
    @DeleteMapping("/remove")
    public ResponseEntity<Object> removeFromQueue(@RequestBody QueueDto queueDto) {
        Optional<StudentQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            StudentQueue bathroomQueue = queueEntry.get();
            try {
                bathroomQueue.removeStudent(queueDto.getStudentName());
                repository.save(bathroomQueue);
                return new ResponseEntity<>("Removed " + queueDto.getStudentName() + " from " + queueDto.getTeacherEmail() + "'s queue", HttpStatus.OK);
            } 
            catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://127.0.0.1:4500")
    @PostMapping("/approve")
    public ResponseEntity<Object> approveStudent(@RequestBody QueueDto queueDto) {
        Optional<StudentQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            StudentQueue studentQueue = queueEntry.get();
            String frontStudent = studentQueue.getFrontStudent();
            if (frontStudent != null && frontStudent.equals(queueDto.getStudentName())) {
                repository.save(studentQueue);
                return new ResponseEntity<>("Approved " + queueDto.getStudentName(), HttpStatus.OK);
            } 
            else {
                return new ResponseEntity<>("Student is not at the front of the queue", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/all")
    public ResponseEntity<List<StudentQueue>> getAllQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
   
    @CrossOrigin(origins = "http://localhost:8585")
    @GetMapping("/getActive")
    public ResponseEntity<Object> getActiveQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
}