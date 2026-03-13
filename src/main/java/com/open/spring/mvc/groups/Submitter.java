package com.open.spring.mvc.groups;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.open.spring.mvc.assignments.AssignmentSubmission;
import com.open.spring.mvc.person.Person;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
// @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Person.class, name = "person"),
    @JsonSubTypes.Type(value = Groups.class, name = "group")
})
@Getter
@NoArgsConstructor
@JsonIgnoreProperties({"submissions"})
public abstract class Submitter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "submitter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference(value = "submitter-submissions")
    private List<AssignmentSubmission> submissions;

    @JsonIgnore
    public List<Person> getMembers() {
        if (this instanceof Groups) {
            return ((Groups) this).getGroupMembers();
        }
        return List.of((Person) this);
    }
}
