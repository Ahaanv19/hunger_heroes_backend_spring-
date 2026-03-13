package com.open.spring.mvc.rpg.adventure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mvc/adventure")
public class AdventureMvcController {

    @Autowired
    private AdventureJpaRepository adventureJpaRepository;

    @GetMapping("/read")
    public String readView(Model model) {
        model.addAttribute("list", adventureJpaRepository.findAll());
        return "adventure/read";
    }
}
