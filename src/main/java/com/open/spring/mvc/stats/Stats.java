package com.open.spring.mvc.stats;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "module", "submodule"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // not added to db in backend

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String module; //module name ex. "resume"

    @Column(nullable = false)
    private int submodule; //submodule number ex. 3

    private Boolean finished; //completed? yes or no
    private double time; // time, in seconds, spent on the page
    private Double grades; // grade percentage or score for the module/submodule
}
