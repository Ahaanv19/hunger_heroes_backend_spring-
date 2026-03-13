package com.open.spring.mvc.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@RestController // Defines this class as a RESTful controller
@RequestMapping("/generate") // Base URL path for this controller
@CrossOrigin(origins = "*") // Enables cross-origin requests from any domain
public class Generator {

    private static final Logger logger = LoggerFactory.getLogger(Generator.class);
    
    private static final String GROQ_API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public static void main(String[] args) {
        SpringApplication.run(Generator.class, args);
    }

    @PostMapping("/question")
    public ResponseEntity<String> generateQuestion(@RequestBody UserRequest userRequest) {
        logger.info("Received request to generate question for topic: {}", userRequest.getTopic());
        
        String prompt = createPrompt(userRequest);
        String generatedQuestion = callGroqAPI(prompt);
        
        return ResponseEntity.ok(generatedQuestion);
    }

    private String createPrompt(UserRequest userRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a question about ").append(userRequest.getTopic()).append(". ");
        
        if (userRequest.getRequirements().toLowerCase().contains("mc")) {
            prompt.append("Make it a multiple-choice question with four options (A, B, C, D) and one correct answer.");
        } else {
            prompt.append("Don't explain why you made the question, don't give a title such as here's a question about blah blah. just ask the question. The question should guide students to write a code block or free response based on these requirements: ")
                  .append(userRequest.getRequirements()).append(". ");
        }
        
        prompt.append("Format the question according to these instructions: ").append(userRequest.getRequirements()).append(".");
        return prompt.toString();
    }

    private String callGroqAPI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + GROQ_API_KEY);
        
        String requestBody = String.format("{\"model\": \"llama3-8b-8192\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", prompt);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            } else {
                logger.error("Error calling Groq API: {}", response.getStatusCode());
                return "Error: " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("Exception while calling Groq API: {}", e.getMessage());
            return "Error calling Groq API.";
        }
    }
}

// Class to encapsulate the user's input
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode methods
class UserRequest {
    private String topic; // Topic for the question
    private String requirements; // Question format and other requirements
}
