package com.open.spring.mvc.geminiChatbotTest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiChatTest {
    private Long id;
    private String userId;
    private String userMessage;
    private String geminiResponse;
    private Long createdAt;

    public GeminiChatTest(String userId, String userMessage) {
        this.userId = userId;
        this.userMessage = userMessage;
        this.createdAt = System.currentTimeMillis();
    }
}
