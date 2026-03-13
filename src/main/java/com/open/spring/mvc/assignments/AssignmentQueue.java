package com.open.spring.mvc.assignments;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
@JsonDeserialize(using = AssignmentQueueDeserializer.class)
public class AssignmentQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    private List<String> working;
    private List<String> waiting;
    private List<String> completed;

    // New queues will be initialized as three empty lists.
    public AssignmentQueue() {
        this.working = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.completed = new ArrayList<>();
    }

    // Reset queue: clears all three lists in queue.
    public void reset() {
        this.working.clear();
        this.waiting.clear();
        this.completed.clear();
    }
}
