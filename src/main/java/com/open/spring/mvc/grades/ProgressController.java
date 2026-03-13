package com.open.spring.mvc.grades;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;

    @GetMapping
    public List<Progress> getAllProgress() {
        return progressRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Progress> getProgressById(@PathVariable Long id) {
        Optional<Progress> progress = progressRepository.findById(id);
        return progress.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public List<Progress> getProgressByStudent(@PathVariable String studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @GetMapping("/subject/{subject}")
    public List<Progress> getProgressBySubject(@PathVariable String subject) {
        return progressRepository.findBySubject(subject);
    }

    @GetMapping("/status/{status}")
    public List<Progress> getProgressByStatus(@PathVariable String status) {
        return progressRepository.findByStatus(status);
    }

    @PostMapping
    public Progress createProgress(@RequestBody Progress progress) {
        progress.setLastUpdated(LocalDateTime.now());
        return progressRepository.save(progress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Progress> updateProgress(@PathVariable Long id, @RequestBody Progress progressDetails) {
        Optional<Progress> optionalProgress = progressRepository.findById(id);
        if (optionalProgress.isPresent()) {
            Progress progress = optionalProgress.get();
            progress.setStudentId(progressDetails.getStudentId());
            progress.setSubject(progressDetails.getSubject());
            progress.setCompletionPercentage(progressDetails.getCompletionPercentage());
            progress.setStatus(progressDetails.getStatus());
            progress.setLastUpdated(LocalDateTime.now());
            return ResponseEntity.ok(progressRepository.save(progress));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long id) {
        if (progressRepository.existsById(id)) {
            progressRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}


