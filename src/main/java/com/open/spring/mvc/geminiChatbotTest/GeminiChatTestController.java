package com.open.spring.mvc.geminiChatbotTest;

import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
public class GeminiChatTestController {

    private final Dotenv dotenv = Dotenv.load();
    private final String geminiApiKey = dotenv.get("GEMINI_API_KEY");
    private final String geminiApiUrl = dotenv.get("GEMINI_API_URL");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String userId;
        private String message;
    }

    // Test API from web browser
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from GET endpoint of GeminiChatBot!");
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody(required = false) ChatRequest request) {
        try {
            String userId = request.getUserId();
            String message = request.getMessage();

            if (userId == null || message == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing userId or message"));
            }

            System.out.println("/chat endpoint hit by userId: " + userId);

            String prompt = String.format("Respond conversationally to the user message: \"%s\"", message);

            String jsonPayload = String.format("""
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"
                        }]
                    }]
                }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpRequest = new HttpEntity<>(jsonPayload, headers);
            String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.POST, httpRequest, String.class);

            String geminiResponse = extractResponseText(response.getBody());

            // created an object incase we want to save chat history later
            GeminiChatTest chat = new GeminiChatTest(userId, message);
            chat.setGeminiResponse(geminiResponse);

            return ResponseEntity.ok(Map.of("response", geminiResponse));

        } catch (HttpClientErrorException.TooManyRequests e) {
            return ResponseEntity.status(429).body(Map.of("error", "Gemini quota exceeded. Please try again later."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }


    private String extractResponseText(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode textNode = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            if (textNode.isTextual()) {
                return textNode.asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error parsing Gemini response";
    }
}
