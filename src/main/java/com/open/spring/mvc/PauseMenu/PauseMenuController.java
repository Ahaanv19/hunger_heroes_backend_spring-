package com.open.spring.mvc.PauseMenu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * MVC Controller for displaying pause menu scores
 */
@Controller
@RequestMapping("/pausemenu")
public class PauseMenuController {

    @Autowired
    private ScorePauseMenuRepo scoreRepository;

    /**
     * Display all scores
     * GET /pausemenu/scores
     */
    @GetMapping("/scores")
    public String showAllScores(Model model) {
        List<ScoreCounter> scores = scoreRepository.findAll();
        model.addAttribute("scores", scores);
        model.addAttribute("pageTitle", "All Scores");
        return "pausemenu/scores-table";
    }

    /**
     * Display scores for a specific user
     * GET /pausemenu/scores?user=Username
     */
    @GetMapping("/scores/user")
    public String showUserScores(String user, Model model) {
        List<ScoreCounter> scores = scoreRepository.findByUser(user);
        model.addAttribute("scores", scores);
        model.addAttribute("pageTitle", "Scores for " + user);
        model.addAttribute("userName", user);
        return "pausemenu/scores-table";
    }
}
