package com.open.spring.mvc.mortevision.nativesocket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mortevision")
public class IsStreamingMapping {
    // now you might say this is unnecessary and you can just poll the websocket
    // and you're right 

    @GetMapping("/isStreamActive")
    public String isStreamActive() {
        return String.valueOf(WebSocketConfig.handler.broadcasterID != null);
    }
}
