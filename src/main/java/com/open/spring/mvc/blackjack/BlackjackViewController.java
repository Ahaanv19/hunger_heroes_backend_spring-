package com.open.spring.mvc.blackjack;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlackjackViewController {
    @GetMapping("/blackjack")
    public String blackjack() {
        return "blackjack";
    }
}