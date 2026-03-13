package com.open.spring.mvc.bathroom;


import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Built using article: https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html
// or similar: https://asbnotebook.com/2020/04/11/spring-boot-thymeleaf-form-validation-example/
@Controller
@RequestMapping("/mvc/bathroom")
public class HallPassViewController {
    // Autowired enables Control to connect HTML and POJO Object to database easily for CRUD
    @Autowired
    private TeacherJpaRepository teacherRepository;
    @Autowired
    private HallPassJpaRepository tinkleRepository;

    @GetMapping("/hallpass")
    public String getTinkleTimePage(Model model,
                                    @RequestParam("fname") String teacherFirstName,
                                    @RequestParam("lname") String teacherLastName, Authentication authentication) {
        // Fetch logged-in student's username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //String email = userDetails.getUsername(); 
        // Find the teacher by first and last name
        List<Teacher> teachers = teacherRepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(teacherFirstName, teacherLastName);
        if (teachers.isEmpty()) {
            model.addAttribute("error", "Teacher not found");
            model.addAttribute("username", username);
            return "bathroom/hallpass"; // Redirect to error page if teacher doesn't exist
        }
        Teacher teacher = teachers.get(0);

        // Check if there's an active pass
        HallPass activePass = tinkleRepository.findByPersonIdAndCheckoutIsNull(username).orElse(null);
        if(activePass != null) {
            Teacher activePassTeacher = teacherRepository.findById(activePass.getTeacher_id()).get();
            model.addAttribute("activepassteacher", activePassTeacher);
        }
        model.addAttribute("teacher", teacher);
        model.addAttribute("activePass", activePass);
        model.addAttribute("username", username);

        return "bathroom/hallpass";
    }



    @PostMapping("/requestPass")
    public String requestPass(@RequestParam Long teacherId,
                            @RequestParam int period, @RequestParam String activity, 
                            Authentication authentication,Model model) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); 
        if (email != null) {
            HallPass pass = new HallPass();
            pass.setPersonId(email);
            pass.setTeacher_id(teacherId);
            pass.setCheckin(new Date(System.currentTimeMillis())); 
            pass.setPeriod(period);
            pass.setActivity(activity);
            tinkleRepository.save(pass);
            model.addAttribute("activePass", pass);
        }
        return "bathroom/thankyou"; // Return to student's page
    }

    @PostMapping("/checkoutPass")
    public String checkoutBathroomPass(Authentication authentication,
    Model model) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); 
        if (email != null) {
            Optional <HallPass> activePasses = tinkleRepository.findByPersonIdAndCheckoutIsNull(email);
            if (!activePasses.isEmpty()) {
                HallPass pass = activePasses.get();
                pass.setCheckout(new Date(System.currentTimeMillis())); 
                tinkleRepository.save(pass);
            }
        }
        return "bathroom/thankyou"; // Return to student's page
    }


}