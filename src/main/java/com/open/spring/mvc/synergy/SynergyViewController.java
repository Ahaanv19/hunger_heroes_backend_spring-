package com.open.spring.mvc.synergy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.open.spring.mvc.assignments.Assignment;
import com.open.spring.mvc.assignments.AssignmentJpaRepository;
import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;

import lombok.Getter;
import lombok.Setter;

@Controller
@RequestMapping("/mvc/synergy")
public class SynergyViewController {
    @Autowired
    private SynergyGradeJpaRepository gradeRepository;
    
    @Autowired
    private SynergyGradeRequestJpaRepository gradeRequestRepository;

    @Autowired
    private AssignmentJpaRepository assignmentRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    @Getter
    public static class SynergyGradeRequestDto {
        private String assignmentName;
        private String explanation;
        private Double gradeSuggestion;
        private String graderName;
        private String studentName;
        private Long id;
        
        public SynergyGradeRequestDto(SynergyGradeRequest request) {
            this.assignmentName = request.getAssignment().getName();
            this.explanation = request.getExplanation();
            this.gradeSuggestion = request.getGradeSuggestion();
            this.graderName = request.getGrader().getName();
            this.studentName = request.getStudent().getName();
            this.id = request.getId();
        }
    }
    
    @Getter
    @Setter
    public class StudentGradeDto {
        private Long assignmentId;
        private Double grade;

        public StudentGradeDto(Long assignmentId, Double grade) {
            this.assignmentId = assignmentId;
            this.grade = grade;
        }
    }

    @Getter
    @Setter
    public class StudentGradeRowDto {
        private Long studentId;
        private String studentName;
        private List<StudentGradeDto> grades;

        public StudentGradeRowDto(Long studentId, String studentName, List<StudentGradeDto> grades) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.grades = grades;
        }
    }

    /**
     * Opens the teacher or student gradebook. The teacher gradebook is for editing grades, while the student gradebook allows them to view grades.
     * @param model The parameters for the webpage
     * @param userDetails The details of the logged in user
     * @return The template for the gradebook
     */
    @GetMapping("/gradebook")
    public String editGrades(Model model, @AuthenticationPrincipal UserDetails userDetails) throws ResponseStatusException {
        // Load the user
        String uid = userDetails.getUsername();
        Person user = personRepository.findByUid(uid);
        if (user == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You must be a logged in user to view this"
            );
        }

        // Load the assignments
        List<Assignment> assignments = assignmentRepository.findAll();

        // If the user is a student, allow them to view their grades, else allow them to edit grades
        if (user.hasRoleWithName("ROLE_TEACHER") || user.hasRoleWithName("ROLE_ADMIN")) {
            // Load info from db, for now we'll show everyone but ideally it should only show students in future
            // List<Person> students = personRepository.findPeopleWithRole("ROLE_STUDENT");
            List<Person> students = personRepository.findAll();
            List<SynergyGrade> gradesList = gradeRepository.findAll();
            List<SynergyGradeRequest> gradeRequests = gradeRequestRepository.findAll();

            List<StudentGradeRowDto> studentGradeRows = buildStudentGradeRows(students, assignments, gradesList);
            
            // Preprocess pending requests into a map so that they can be easily accessed on the frontend
            Map<String, List<SynergyGradeRequestDto>> pendingRequestsMap = new HashMap<>();
            for (SynergyGradeRequest request : gradeRequests) {
                if (request.getStatus() == 0) {  // Only include pending requests
                    String key = request.getAssignment().getId().toString() + "-" + request.getStudent().getId().toString();
                    pendingRequestsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(new SynergyGradeRequestDto(request));
                }
            }
            
            // Pass in information to thymeleaf template
            model.addAttribute("assignments", assignments);
            model.addAttribute("studentGradeRows", studentGradeRows);
            model.addAttribute("pendingRequestsMap", pendingRequestsMap);
            model.addAttribute("gradeRequests", gradeRequests);

            return "synergy/edit_grades";
        } else if (user.hasRoleWithName("ROLE_STUDENT")) {
            List<SynergyGrade> studentGrades = gradeRepository.findByStudent(user);
        
            Map<Long, SynergyGrade> assignmentGrades = new HashMap<>();
            for (SynergyGrade grade : studentGrades) {
                assignmentGrades.put(grade.getAssignment().getId(), grade);
            }
        
            model.addAttribute("assignments", assignments);
            model.addAttribute("assignmentGrades", assignmentGrades);
            return "synergy/view_student_grades";
        }

        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "You must a student, teacher, or admin to view grades."
        );
    }

    private List<StudentGradeRowDto> buildStudentGradeRows(List<Person> students, List<Assignment> assignments, List<SynergyGrade> allGrades) {
        // Build a lookup table: Map<studentId-assignmentId, grade>
        Map<String, Double> gradeLookup = new HashMap<>();
        for (SynergyGrade g : allGrades) {
            String key = g.getStudent().getId() + "-" + g.getAssignment().getId();
            gradeLookup.put(key, g.getGrade());
        }

        List<StudentGradeRowDto> rows = new ArrayList<>();

        for (Person student : students) {
            List<StudentGradeDto> gradeDtos = new ArrayList<>();
            for (Assignment assignment : assignments) {
                String key = student.getId() + "-" + assignment.getId();
                Double grade = gradeLookup.getOrDefault(key, null);
                gradeDtos.add(new StudentGradeDto(assignment.getId(), grade));
            }
            rows.add(new StudentGradeRowDto(student.getId(), student.getName(), gradeDtos));
        }

        return rows;
    }

    /**
     * A page to view grade requests.
     * @param model The parameters for the webpage
     * @return The template for viewing grade requests
     */
    @GetMapping("/view-grade-requests")
    public String viewRequests(Model model) {
        List<SynergyGradeRequest> requests = gradeRequestRepository.findAll();
        model.addAttribute("requests", requests);
        return "synergy/view_grade_requests";
    }

    /**
     * A page to create grade requests.
     * @param model The parameters for the webpage
     * @return The template for create grade requests
     */
    @GetMapping("/create-grade-request")
    public String createGradeRequest(Model model) {
        List<Assignment> assignments = assignmentRepository.findAll();
        List<Person> students = personRepository.findPeopleWithRole("ROLE_STUDENT");

        model.addAttribute("assignments", assignments);
        model.addAttribute("students", students);

        return "synergy/create_grade_request";
    }
}
