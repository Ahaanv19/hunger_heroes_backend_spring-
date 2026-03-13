package com.open.spring.mvc.resume;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExperience {
    private String jobTitle;
    private String company;
    private String dates;
    private String description;
}
