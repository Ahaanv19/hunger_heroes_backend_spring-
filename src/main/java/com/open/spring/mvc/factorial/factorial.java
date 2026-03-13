package com.open.spring.mvc.factorial;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class factorial {

    @GetMapping("/factorial-calculate")
    public String showFactorialPage(@RequestParam(value = "num", required = false, defaultValue = "5") int num, Model model) {
        if (num < 0) {
            num = 0; // Enforce non-negative numbers
        }

        int nextNum = num > 1 ? num - 1 : 0;
        String factorialStep = num > 1 ? num + " x " + nextNum + "!" : "1"; 
        String fullCalculation = calculateFullFactorial(num);

        model.addAttribute("num", num);
        model.addAttribute("nextNum", nextNum);
        model.addAttribute("factorialStep", factorialStep);
        model.addAttribute("fullCalculation", fullCalculation);

        return "factorial";
    }

    private String calculateFullFactorial(int num) {
        if (num == 0 || num == 1) {
            return "1";
        }
        
        StringBuilder result = new StringBuilder();
        int factorial = 1;

        for (int i = num; i > 0; i--) {
            factorial *= i;
            result.append(i);
            if (i > 1) {
                result.append(" × ");
            }
        }

        result.append(" = ").append(factorial);
        return result.toString();
    }
}