package com.open.spring.mvc.business;

import com.open.spring.mvc.person.PersonJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * REST controller for the Hunger Heroes business directory API.
 * Uses in-memory storage (mirrors the Flask businesses API).
 *
 * Public endpoints (no auth required):
 *   GET /api/businesses           – list all active businesses
 *   GET /api/businesses/{id}      – get a single business
 *
 * Authenticated endpoints:
 *   GET  /api/businesses/spotlight       – get current user's spotlighted IDs
 *   POST /api/businesses/spotlight       – toggle spotlight on a business
 *   POST /api/businesses/spotlight/sync  – sync localStorage spotlights
 *   GET  /api/businesses/spotlight/all   – get full data for spotlighted businesses
 *
 * Admin-only:
 *   POST   /api/businesses              – create business
 *   PUT    /api/businesses/{id}         – update business
 *   DELETE /api/businesses/{id}         – soft-delete business
 */
@RestController
@RequestMapping("/api/businesses")
public class BusinessApiController {

    @Autowired
    private PersonJpaRepository personRepo;

    // ── In-memory business store (seeded to match Flask) ──

    private static final List<Map<String, Object>> BUSINESSES = new ArrayList<>();
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(3);

    /** user id → set of spotlighted business ids */
    private static final Map<Long, Set<Integer>> USER_SPOTLIGHTS = new ConcurrentHashMap<>();

    static {
        BUSINESSES.add(buildBusiness(
                1,
                "ActiveMed Integrative Health Center",
                "We believe in a collaborative approach to healthcare. We offer acupuncture, massage therapy, functional medicine, physical therapy, and axon therapy.",
                "11588 Via Rancho San Diego, Suite 101, El Cajon, CA 92019",
                "https://activemedhealth.com/",
                "bus.png",
                "standard",
                "Healthcare",
                32.7914,
                -116.9259
        ));
        BUSINESSES.add(buildBusiness(
                2,
                "Digital One Printing",
                "Digital One Printing is your premier one-stop Poway printshop that offers a wide range of services, has many years of experience and a tremendous reputation.",
                "12630 Poway Rd, Poway, CA 92064",
                "https://d1printing.net/",
                "Screenshot 2025-07-23 at 8.34.48 AM.png",
                "wide",
                "Printing Services",
                32.9579,
                -117.0287
        ));
    }

