package com.open.spring.mvc.donation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for donation-related Thymeleaf views.
 * 
 * <p>Provides styled HTML pages for viewing donations, statistics,
 * and leaderboards instead of raw JSON API responses.</p>
 * 
 * @author Ahaan
 * @version 1.0
 */
@Controller
@RequestMapping("/mvc/donations")
public class DonationViewController {

    /**
     * Displays the donation list page with filtering support.
     * 
     * @return the donation list template
     */
    @GetMapping({"", "/", "/list"})
    public String listDonations() {
        return "donation/list";
    }

    /**
     * Displays the donation statistics page.
     * 
     * @return the statistics template
     */
    @GetMapping("/stats")
    public String showStats() {
        return "donation/stats";
    }

    /**
     * Displays the donor leaderboard page.
     * 
     * @return the leaderboard template
     */
    @GetMapping("/leaderboard")
    public String showLeaderboard() {
        return "donation/leaderboard";
    }

    /**
     * Displays the food categories tree page.
     * 
     * @return the categories template
     */
    @GetMapping("/categories")
    public String showCategories() {
        return "donation/categories";
    }

    /**
     * Displays the donation network graph page.
     * 
     * @return the graph template
     */
    @GetMapping("/graph")
    public String showGraph() {
        return "donation/graph";
    }
}
