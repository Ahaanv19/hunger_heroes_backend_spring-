package com.open.spring.mvc.resume;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
public class ResumeApiController {
    @Autowired
    private ResumeJpaRepository resumeRepo;

    // GET Resume for current user
    @GetMapping("/me")
    public ResponseEntity<Resume> getResume(
            Principal principal,
            @RequestParam(value = "username", required = false) String paramUsername) {
        String username = (principal != null) ? principal.getName() : paramUsername;
        if (username == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Optional<Resume> resume = resumeRepo.findByUsername(username);
        return resume.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST Resume for current user (create or update)
    @PostMapping("/me")
    public ResponseEntity<Resume> saveResume(
            Principal principal,
            @RequestParam(value = "username", required = false) String paramUsername,
            @RequestBody Resume resume) {
        String username = (principal != null) ? principal.getName() : paramUsername;
        if (username == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        resume.setUsername(username); // Override username for authenticated user
        Optional<Resume> previous = resumeRepo.findByUsername(username);
        previous.ifPresent(r -> resume.setId(r.getId())); // update instead of create if exists
        Resume saved = resumeRepo.save(resume);
        return ResponseEntity.ok(saved);
    }
}
