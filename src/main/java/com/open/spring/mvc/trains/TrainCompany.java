package com.open.spring.mvc.trains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.open.spring.mvc.person.Person;

import java.util.*;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainCompany {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JsonIgnore
    private Person owner;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Train> trains;

    @OneToOne(mappedBy = "company",  cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    TrainStation station;

    private String companyName;

    private Float balance;
}
