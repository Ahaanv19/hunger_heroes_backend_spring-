package com.open.spring.mvc.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
public class StudentQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String teacherEmail;
    private String peopleQueue;

    // Custom constructor

    /**
     * Constructor which creates each element in the queue
     * 
     * @param teacherEmail - the teacher's email for what class they are from
     * @param peopleQueue  - the people in the queue
     */
    public StudentQueue(String teacherEmail, String peopleQueue) {
        this.teacherEmail = teacherEmail;
        this.peopleQueue = peopleQueue;
    }

    /**
     * Function to add a student to the queue
     * 
     * @param studentName - the name you want to add to the queue
     */

    public void addStudent(String studentName) {
        if (this.peopleQueue == null || this.peopleQueue.isEmpty()) {
            this.peopleQueue = studentName;
        } else {
            this.peopleQueue += "," + studentName;
        }
    }

    /**
     * Function to remove the student from a queue
     * 
     * @param studentName - the name you want to remove from the queue. In frontend,
     *                    your own name is passed.
     */
    public void removeStudent(String studentName) {
        if (this.peopleQueue != null) {
            this.peopleQueue = Arrays.stream(this.peopleQueue.split(","))
                    .filter(s -> !s.equals(studentName))
                    .collect(Collectors.joining(","));
        }
    }

    /**
     * @return - returns the student who is at the front of the line, removing the
     *         commas and sanitizing the data
     */
    public String getFrontStudent() {
        if (this.peopleQueue != null && !this.peopleQueue.isEmpty()) {
            return this.peopleQueue.split(",")[0];
        }
        return null;
    }

    /**
     * Students need to be approved to go to the bathroom by the teacher
     * When they are, their status is set to away
     * When they return, they are removed from the queue
     */

    /**
     * @return - initialize the queue
     */
    public static StudentQueue[] init() {
        ArrayList<StudentQueue> queues = new ArrayList<>();
        queues.add(new StudentQueue("jm1021@gmail.com", ""));
        return queues.toArray(new StudentQueue[0]);
    }
}