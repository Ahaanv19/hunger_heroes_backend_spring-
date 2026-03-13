package com.open.spring.mvc.mortevision;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.Date;

import com.open.spring.mvc.assignments.Assignment;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String taskName;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Temporal(TemporalType.DATE)  // Change to DATE to store only the date portion
    private Date dueDate; // New field for due date

    private String status;

    private String description;

    private Long points;

    // Additional fields or methods if needed
}
