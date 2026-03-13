package com.open.spring.mvc.chat;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.open.spring.mvc.S3uploads.S3FileHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private static final String CHAT_FILENAME = "chat_history.jsonl";

    private final S3FileHandler s3FileHandler;
    private final ObjectMapper objectMapper;

    public List<ChatMessage> getChatHistory(Long groupId) {
        return readHistory(groupId.toString());
    }

    public List<ChatMessage> addMessage(Long groupId, ChatMessage message) {
        List<ChatMessage> history = readHistory(groupId.toString());
        history.add(message);
        writeHistory(groupId.toString(), history);
        return history;
    }

    private List<ChatMessage> readHistory(String groupId) {
        String base64Data = s3FileHandler.decodeFile(groupId, CHAT_FILENAME);
        if (base64Data == null || base64Data.isBlank()) {
            return new ArrayList<>();
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid base64 data for group {} chat history.", groupId, e);
            return new ArrayList<>();
        }

        String jsonl = new String(decoded, StandardCharsets.UTF_8);
        if (jsonl.isBlank()) {
            return new ArrayList<>();
        }

        List<ChatMessage> messages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(jsonl))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    ChatMessage message = objectMapper.readValue(line, ChatMessage.class);
                    messages.add(message);
                } catch (Exception e) {
                    log.warn("Skipping invalid chat line for group {}: {}", groupId, line, e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed reading chat history for group {}", groupId, e);
        }

        return messages;
    }

    private void writeHistory(String groupId, List<ChatMessage> history) {
        String jsonl = history.stream()
                .map(this::toJson)
                .collect(Collectors.joining("\n"));

        String base64Data = Base64.getEncoder()
                .encodeToString(jsonl.getBytes(StandardCharsets.UTF_8));

        s3FileHandler.uploadFile(base64Data, CHAT_FILENAME, groupId);
    }

    private String toJson(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.warn("Failed to serialize chat message: {}", message, e);
            return "{}";
        }
    }
}
