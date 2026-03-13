package com.open.spring.mvc.bathroom;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/tinkle")
public class TinkleApiController {

    // Inject Tinkle JPA repository for database operations
    @Autowired
    private TinkleJPARepository repository;

    // Inject Person JPA repository to fetch person-related data
    @Autowired
    private PersonJpaRepository personRepository;

    @Autowired
    private BathroomQueueJPARepository bathroomQueue;

    @Autowired
    private EntityManager entityManager;

    @Getter
    @Setter
    public static class TinkleDto {
        // private String studentEmail;  // Used to identify the person
        private String sid;
        private String timeIn;        // String of time-in/time-out pairs
    }

    /**
     * Add or update time-in data for a student by email.
     */
    @PostMapping("/add")
    public ResponseEntity<Object> timeInOut(@RequestBody TinkleDto tinkleDto) {
        Optional<Tinkle> student = repository.findBySid(tinkleDto.getSid());

        if (student.isPresent()) {
            student.get().addTimeIn(tinkleDto.getTimeIn());
            repository.save(student.get());
            return new ResponseEntity<>(student.get(), HttpStatus.OK);
        } else {
            // List<BathroomQueue> queues = bathroomQueue.findAll();
            // for (BathroomQueue queue : queues) {
            //     if (queue.getPeopleQueue().contains(tinkleDto.getStudentEmail())) {
            //         queue.removeStudent(tinkleDto.getStudentEmail());
            //         bathroomQueue.save(queue);
            //         break;
            //     }
            // }
            return new ResponseEntity<>("Student not found in Tinkle. Queue entry removed.", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Return a list of all Tinkle entries in the database.
     */
    @GetMapping("/all")
    public List<Tinkle> getAll() {
        return repository.findAll();
    }

    /**
     * Fetch a specific student's bathroom stats by name.
     */
    @GetMapping("/{name}")
    public ResponseEntity<Object> getTinkle(@PathVariable String name) {
        Optional<Tinkle> tinkle = repository.findByPersonName(name);

        if (tinkle.isPresent()) {
            return new ResponseEntity<>(tinkle.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Populate initial Tinkle records for all people if they don't already exist.
     */
    @GetMapping("/repopulate")
    public ResponseEntity<Object> populatePeople() {
        var personArray = personRepository.findAllByOrderByNameAsc();

        for (Person person : personArray) {
            Optional<Tinkle> tinkleFound = repository.findBySid(person.getSid());
            if (tinkleFound.isEmpty()) {
                Tinkle tinkle = new Tinkle(person, "");
                repository.save(tinkle);
            }
        }

        return ResponseEntity.ok("Complete");
    }


    /**
     * Retrieve a student's `timeIn` string directly from in-memory cache.
     * This seems to depend on another controller (ApprovalRequestApiController).
     */
    @GetMapping("/timeIn/{studentName}")
    public ResponseEntity<Object> getTimeIn(@PathVariable String studentName) {
        System.out.println("🔍 Fetching timeIn for: " + studentName);

        String timeIn = ApprovalRequestApiController.getTimeInFromMemory(studentName);

        if (timeIn != null) {
            System.out.println("Retrieved timeIn from memory for " + studentName + ": " + timeIn);
            return ResponseEntity.ok(timeIn);
        } else {
            System.out.println("Student not found in memory: " + studentName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }
    }

    /**
     * Clears all bathroom records from the database.
     * Requires the requester to be an admin (checked via request attribute).
     */

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/bulk/clear")
    public ResponseEntity<?> clearTable(HttpServletRequest request) {
        try {
            repository.deleteAllInBatch();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All bathroom records have been cleared"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }



    @GetMapping("/bulk/extract")
    public ResponseEntity<List<TinkleDto>> bulkExtract() {
        List<Tinkle> tinkleList = repository.findAll();

        List<TinkleDto> tinkleDtos = new ArrayList<>();
        for (Tinkle tinkle : tinkleList) {
            TinkleDto dto = new TinkleDto();
            dto.setSid(tinkle.getSid());
            dto.setTimeIn(tinkle.getTimeIn());
            tinkleDtos.add(dto);
        }

        return new ResponseEntity<>(tinkleDtos, HttpStatus.OK);
    }

    @PostMapping("/bulk/create")
    public ResponseEntity<Object> bulkCreateTinkles(@RequestBody List<TinkleDto> tinkleDtos) {
        List<String> createdTinkles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (TinkleDto tinkleDto : tinkleDtos) {
            String sid = tinkleDto.getSid();

            if (sid == null || sid.isEmpty()) {
                errors.add("Missing sid for entry");
                continue;
            }

            try {
                Optional<Tinkle> existingTinkle = repository.findBySid(sid);

                if (existingTinkle.isPresent()) {
                    Tinkle tinkle = existingTinkle.get();
                    tinkle.addTimeIn(tinkleDto.getTimeIn());
                    repository.save(tinkle);
                    createdTinkles.add(sid + " (updated)");
                } else {
                    Person person = personRepository.findBySid(sid);
                    Optional<Person> personOpt = Optional.ofNullable(person);

                    if (personOpt.isPresent()) {
                        Tinkle newTinkle = new Tinkle(personOpt.get(), tinkleDto.getTimeIn());
                        repository.save(newTinkle);
                        createdTinkles.add(sid);
                    } else {
                        errors.add("No person found for sid: " + sid);
                    }
                }
            } catch (Exception e) {
                errors.add("Exception for sid " + sid + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("created", createdTinkles);
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
