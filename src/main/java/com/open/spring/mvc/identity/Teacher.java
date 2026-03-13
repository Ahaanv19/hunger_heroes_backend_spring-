package com.open.spring.mvc.identity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity(name = "IdentityTeacher")
@DiscriminatorValue("TEACHER")
public class Teacher extends User {

    // Teacher specific fields can be added here
    // For example:
    // private String subject;

    public Teacher(String email, String passwordHash) {
        super();
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
    }
}
