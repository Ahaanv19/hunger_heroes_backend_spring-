package com.open.spring.mvc.bathroom;

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
public class BathroomQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String teacherEmail;
    private String peopleQueue;
    private int away;

    // Custom constructor

    /**
     * Constructor which creates each element in the queue
     * 
     * @param teacherEmail - the teacher's email for what class they are from
     * @param peopleQueue  - the people in the queue
     */
    public BathroomQueue(String teacherEmail, String peopleQueue) {
        this.teacherEmail = teacherEmail;
        this.peopleQueue = peopleQueue;
        this.away = 0;
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
    public void approveStudent() {
       if (this.peopleQueue != null && !this.peopleQueue.isEmpty()) {
            if (this.away == 0) {
                // Student is approved to go away
                this.away = 1;
            } else {
                // Student has returned; remove from queue
                String[] students = this.peopleQueue.split(",");
                if (students.length > 1) {
                    this.peopleQueue = String.join(",", Arrays.copyOfRange(students, 1, students.length));
                } else {
                    this.peopleQueue = "";
                }
                this.away = 0;
            }
        } else {
            throw new IllegalStateException("Queue is empty");
        }
    }

    /**
     * @return - initialize the queue
     */
    public static BathroomQueue[] init() {
        ArrayList<BathroomQueue> queues = new ArrayList<>();
        queues.add(new BathroomQueue("jmort1021@gmail.com", ""));
        return queues.toArray(new BathroomQueue[0]);
    }
}