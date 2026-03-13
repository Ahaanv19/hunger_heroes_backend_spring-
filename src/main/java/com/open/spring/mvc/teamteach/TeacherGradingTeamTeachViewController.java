package com.open.spring.mvc.teamteach;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;




@Controller
@RequestMapping("/mvc/teamteach")
public class TeacherGradingTeamTeachViewController {


    @GetMapping("/teachergrading")
    public String assignmentTracker(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        return "teamteach/teacher-grading-teamteach";
    }


}