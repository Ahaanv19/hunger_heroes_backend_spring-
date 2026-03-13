package com.open.spring.mvc.mortevision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TasksApiController {

    @Autowired
    private TasksJpaRepository repository;

    // GET all tasks
    @GetMapping("/")
    public ResponseEntity<List<Tasks>> getAllTasks() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    // GET a specific task by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tasks> getTaskById(@PathVariable Long id) {
        Optional<Tasks> task = repository.findById(id);
        return task.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // CREATE a new task
    @PostMapping("/create")
    public ResponseEntity<Tasks> createTask(@RequestBody Tasks task) {
        repository.save(task);
        return new ResponseEntity<>(task, HttpStatus.CREATED);
    }

    @GetMapping("/getTasks/{username}")
    public ResponseEntity<List<Tasks>> getTasksByUsername(@PathVariable String username) {
        List<Tasks> tasks = repository.findByUsernameOrderByDueDateAsc(username);
        if (!tasks.isEmpty()) {
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Tasks> updateTask(@PathVariable Long id, @RequestBody Tasks updatedTask) {
        Optional<Tasks> optionalTask = repository.findById(id);
        if (optionalTask.isPresent()) {
            Tasks task = optionalTask.get();

            // Update task details if fields are provided
            if (updatedTask.getDueDate() != null) task.setDueDate(updatedTask.getDueDate());
            if (updatedTask.getStatus() != null) task.setStatus(updatedTask.getStatus());
            if (updatedTask.getDescription() != null) task.setDescription(updatedTask.getDescription());

            Tasks savedTask = repository.save(task); // Save the updated task
            return new ResponseEntity<>(savedTask, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Task not found
    }

    // DELETE a task
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteTask(@PathVariable Long id) {
        repository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
