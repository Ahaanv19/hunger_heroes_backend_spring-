package com.open.spring.mvc.rpg.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/game")
public class GameWebController {

    @Autowired
    private UnifiedGameRepository repo;

    @GetMapping("/delete/{id}")
    public String deleteById(@PathVariable("id") Long id) {
        try { repo.deleteById(id); } catch (Exception ignored) {}
        return "redirect:/mvc/games/read";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Game> o = repo.findById(id);
        model.addAttribute("g", o.orElse(new Game()));
        return "games/edit";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("g", new Game());
        return "games/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Game g) {
        repo.save(g);
        return "redirect:/mvc/games/read";
    }
}
