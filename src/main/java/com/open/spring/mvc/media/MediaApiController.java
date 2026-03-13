package com.open.spring.mvc.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * MediaApiController class:
 *  - Top ranked user is returned by default
 *  - Top ranked user is able to be returned
 */

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "http://localhost:4500") // Enable CORS
public class MediaApiController {

    @Autowired
    private MediaJpaRepository mediaJpaRepository;

    private static void heapify(List<Score> list, int n, int i) {
        // Helper method to ensure the subtree rooted at index `i` satisfies the max-heap property.
        // Initialize largest as root and left and right children
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        // If left child is larger than root, set largest to left
        if (l < n && list.get(l).getScore() > list.get(largest).getScore()) {
            largest = l;
        }

        // If right child is larger than largest so far, set largest to right
        if (r < n && list.get(r).getScore() > list.get(largest).getScore()) {
            largest = r;
        }

        // If largest is not root, swap the i-th element with the largest element
        if (largest != i) {
            Score temp = list.get(i);
            list.set(i, list.get(largest));
            list.set(largest, temp);

            // Recursively heapify the affected subtree
            heapify(list, n, largest);
        }
    }

    // Heap sort implementation (O(n log n) complexity)
    private static List<Score> heapSort(List<Score> list) {
        int n = list.size();

        // Build heap (rearrange array into max-heap structure)
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(list, n, i);
        }

        // One by one, extract elements from the heap and place them at the end
        for (int i = n - 1; i > 0; i--) {
            Score temp = list.get(0);
            list.set(0, list.get(i));
            list.set(i, temp);
  
            // Restore heap property after each extraction
            heapify(list, i, 0);
        }

        // After the array is fully sorted into ascending order, return the list
        return list;
    }

    @GetMapping("/")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<Score> scoresList = mediaJpaRepository.findAll();

        if (scoresList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Sort scores using heap sort
        Collections.reverse(heapSort(scoresList));


        /*
         * Create entries for the leaderboard which is accessed through GET to /api/media
         * - rank: the position of the user in the leaderboard
         * - username: the name of the user
         * - score: the score of the user
         */
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (int i = 0; i < scoresList.size(); i++) {
            Score score = scoresList.get(i);

            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", i + 1);
            entry.put("username", score.getPersonName());
            entry.put("score", score.getScore());

            leaderboard.add(entry);
        }

        return ResponseEntity.ok(leaderboard);
    }

    // POST to accept score from frontend
    @PostMapping("/score/{personName}/{score}")
    public synchronized ResponseEntity<Score> postScore(@PathVariable String personName, @PathVariable int score) {
        List<Score> existingScores = mediaJpaRepository.findByPersonName(personName);
        if (!existingScores.isEmpty()) {
            Score existingScore = existingScores.get(0);
            if (existingScore.getScore() < score) {
                existingScore.setScore(score);
                mediaJpaRepository.save(existingScore);
                mediaJpaRepository.flush();
            }
            return new ResponseEntity<>(existingScore, HttpStatus.OK);
        } else {
            Score newScore = new Score(personName, score);
            mediaJpaRepository.save(newScore);
            mediaJpaRepository.flush();
            return new ResponseEntity<>(newScore, HttpStatus.CREATED);
        }
    }
}
