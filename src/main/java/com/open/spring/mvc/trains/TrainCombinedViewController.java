package com.open.spring.mvc.trains;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/mvc/train")
public class TrainCombinedViewController {

    @GetMapping("/home")
    public String getTrainHomePage(){
        return "train/home";
    }

    @GetMapping("/store")
    public String getTrainStorePage(){
        return "train/store";
    }
}
