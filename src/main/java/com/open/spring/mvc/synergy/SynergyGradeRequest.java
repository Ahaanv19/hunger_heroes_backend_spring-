package com.open.spring.mvc.synergy;

import com.open.spring.mvc.assignments.Assignment;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SynergyGradeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Short status; // 0 = pending, 1 = accepted, 2 = rejected
    
    private String explanation;

    @NotNull
    private Double gradeSuggestion;

    @NotNull
    @ManyToOne
    private Person grader;
    
    @NotNull
    @ManyToOne
    private Person student;

    @NotNull
    @ManyToOne
    private Assignment assignment;


    public SynergyGradeRequest(Assignment assignment, Person student, Person grader, String explanation, Double gradeSuggestion) {
        this.gradeSuggestion = gradeSuggestion;
        this.explanation = explanation;
        this.grader = grader;
        this.student = student;
        this.assignment = assignment;
        this.status = 0;
    }

    @Override
    public String toString() {
        return "SynergyGradeRequest{id=" + id + ", explanation=" + explanation + ", gradeSuggestion=" + gradeSuggestion + ", status=" + status + ", grader=" + grader.getName() + ", student=" + student.getName() + ", assignment=" + assignment.getName() + "}";
    }

    public void accept() {
        this.status = 1;
    }

    public void reject() {
        this.status = 2;
    }

    public boolean isAccepted() {
        return this.status == 1;
    }
}
