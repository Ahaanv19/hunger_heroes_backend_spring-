package com.open.spring.mvc.teamteach;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TeacherGradingTeamTeach {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Topic topic;
    private String student;
    private Double grade;
    private String comment;
    
    public TeacherGradingTeamTeach(Topic topic, String student, Double grade, String comment) {
        this.grade = grade;
        this.student = student;
        this.comment = comment;
        this.topic = topic;

    }

    // Getters and Setters (if not using Lombok)
    public Long getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }
    public String getStudent() {
        return student;
    }

    public String getComment() {
        return comment;
    }

    public Double getGrade() {
        return grade;
    }

         /** 
      * This method is an static message to create a Default Team Grade
     * @param topic the team
     * @param grade the grade
     * @param comment the comment of the grade
     * @return the TeacherGradingTeamTeach object;
     */
    public static TeacherGradingTeamTeach createTeacherGrade(Topic topic, String student, Double grade, String comment) {
        TeacherGradingTeamTeach teamgrade = new TeacherGradingTeamTeach(topic,student, grade, comment);
        return teamgrade;
    }
    

      /** Static method to initialize an array list of TeacherGradingTeamTeach objects 
     * @return TeacherGradingTeamTeach[], an array of TeacherGradingTeamTeach objects
     */
    public static TeacherGradingTeamTeach[] init() {
        ArrayList<TeacherGradingTeamTeach> teacherGradingTeamTeachs = new ArrayList<>();
        // teacherGradingTeamTeachs.add(createTeacherGrade(1, 97.2, "Great job"));
        // teacherGradingTeamTeachs.add(createTeacherGrade(1, 87.2, "Nice job"));
        // teacherGradingTeamTeachs.add(createTeacherGrade(1, 77.2, "Good job"));
        return teacherGradingTeamTeachs.toArray(new TeacherGradingTeamTeach[0]);

    }
    
    public static void main(String[] args) {
        // obtain TeacherGradingTeamTeach from initializer
        TeacherGradingTeamTeach teacherGradingTeamTeachs[] = init();

        // iterate using "enhanced for loop"
        for( TeacherGradingTeamTeach teacherGrade : teacherGradingTeamTeachs) {
            System.out.println(teacherGrade);  // print object
        }
    }
    public static TeacherGradingTeamTeach createComment(String comnt) {
        TeacherGradingTeamTeach comment = new TeacherGradingTeamTeach();
        comment.setComment(comnt);
        return comment;
    }
}