package com.open.spring.mvc.palindrome;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PalindromeController {

    @GetMapping("/palindrome")
    public String checkPalindrome(@RequestParam(value = "input", required = false) String input, Model model) {
        if (input != null) {
            String cleanedInput = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            List<Check> checks = getCharacterComparisons(cleanedInput);
            boolean isPalindrome = isCustomPalindrome(cleanedInput);
            
            model.addAttribute("input", input);
            model.addAttribute("checks", checks);  // Add checks to model
            model.addAttribute("isPalindrome", isPalindrome);
        }
        return "palindrome";
    }

    private List<Check> getCharacterComparisons(String str) {
        List<Check> checks = new ArrayList<>();
        int length = str.length();
        int left = 0;
        int right = length - 1;

        while (left < right) {
            checks.add(new Check(str.charAt(left), str.charAt(right)));
            left++;
            right--;
        }

        return checks;
    }

    private boolean isCustomPalindrome(String str) {
        int length = str.length();
        if (length == 0) return false;

        int left = 0;
        int right = length - 1;

        if (length % 2 != 0) {
            left++; // Ignore middle character for odd length strings
        }

        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }

        return true;
    }

    // Inner class to hold character comparisons
    public static class Check {
        private char left;
        private char right;

        public Check(char left, char right) {
            this.left = left;
            this.right = right;
        }

        public char getLeft() {
            return left;
        }

        public char getRight() {
            return right;
        }
    }
}