    private static Map<String, Object> buildBusiness(int id, String name, String description,
            String address, String website, String image, String imageLayout,
            String category, double lat, double lng) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("id", id);
        b.put("name", name);
        b.put("description", description);
        b.put("address", address);
        b.put("website", website);
        b.put("image", image);
        b.put("image_layout", imageLayout);
        b.put("category", category);
        b.put("lat", lat);
        b.put("lng", lng);
        b.put("is_active", true);
        return b;
    }

    private Map<String, Object> formatFull(Map<String, Object> b) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", b.get("id"));
        r.put("name", b.get("name"));
        r.put("description", b.get("description"));
        r.put("address", b.get("address"));
        r.put("website", b.get("website"));
        r.put("image", b.get("image"));
        r.put("imageLayout", b.get("image_layout"));
        r.put("category", b.get("category"));
        r.put("coordinates", Map.of("lat", b.get("lat"), "lng", b.get("lng")));
        return r;
    }

    private Map<String, Object> formatMinimal(Map<String, Object> b) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", b.get("id"));
        r.put("name", b.get("name"));
        r.put("address", b.get("address"));
        r.put("category", b.get("category"));
        r.put("coordinates", Map.of("lat", b.get("lat"), "lng", b.get("lng")));
        r.put("website", b.get("website"));
        return r;
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        var person = personRepo.findByUid(userDetails.getUsername());
        return person != null ? person.getId() : null;
    }

    private Optional<Map<String, Object>> findById(int id) {
        return BUSINESSES.stream()
                .filter(b -> (Integer) b.get("id") == id && Boolean.TRUE.equals(b.get("is_active")))
                .findFirst();
    }

    // ── Public: GET /api/businesses ──

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getBusinesses() {
        List<Map<String, Object>> result = BUSINESSES.stream()
                .filter(b -> Boolean.TRUE.equals(b.get("is_active")))
                .map(this::formatFull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Public: GET /api/businesses/{id} ──

    @GetMapping("/{id}")
    public ResponseEntity<?> getBusiness(@PathVariable int id) {
        Optional<Map<String, Object>> opt = findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Business not found"));
        }
        return ResponseEntity.ok(formatFull(opt.get()));
    }

    // ── Authenticated: GET /api/businesses/spotlight ──

    @GetMapping("/spotlight")
    public ResponseEntity<Map<String, Object>> getUserSpotlights(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        List<Integer> ids = new ArrayList<>(USER_SPOTLIGHTS.getOrDefault(userId, Collections.emptySet()));
        return ResponseEntity.ok(Map.of("spotlighted_ids", ids));
    }

    // ── Authenticated: POST /api/businesses/spotlight ──

    @PostMapping("/spotlight")
    public ResponseEntity<Map<String, Object>> toggleSpotlight(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        if (!body.containsKey("business_id")) {
            return ResponseEntity.badRequest().body(Map.of("error", "business_id is required"));
        }
        if (!body.containsKey("spotlight")) {
            return ResponseEntity.badRequest().body(Map.of("error", "spotlight (boolean) is required"));
        }

        int businessId = Integer.parseInt(body.get("business_id").toString());
        boolean spotlight = Boolean.parseBoolean(body.get("spotlight").toString());

        if (findById(businessId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Business not found"));
        }

        USER_SPOTLIGHTS.computeIfAbsent(userId, k -> new HashSet<>());
        if (spotlight) {
            USER_SPOTLIGHTS.get(userId).add(businessId);
        } else {
            USER_SPOTLIGHTS.get(userId).remove(businessId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("business_id", businessId);
        response.put("spotlight", spotlight);
        return ResponseEntity.ok(response);
    }

    // ── Authenticated: POST /api/businesses/spotlight/sync ──

    @PostMapping("/spotlight/sync")
    public ResponseEntity<Map<String, Object>> syncSpotlights(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }
        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        @SuppressWarnings("unchecked")
        List<Object> localIds = body.containsKey("spotlighted_ids")
                ? (List<Object>) body.get("spotlighted_ids") : Collections.emptyList();

        USER_SPOTLIGHTS.computeIfAbsent(userId, k -> new HashSet<>());
        for (Object idObj : localIds) {
            int bid = Integer.parseInt(idObj.toString());
            if (findById(bid).isPresent()) {
                USER_SPOTLIGHTS.get(userId).add(bid);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("spotlighted_ids", new ArrayList<>(USER_SPOTLIGHTS.get(userId)));
        return ResponseEntity.ok(response);
    }

    // ── Authenticated: GET /api/businesses/spotlight/all ──

    @GetMapping("/spotlight/all")
    public ResponseEntity<List<Map<String, Object>>> getSpotlightedBusinesses(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Long userId = resolveUserId(userDetails);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Set<Integer> ids = USER_SPOTLIGHTS.getOrDefault(userId, Collections.emptySet());
        List<Map<String, Object>> result = BUSINESSES.stream()
                .filter(b -> ids.contains(b.get("id")) && Boolean.TRUE.equals(b.get("is_active")))
                .map(this::formatMinimal)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Admin: POST /api/businesses ──

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBusiness(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        String[] required = {"name", "address", "category", "lat", "lng"};
        for (String field : required) {
            if (!body.containsKey(field)) {
                return ResponseEntity.badRequest().body(Map.of("error", field + " is required"));
            }
        }

        Map<String, Object> business = new LinkedHashMap<>();
        business.put("id", ID_COUNTER.getAndIncrement());
        business.put("name", body.get("name"));
        business.put("description", body.getOrDefault("description", ""));
        business.put("address", body.get("address"));
        business.put("website", body.getOrDefault("website", ""));
        business.put("image", body.getOrDefault("image", ""));
        business.put("image_layout", body.getOrDefault("image_layout", "standard"));
        business.put("category", body.get("category"));
        business.put("lat", Double.parseDouble(body.get("lat").toString()));
        business.put("lng", Double.parseDouble(body.get("lng").toString()));
        business.put("is_active", true);

        BUSINESSES.add(business);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Business created successfully", "business", formatFull(business)));
    }

    // ── Admin: PUT /api/businesses/{id} ──

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBusiness(
            @PathVariable int id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        Optional<Map<String, Object>> opt = BUSINESSES.stream()
                .filter(b -> (Integer) b.get("id") == id).findFirst();
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Business not found"));
        }

        Map<String, Object> business = opt.get();
        List<String> allowed = Arrays.asList("name", "description", "address", "website", "image",
                "image_layout", "category", "lat", "lng", "is_active");
        for (String field : allowed) {
            if (body.containsKey(field)) {
                business.put(field, body.get(field));
            }
        }

        return ResponseEntity.ok(Map.of("message", "Business updated successfully", "business", formatFull(business)));
    }

    // ── Admin: DELETE /api/businesses/{id} ──

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBusiness(
            @PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required"));
        }

        Optional<Map<String, Object>> opt = BUSINESSES.stream()
                .filter(b -> (Integer) b.get("id") == id).findFirst();
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Business not found"));
        }

        opt.get().put("is_active", false);
        USER_SPOTLIGHTS.values().forEach(s -> s.remove(id));

        return ResponseEntity.ok(Map.of("message", "Business deleted successfully"));
    }
}
