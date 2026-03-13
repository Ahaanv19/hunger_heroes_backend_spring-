package com.open.spring.mvc.bathroom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BathroomViewController {

    @Autowired
    private BathroomQueueJPARepository queueRepository;

    @GetMapping("/admin/bathroomView")
    public String bathroomQueue(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        List<BathroomQueue> queues = queueRepository.findAll();
        model.addAttribute("queues", queues);

        return "bathroomView";
    }
}
