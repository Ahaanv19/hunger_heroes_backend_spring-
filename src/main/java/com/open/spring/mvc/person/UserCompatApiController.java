package com.open.spring.mvc.person;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonDetailsService;
import com.open.spring.mvc.person.PersonJpaRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flask-compatible user API controller for the Hunger Heroes frontend.
 *
 * Maps Flask endpoints to the Spring Boot Person system:
 *   POST /api/user  → create user (equivalent to POST /api/person/create)
 *   GET  /api/user  → current user info (equivalent to GET /api/person/get)
 */
@RestController
@RequestMapping("/api/user")
public class UserCompatApiController {

    @Autowired
    private PersonJpaRepository personRepo;

    @Autowired
    private PersonDetailsService personDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * POST /api/user — create a new user account.
     * Matches Flask's UserAPI._CRUD.post() behaviour.
     *
     * Request body:
     * {
     *   "name":     "Jane Doe",
     *   "uid":      "janedoe",
     *   "password": "secret123"
     * }
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String uid  = (String) body.get("uid");
        String password = (String) body.get("password");

        if (name == null || name.length() < 2) {
            return badRequest("Name is missing, or is less than 2 characters");
        }
        if (uid == null || uid.length() < 2) {
            return badRequest("User ID is missing, or is less than 2 characters");
        }
        if (password == null || password.isBlank()) {
            return badRequest("Password is missing");
        }

        // Check for duplicates
        if (personRepo.existsByUid(uid)) {
            return badRequest("Processed " + name + ", either a format error or User ID " + uid + " is duplicate");
        }

        String email = body.containsKey("email") ? (String) body.get("email") : uid + "@hunger.heroes";
        if (personRepo.existsByEmail(email)) {
            email = uid + "@hunger.heroes";
        }

        PersonRole defaultRole = personDetailsService.findRole("ROLE_USER");
        if (defaultRole == null) {
            defaultRole = personDetailsService.findRole("USER");
        }
        if (defaultRole == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Default role not configured"));
        }

        Person person = new Person(email, uid, password, null, name, "/images/default.png", true, defaultRole);
        personDetailsService.save(person);

        // Return user info in Flask-compatible format
        return ResponseEntity.ok(personToMap(person));
    }

    /**
     * GET /api/user — return the currently authenticated user's details.
     * Matches Flask's UserAPI._CRUD.get() behaviour.
     */
    @GetMapping
    public ResponseEntity<Object> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }
        Person person = personRepo.findByUid(userDetails.getUsername());
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok(personToMap(person));
    }

    // ── Helpers ──

    private Map<String, Object> personToMap(Person person) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", person.getId());
        m.put("name", person.getName());
        m.put("uid", person.getUid());
        m.put("email", person.getEmail());
        m.put("pfp", person.getPfp());
        m.put("sid", person.getSid());
        return m;
    }

    private ResponseEntity<Object> badRequest(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject body = new JSONObject();
        body.put("message", message);
        return new ResponseEntity<>(body.toString(), headers, HttpStatus.BAD_REQUEST);
    }
}
