package com.open.spring.mvc.stats;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsRepository statsRepository;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=AIzaSyAwUorzifmPEIX6M74Kd_as-C-7Ih6UyLs";
    private static final double MIN_GRADE = 0.55;
    private static final double MAX_GRADE = 0.9;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /api/stats
     * Get stats for a specific user or all users.
     * 
     * Request Body Examples:
     * To get a specific user: {"username": "toby"}
     * To get all users: {} or {"username": null}
     */
    @GetMapping
    public ResponseEntity<?> getStats(@RequestBody(required = false) StatsGetDto getRequest) {
        // If no body or username is null/empty, return all stats
        if (getRequest == null || getRequest.getUsername() == null || getRequest.getUsername().isEmpty()) {
            List<Stats> statsList = statsRepository.findAll();
            return new ResponseEntity<>(statsList, HttpStatus.OK);
        }
        
        // Otherwise, get stats for the specific username
        List<Stats> statsList = statsRepository.findAllByUsername(getRequest.getUsername());
        if (statsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(statsList, HttpStatus.OK);
    }
    /**
     * POST /api/stats
     * Create a new Stats record.
     * 
     * Request Body Example:
     * {
     * "username": "nolan",
     * "module": "ai",
     * "submodule": 2,
     * }
     */
    @PostMapping
    public ResponseEntity<Stats> createStats(@RequestBody Stats stats) {
        Optional<Stats> existingStats = statsRepository.findByUsernameAndModuleAndSubmodule(
                stats.getUsername(), stats.getModule(), stats.getSubmodule());
        if (existingStats.isPresent()) {
            Stats statsToUpdate = existingStats.get();
            statsToUpdate.setFinished(stats.getFinished());
            statsToUpdate.setTime(stats.getTime());
            Stats updatedStats = statsRepository.save(statsToUpdate);
            return new ResponseEntity<>(updatedStats, HttpStatus.OK);
        }
        Stats newStats = statsRepository.save(stats);
        return new ResponseEntity<>(newStats, HttpStatus.CREATED);
    }

    /**
     * PUT /api/stats
     * Update stats for a specific column.
     * 
     * Request Body Example:
     * {
     *   "username": "toby",
     *   "module": "ai",
     *   "submodule": 2,
     *   "finished": true,
     *   "time": 321.4
     * }
     */
    @PutMapping
    public ResponseEntity<Stats> updateStats(@RequestBody StatsUpdateDto updateRequest) {

        // 1. Find the user from the DTO
        if (updateRequest.getUsername() == null || updateRequest.getModule() == null || updateRequest.getSubmodule() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Stats> optionalStats = statsRepository.findByUsernameAndModuleAndSubmodule(
                updateRequest.getUsername(), updateRequest.getModule(), updateRequest.getSubmodule());
        if (!optionalStats.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }

        Stats statsToUpdate = optionalStats.get();

        // 2. Update fields if provided
        boolean updated = false;
        if (updateRequest.getFinished() != null) {
            statsToUpdate.setFinished(updateRequest.getFinished());
            updated = true;
        }
        if (updateRequest.getTime() != null) {
            statsToUpdate.setTime(updateRequest.getTime());
            updated = true;
        }
        if (updateRequest.getGrades() != null) {
            statsToUpdate.setGrades(updateRequest.getGrades());
            updated = true;
        }

        if (!updated) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 3. Save the updated object
        Stats updatedStats = statsRepository.save(statsToUpdate);
        return new ResponseEntity<>(updatedStats, HttpStatus.OK);
    }
    @PostMapping("/grade")
    public ResponseEntity<Stats> submitGrade(@RequestBody StatsGradeDto gradeRequest) {
        if (gradeRequest.getUsername() == null || gradeRequest.getUsername().isEmpty()
                || gradeRequest.getModule() == null || gradeRequest.getModule().isEmpty()
                || gradeRequest.getSubmodule() == null
                || gradeRequest.getQuestion() == null || gradeRequest.getQuestion().isEmpty()
                || gradeRequest.getResponse() == null || gradeRequest.getResponse().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double gradeScore;
        try {
            gradeScore = requestGradeFromGemini(gradeRequest.getQuestion(), gradeRequest.getResponse());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<Stats> optionalStats = statsRepository.findByUsernameAndModuleAndSubmodule(
                gradeRequest.getUsername(), gradeRequest.getModule(), gradeRequest.getSubmodule());

        if (optionalStats.isPresent()) {
            Stats existingStats = optionalStats.get();
            existingStats.setGrades(gradeScore);
            Stats savedStats = statsRepository.save(existingStats);
            return new ResponseEntity<>(savedStats, HttpStatus.OK);
        }

        Stats newStats = new Stats();
        newStats.setUsername(gradeRequest.getUsername());
        newStats.setModule(gradeRequest.getModule());
        newStats.setSubmodule(gradeRequest.getSubmodule());
        newStats.setGrades(gradeScore);
        Stats savedStats = statsRepository.save(newStats);
        return new ResponseEntity<>(savedStats, HttpStatus.CREATED);
    }

    /**
     * DELETE /api/stats
     * Delete a user's stats.
     * 
     * Request Body Example:
     * {
     * "username": "toby"
     * }
     */
    @DeleteMapping
    public ResponseEntity<String> deleteStats(@RequestBody StatsDeleteDto deleteRequest) {

        if (deleteRequest.getUsername() == null || deleteRequest.getUsername().isEmpty()) {
            return new ResponseEntity<>("Username is required.", HttpStatus.BAD_REQUEST);
        }

        // Delete all user stats if module not provided
        if (deleteRequest.getModule() == null || deleteRequest.getModule().isEmpty()) {
            List<Stats> statsList = statsRepository.findAllByUsername(deleteRequest.getUsername());
            if (statsList.isEmpty()) {
                return new ResponseEntity<>("User '" + deleteRequest.getUsername() + "' not found.", HttpStatus.NOT_FOUND);
            }
            statsRepository.deleteAll(statsList);
            return new ResponseEntity<>("All stats for '" + deleteRequest.getUsername() + "' deleted successfully.", HttpStatus.OK);
        }

        // Delete all submodules for module if submodule not provided
        if (deleteRequest.getSubmodule() == null) {
            List<Stats> statsList = statsRepository.findAllByUsernameAndModule(
                    deleteRequest.getUsername(), deleteRequest.getModule());
            if (statsList.isEmpty()) {
                return new ResponseEntity<>(
                        "No stats for '" + deleteRequest.getUsername() + "' in module '" + deleteRequest.getModule() + "'.",
                        HttpStatus.NOT_FOUND);
            }
            statsRepository.deleteAll(statsList);
            return new ResponseEntity<>(
                    "Stats for '" + deleteRequest.getUsername() + "' in module '" + deleteRequest.getModule() + "' deleted successfully.",
                    HttpStatus.OK);
        }

        // Delete specific module/submodule entry
        Optional<Stats> optionalStats = statsRepository.findByUsernameAndModuleAndSubmodule(
                deleteRequest.getUsername(), deleteRequest.getModule(), deleteRequest.getSubmodule());
        if (!optionalStats.isPresent()) {
            // Return Not Found if the user doesn't exist
            return new ResponseEntity<>(
                    "No stats for '" + deleteRequest.getUsername() + "' in module '" + deleteRequest.getModule() +
                            "' submodule '" + deleteRequest.getSubmodule() + "'.",
                    HttpStatus.NOT_FOUND);
        }

        // 2. Get the stats object and delete it
        Stats statsToDelete = optionalStats.get();
        statsRepository.delete(statsToDelete);

        // 3. Return a success message
        return new ResponseEntity<>(
                "Stats for '" + deleteRequest.getUsername() + "' in module '" + deleteRequest.getModule() +
                        "' submodule '" + deleteRequest.getSubmodule() + "' deleted successfully.",
                HttpStatus.OK);
    }

    private Double requestGradeFromGemini(String question, String response) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = "Grade the student's response to the question on a numeric scale between 0.55 and 0.9 inclusive. If there are multiple questions given, they will given be numbered in an ordered list. The responses will also be given in an ordered list. You will need to grade each response individually then give provide the average grade in the numeric grading scale from 0.55 to 0.9, inclusive." +
                "No explanation. Return ONLY the numeric grade. \nQuestion: " + question + "\nResponse: " + response;

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        String payload = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> geminiResponse = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

        if (!geminiResponse.getStatusCode().is2xxSuccessful() || geminiResponse.getBody() == null) {
            throw new IllegalStateException("Gemini API call failed");
        }

        JsonNode root = objectMapper.readTree(geminiResponse.getBody());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isEmpty()) {
            throw new IllegalStateException("Gemini API returned no grade text");
        }

        double parsedScore = parseScore(textNode.asText());
        parsedScore = Math.max(MIN_GRADE, Math.min(MAX_GRADE, parsedScore));
        return parsedScore;
    }

    private double parseScore(String text) {
        Matcher matcher = Pattern.compile("(\\d+\\.?\\d*)").matcher(text);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalStateException("Unable to parse numeric grade from response: " + text);
    }
}
