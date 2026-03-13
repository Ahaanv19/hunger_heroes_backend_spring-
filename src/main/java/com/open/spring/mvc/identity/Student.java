package com.open.spring.mvc.identity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity(name = "IdentityStudent")
@DiscriminatorValue("STUDENT")
public class Student extends User {

    // Student specific fields can be added here
    // For example:
    // private int gradeLevel;
    // private double gpa;

    public Student(String email, String passwordHash) {
        super();
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
    }
}
