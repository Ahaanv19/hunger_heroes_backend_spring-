package com.open.spring.mvc.rpg.adventure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/adventure")
public class AdventureWebController {

    @Autowired
    private AdventureJpaRepository repo;

    @GetMapping("/delete/{id}")
    public String deleteById(@PathVariable("id") Long id) {
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            // ignore if not found
        }
        return "redirect:/mvc/adventure/read";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Optional<Adventure> o = repo.findById(id);
        model.addAttribute("adv", o.orElse(new Adventure()));
        return "adventure/edit";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("adv", new Adventure());
        return "adventure/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Adventure adv) {
        repo.save(adv);
        return "redirect:/mvc/adventure/read";
    }
}
