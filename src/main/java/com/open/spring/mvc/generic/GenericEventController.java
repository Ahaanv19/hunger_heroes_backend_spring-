package com.open.spring.mvc.generic;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class GenericEventController {

    @Autowired
    private AlgorithmicEventRepository eventRepository;

    @PostMapping("/{type}")
    public ResponseEntity<AlgorithmicEvent> logEvent(
            @PathVariable EventType type,
            @RequestBody AlgorithmicEvent event) {

        // Store provided userId verbatim (may be null for anonymous)
        event.setType(type);
        AlgorithmicEvent saved = eventRepository.save(event);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{param}")
    public ResponseEntity<?> listEvents(@PathVariable String param) {
        // Try to parse as Long (id)
        try {
            Long id = Long.parseLong(param);
            return eventRepository.findById(id)
                    .map(event -> new ResponseEntity<Object>(event, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (NumberFormatException e) {
            // Not a number, try as EventType
            try {
                EventType type = EventType.valueOf(param.toUpperCase());
                return new ResponseEntity<>(eventRepository.findByType(type), HttpStatus.OK);
            } catch (IllegalArgumentException ex) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlgorithmicEvent> updateEvent(
            @PathVariable Long id,
            @RequestBody AlgorithmicEvent updatedEvent) {

        Optional<AlgorithmicEvent> existing = eventRepository.findById(id);
        if (existing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        AlgorithmicEvent event = existing.get();

        // No owner-by-email checks; update allowed
        if (updatedEvent.getType() != null) {
            event.setType(updatedEvent.getType());
        }
        event.setPayload(updatedEvent.getPayload());
        // allow replacing userId if provided
        event.setUserId(updatedEvent.getUserId());

        AlgorithmicEvent saved = eventRepository.save(event);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Optional<AlgorithmicEvent> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        eventRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
