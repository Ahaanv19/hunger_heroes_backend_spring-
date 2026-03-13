package com.open.spring.mvc.bathroom;
import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String bathroom;

    private String issue;
    
    private int count;

    private double positionX;

    private double positionY;

    public Issue(String bathroomName, String issue, int count, double positionX, double positionY)
        {
            this.bathroom = bathroomName;
            this.issue = issue;
            this.count = count;
            this.positionX = positionX;
            this.positionY = positionY;

    }

    public static Issue[] init()
    {
        ArrayList<Issue> issues = new ArrayList<>();
        issues.add(new Issue("D Building Bathroom", "No Door Lock", 0, 0.47f, 0.235f));
        issues.add(new Issue("D Building Bathroom", "No Toilet Paper", 0,0.47f, 0.235f));
        issues.add(new Issue("D Building Bathroom", "No Stall Doors", 0,0.47f, 0.235f));

        issues.add(new Issue("L Building Bathroom", "No Door Lock", 0,0.30f, 0.28f));
        issues.add(new Issue("L Building Bathroom", "No Toilet Paper", 0,0.30f, 0.28f));
        issues.add(new Issue("L Building Bathroom", "No Stall Doors", 0,0.30f, 0.28f));

        issues.add(new Issue("A Building Bathroom", "No Door Lock", 0,0.31f, 0.49f));
        issues.add(new Issue("A Building Bathroom", "No Toilet Paper", 0,0.31f, 0.49f));
        issues.add(new Issue("A Building Bathroom", "No Stall Doors", 0,0.31f, 0.49f));

        issues.add(new Issue("Football Field Bathroom", "No Door Lock", 0,0.71f, 0.42f));
        issues.add(new Issue("Football Field Bathroom", "No Toilet Paper", 0,0.71f, 0.42f));
        issues.add(new Issue("Football Field Bathroom", "No Stall Doors", 0,0.71f, 0.42f));

        issues.add(new Issue("Locker Room Bathroom", "No Door Lock", 0,0.67f, 0.71f));
        issues.add(new Issue("Locker Room Bathroom", "No Toilet Paper", 0,0.67f, 0.71f));
        issues.add(new Issue("Locker Room Bathroom", "No Stall Doors", 0,0.67f, 0.71f));
        return issues.toArray(new Issue[0]);
    }
}
