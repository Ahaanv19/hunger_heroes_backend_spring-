package com.open.spring.mvc.assignments;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Setter;

@Setter
public class AssignmentDto {
    private Long id;
    private String name;
    private String description;
    private String dueDate;
    private Double points;
    private String type;
    private List<AssignmentSubmissionsDTO> submissions;

    public AssignmentDto(Assignment assignment) {
        this.id = assignment.getId();
        this.name = assignment.getName();
        this.description = assignment.getDescription();
        this.dueDate = assignment.getDueDate();
        this.points = assignment.getPoints();
        this.type = assignment.getType();
        this.submissions = assignment.getSubmissions().stream()
                .map(AssignmentSubmissionsDTO::new)
                .collect(Collectors.toList());
    }
}