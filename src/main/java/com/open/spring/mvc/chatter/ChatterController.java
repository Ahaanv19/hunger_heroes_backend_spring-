package com.open.spring.mvc.chatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@RestController
@RequestMapping("/grader")
public class ChatterController {

    private static final String MODEL_URL = "https://ggaib.stu.opencodingsociety.com/api/grader";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatterController() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        // Set timeouts in milliseconds
        requestFactory.setConnectTimeout(5000);
        // For newer Spring versions, use setConnectionRequestTimeout instead of setReadTimeout
        requestFactory.setConnectionRequestTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @PostMapping("/input")
    public String getInput(@RequestBody RequestBodyData requestBodyData) {
        System.out.println("Received message: " + requestBodyData.getPrompt());
        if (requestBodyData.getCodeBlock() == null || requestBodyData.getCodeBlock().isEmpty()) {
            return "Error: code_block is required.";
        }
        try {
            String response = sendMessage(requestBodyData);
            String cleanedResponse = cleanResponse(response);
            return cleanedResponse;
        } catch (RestClientException e) {
            System.out.println("An error occurred while generating text: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while generating text: " + e.getMessage();
        }
    }

    private String sendMessage(RequestBodyData requestBodyData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        RequestPayload payload = new RequestPayload();
        payload.setPrompt(requestBodyData.getPrompt());
        payload.setCodeBlock(requestBodyData.getCodeBlock());

        try {
            String jsonInputString = objectMapper.writeValueAsString(payload);
            System.out.println("Sending JSON: " + jsonInputString);
            HttpEntity<String> entity = new HttpEntity<>(jsonInputString, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(MODEL_URL, entity, String.class);
            System.out.println("HTTP Response Code: " + response.getStatusCode().value());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Unexpected response code: " + response.getStatusCode().value());
            }

            System.out.println("Raw API response: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            throw new RestClientException("Error sending message: " + e.getMessage(), e);
        }
    }

    private String cleanResponse(String response) {
        System.out.println("Raw response before cleaning: " + response);
        String cleanedResponse = response.replace("[", "")
                                         .replace("]", "")
                                         .replace("*", "")
                                         .replace("#", "")
                                         .replace("(", "")
                                         .replace(")", "")
                                         .trim();
        System.out.println("Cleaned response: " + cleanedResponse);
        return cleanedResponse;
    }

    public static class RequestBodyData {
        private String prompt;

        @JsonProperty("code_block")
        private String codeBlock;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getCodeBlock() {
            return codeBlock;
        }

        public void setCodeBlock(String codeBlock) {
            this.codeBlock = codeBlock;
        }
    }

    public static class RequestPayload {
        private String prompt;

        @JsonProperty("code_block")
        private String codeBlock;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getCodeBlock() {
            return codeBlock;
        }

        public void setCodeBlock(String codeBlock) {
            this.codeBlock = codeBlock;
        }
    }
}