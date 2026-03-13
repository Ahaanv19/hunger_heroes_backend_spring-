package com.open.spring.mvc.profileThymeleaf;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profileThymeleaf")
public class profileThymeleafApi {
    // post mapping for the endpoint
    @PostMapping("/fibonacci")
    public Map<String, Integer> fibonacci(@RequestBody Map<String, Integer> request) {
        int n = request.get("n");
        int result = calculateFibonacci(n);
        Map<String, Integer> response = new HashMap<>();
        response.put("n", n);
        response.put("fibResult", result);
        return response;
    }

    private int calculateFibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return calculateFibonacci(n - 1) + calculateFibonacci(n - 2);
    }
}