package com.open.spring.mvc.grades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.PersonJpaRepository;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private PersonJpaRepository personRepository;

    @GetMapping
    public List<Grade> getAllGrades() {
        List<Grade> out = new ArrayList<>();
        for (com.open.spring.mvc.person.Person p : personRepository.findAll()) {
            List<Map<String, Object>> gjs = p.getGradesJson();
            if (gjs == null) continue;
            for (Map<String, Object> m : gjs) {
                Grade g = mapToGrade(m);
                if (g.getUid() == null) g.setUid(p.getUid());
                out.add(g);
            }
        }
        return out;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        for (com.open.spring.mvc.person.Person p : personRepository.findAll()) {
            List<Map<String, Object>> gjs = p.getGradesJson();
            if (gjs == null) continue;
            for (Map<String, Object> m : gjs) {
                Object mid = m.get("id");
                if (mid != null && Long.valueOf(String.valueOf(mid)).equals(id)) {
                    Grade g = mapToGrade(m);
                    if (g.getUid() == null) g.setUid(p.getUid());
                    return ResponseEntity.ok(g);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{uid}")
    public List<Grade> getGradesByStudent(@PathVariable String uid) {
        com.open.spring.mvc.person.Person p = personRepository.findByUid(uid);
        List<Grade> out = new ArrayList<>();
        if (p == null) return out;
        List<Map<String, Object>> gjs = p.getGradesJson();
        if (gjs == null) return out;
        for (Map<String, Object> m : gjs) {
            Grade g = mapToGrade(m);
            if (g.getUid() == null) g.setUid(uid);
            out.add(g);
        }
        return out;
    }

    @GetMapping("/assignment/{assignment}")
    public List<Grade> getGradesByAssignment(@PathVariable String assignment) {
        List<Grade> out = new ArrayList<>();
        for (com.open.spring.mvc.person.Person p : personRepository.findAll()) {
            List<Map<String, Object>> gjs = p.getGradesJson();
            if (gjs == null) continue;
            for (Map<String, Object> m : gjs) {
                Object asg = m.get("assignment");
                if (assignment.equals(String.valueOf(asg))) {
                    Grade g = mapToGrade(m);
                    if (g.getUid() == null) g.setUid(p.getUid());
                    out.add(g);
                }
            }
        }
        return out;
    }

    @PostMapping
    public Grade createGrade(@RequestBody Grade grade) {
        com.open.spring.mvc.person.Person p = personRepository.findByUid(grade.getUid());
        if (p == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("assignment", grade.getAssignment());
        m.put("score", grade.getScore());
        m.put("teacherComments", grade.getTeacherComments());
        m.put("submission", grade.getSubmission());
        if (p.getGradesJson() == null) {
            p.setGradesJson(new ArrayList<>());
        }
        p.getGradesJson().add(m);
        personRepository.save(p);
        return grade;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grade> updateGrade(@PathVariable Long id, @RequestBody Grade gradeDetails) {
        for (com.open.spring.mvc.person.Person p : personRepository.findAll()) {
            List<Map<String, Object>> gjs = p.getGradesJson();
            if (gjs == null) continue;
            for (Map<String, Object> m : gjs) {
                Object mid = m.get("id");
                if (mid != null && Long.valueOf(String.valueOf(mid)).equals(id)) {
                    m.put("assignment", gradeDetails.getAssignment());
                    m.put("score", gradeDetails.getScore());
                    m.put("teacherComments", gradeDetails.getTeacherComments());
                    m.put("submission", gradeDetails.getSubmission());
                    personRepository.save(p);
                    Grade g = mapToGrade(m);
                    if (g.getUid() == null) g.setUid(p.getUid());
                    return ResponseEntity.ok(g);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        for (com.open.spring.mvc.person.Person p : personRepository.findAll()) {
            List<Map<String, Object>> gjs = p.getGradesJson();
            if (gjs == null) continue;
            boolean removed = gjs.removeIf(m -> {
                Object mid = m.get("id");
                return mid != null && Long.valueOf(String.valueOf(mid)).equals(id);
            });
            if (removed) {
                personRepository.save(p);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    private Grade mapToGrade(Map<String, Object> m) {
        if (m == null) return null;
        Object idObj = m.get("id");
        Long id = idObj == null ? null : Long.valueOf(String.valueOf(idObj));
        String uid = (String) m.get("uid");
        String assignment = m.get("assignment") == null ? null : String.valueOf(m.get("assignment"));
        Double score = null;
        Object scoreObj = m.get("score");
        if (scoreObj != null) {
            try {
                score = Double.valueOf(String.valueOf(scoreObj));
            } catch (NumberFormatException e) {
                score = null;
            }
        }
        String teacherComments = m.get("teacherComments") == null ? null : String.valueOf(m.get("teacherComments"));
        String submission = m.get("submission") == null ? null : String.valueOf(m.get("submission"));
        Grade g = new Grade(uid, assignment, score, teacherComments, submission);
        g.setId(id);
        return g;
    }
}