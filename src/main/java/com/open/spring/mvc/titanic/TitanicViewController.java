package com.open.spring.mvc.titanic;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TitanicViewController {

    @GetMapping("/titanic")
    public String titanicDashboard(Model model) {
        // Add metadata for the dashboard
        model.addAttribute("title", "Titanic Analysis Dashboard");
        model.addAttribute("description", "Interactive data analysis of the Titanic dataset");
        
        // Define the charts available in the dashboard
        model.addAttribute("charts", new String[][]{
            {"Gender Analysis", "/titanic/gender", "Comparison of survival rates by gender"},
            {"Age Distribution", "/titanic/age", "Analysis of passenger ages and survival rates"},
            {"Fare Analysis", "/titanic/fare", "Ticket prices and their impact on survival"},
            {"Traveling Status", "/titanic/alone", "Survival rates for solo travelers vs families"}
        });
        
        return "titanic_plots/dashboard";
    }
    
    @GetMapping("/titanic/gender")
    public String genderAnalysis() {
        return "titanic_plots/gender_counts";
    }
    
    @GetMapping("/titanic/age")
    public String ageAnalysis() {
        return "titanic_plots/age_histogram";
    }
    
    @GetMapping("/titanic/fare")
    public String fareAnalysis() {
        return "titanic_plots/fare_histogram";
    }
    
    @GetMapping("/titanic/alone")
    public String aloneAnalysis() {
        return "titanic_plots/alone_counts";
    }
}