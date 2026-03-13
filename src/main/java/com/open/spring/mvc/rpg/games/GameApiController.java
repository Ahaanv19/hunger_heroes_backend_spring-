package com.open.spring.mvc.rpg.games;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameApiController {

    @Autowired
    private UnifiedGameRepository repo;

    @GetMapping("/combined/{personid}")
    public ResponseEntity<?> combined(@PathVariable("personid") Long personid) {
        List<Game> rows = repo.findByPersonId(personid);
        Map<String, Object> out = new HashMap<>();
        out.put("count", rows.size());
        double balance = rows.stream().mapToDouble(g -> g.getBalance() == null ? 0.0 : g.getBalance()).sum();
        out.put("balance", balance);
        out.put("rows", rows);
        return ResponseEntity.ok(out);
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody Game game) {
        Game saved = repo.save(game);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody Game game) {
        game.setId(id);
        Game saved = repo.save(game);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
