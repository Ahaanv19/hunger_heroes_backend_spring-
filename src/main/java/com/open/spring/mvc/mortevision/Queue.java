package com.open.spring.mvc.mortevision;

import java.util.ArrayList;
import java.util.List;

// New Queue class to represent the three lists
public class Queue {
    private List<String> haventGone;
    private List<String> queue;
    private List<String> done;

    public Queue() {
        this.haventGone = new ArrayList<>();
        this.queue = new ArrayList<>();
        this.done = new ArrayList<>();
    }

    // Getters and setters for the lists
    public List<String> getHaventGone() {
        return haventGone;
    }

    public void setHaventGone(List<String> haventGone) {
        this.haventGone = haventGone;
    }

    public List<String> getQueue() {
        return queue;
    }

    public void setQueue(List<String> queue) {
        this.queue = queue;
    }

    public List<String> getDone() {
        return done;
    }

    public void setDone(List<String> done) {
        this.done = done;
    }

    public void reset() {
        this.haventGone.clear();
        this.queue.clear();
        this.done.clear();
    }
}
