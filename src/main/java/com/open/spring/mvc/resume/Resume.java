package com.open.spring.mvc.resume;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username; // Linking resume to User (simplified for now)

    private String professionalSummary;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<JobExperience> experiences;

    public static Resume[] init() {
        Resume madam = new Resume(
            null,
            "madam",
            "Experienced learner with interests in CS and problem solving.",
            Arrays.asList(
                new JobExperience(
                    "Student Developer",
                    "DNHS",
                    "2024-2025",
                    "Built Spring Boot APIs, worked with SQLite, and improved classroom tools."),
                new JobExperience(
                    "Team Collaborator",
                    "Nighthawk Labs",
                    "2025",
                    "Contributed to REST endpoints, testing, and documentation.")
            )
        );

        return new Resume[] { madam };
    }
}
