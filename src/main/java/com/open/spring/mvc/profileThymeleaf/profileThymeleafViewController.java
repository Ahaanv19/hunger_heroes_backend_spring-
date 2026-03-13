package com.open.spring.mvc.profileThymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller  // HTTP requests are handled as a controller, using the @Controller annotation
public class profileThymeleafViewController {
    @GetMapping("/profileThymeleaf")
    // @RequestParam handles variables binding to frontend, defaults, etc
    public String profileThymelef(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {

        // model attributes are visible to Thymeleaf when HTML is "pre-processed"
        model.addAttribute("name", name);

        // load HTML VIEW (profileThymeleaf.html)
        return "profileThymeleaf"; 
    }
}
