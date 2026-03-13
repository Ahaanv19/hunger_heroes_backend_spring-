package com.open.spring.mvc.donation;

import com.open.spring.mvc.person.PersonJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for the Hunger Heroes donation API.
 * Mirrors the Flask donation API endpoints from hunger_heroes_backend.
 *
 * Public endpoints (no authentication required):
 *   GET  /api/donations           – list/search donations
 *   POST /api/donations           – create a donation
 *   GET  /api/donations/stats     – aggregate stats
 *   POST /api/donations/scan      – scan a QR/barcode
 *   GET  /api/donations/{id}      – get single donation
 *   POST /api/donation/{id}/accept  – legacy accept (claimed)
 *   POST /api/donation/{id}/deliver – legacy deliver
 *
 * Authenticated endpoints:
 *   DELETE /api/donations/{id}        – archive donation
 *   PATCH  /api/donations/{id}/status – update status
 */
@RestController
@RequestMapping("/api")
public class DonationApiController {

    // ── Allowed enum values (mirrors Flask model) ──
    private static final List<String> ALLOWED_CATEGORIES = Arrays.asList(
            "canned", "fresh-produce", "dairy", "bakery", "meat-protein",
            "grains", "beverages", "frozen", "snacks", "baby-food",
            "prepared-meals", "condiments", "other"
    );
    private static final List<String> ALLOWED_UNITS = Arrays.asList(
            "lbs", "kg", "oz", "g", "items", "cans", "boxes", "bags", "liters", "gallons", "servings"
    );
    private static final List<String> ALLOWED_STORAGE = Arrays.asList(
            "room-temp", "refrigerated", "frozen", "dry-storage"
    );
    private static final List<String> ALLOWED_ALLERGENS = Arrays.asList(
            "gluten", "dairy", "eggs", "nuts", "peanuts", "soy", "fish",
            "shellfish", "sesame", "none"
    );
    private static final List<String> ALLOWED_DIETARY = Arrays.asList(
            "vegan", "vegetarian", "halal", "kosher", "gluten-free",
            "dairy-free", "nut-free", "organic", "low-sodium"
    );
    private static final List<String> ALLOWED_FOOD_TYPES = Arrays.asList(
            "non-perishable", "perishable", "frozen", "prepared", "produce"
    );
    private static final List<String> ALLOWED_STORAGE_METHODS = Arrays.asList(
            "pantry", "refrigerator", "freezer", "cool-dry-place", "room-temperature"
    );
    private static final Map<String, List<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put("posted",     Arrays.asList("claimed", "cancelled", "expired"));
        VALID_TRANSITIONS.put("claimed",    Arrays.asList("in_transit", "cancelled"));
        VALID_TRANSITIONS.put("in_transit", Arrays.asList("delivered", "cancelled"));
        VALID_TRANSITIONS.put("delivered",  Collections.singletonList("confirmed"));
        VALID_TRANSITIONS.put("confirmed",  Collections.emptyList());
        VALID_TRANSITIONS.put("expired",    Collections.emptyList());
        VALID_TRANSITIONS.put("cancelled",  Collections.emptyList());
    }

    @Autowired
    private DonationJpaRepository donationRepo;

    @Autowired
    private PersonJpaRepository personRepo;

    // ── Helpers ──

    /** Generate a human-readable donation ID: DON-{hex4}-{hex4} */
    private String generateDonationId() {
        long ts = System.currentTimeMillis();
        String hex = String.format("%04X", ts & 0xFFFFL);
        String rand = String.format("%04X", new Random().nextInt(0xFFFF + 1));
        return "DON-" + hex + "-" + rand;
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        var person = personRepo.findByUid(userDetails.getUsername());
        return person != null ? person.getId() : null;
    }

    private String getUserName(UserDetails userDetails) {
        if (userDetails == null) return null;
        var person = personRepo.findByUid(userDetails.getUsername());
        return person != null ? person.getName() : userDetails.getUsername();
    }

    /** Convert a Donation entity to a response map */
    private Map<String, Object> toDict(Donation d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("food_name", d.getFoodName());
        m.put("category", d.getCategory());
        m.put("food_type", d.getFoodType());
        m.put("quantity", d.getQuantity());
        m.put("unit", d.getUnit());
        m.put("serving_count", d.getServingCount());
        m.put("weight_lbs", d.getWeightLbs());
        m.put("description", d.getDescription());
        m.put("expiry_date", d.getExpiryDate() != null ? d.getExpiryDate().toString() : null);
        m.put("storage", d.getStorage());
        m.put("allergens", splitTags(d.getAllergens()));
        m.put("allergen_info", d.getAllergenInfo());
        m.put("dietary_tags", splitTags(d.getDietaryTags()));
        m.put("temperature_at_pickup", d.getTemperatureAtPickup());
        m.put("storage_method", d.getStorageMethod());
        m.put("donor_name", d.getDonorName());
        m.put("donor_email", d.getDonorEmail());
        m.put("donor_phone", d.getDonorPhone());
        m.put("donor_zip", d.getDonorZip());
        m.put("special_instructions", d.getSpecialInstructions());
        m.put("pickup_location", d.getPickupLocation());
        m.put("zip_code", d.getZipCode());
        m.put("pickup_window_start", d.getPickupWindowStart() != null ? d.getPickupWindowStart().toString() : null);
        m.put("pickup_window_end", d.getPickupWindowEnd() != null ? d.getPickupWindowEnd().toString() : null);
        m.put("status", d.getStatus());
        m.put("created_at", d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
        m.put("claimed_by", d.getClaimedBy());
        m.put("claimed_at", d.getClaimedAt() != null ? d.getClaimedAt().toString() : null);
        m.put("delivered_by", d.getDeliveredBy());
        m.put("delivered_at", d.getDeliveredAt() != null ? d.getDeliveredAt().toString() : null);
        m.put("is_archived", d.isArchived());
        m.put("scan_count", d.getScanCount());
        m.put("user_id", d.getUserId());
        m.put("donor_id", d.getDonorId());
        m.put("receiver_id", d.getReceiverId());
        return m;
    }

    private List<String> splitTags(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.asList(csv.split(","));
    }

    private String joinTags(List<?> tags) {
        if (tags == null) return null;
        return tags.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    // Auto-expire posted donations that are past their expiry date
    private void autoExpire() {
        List<Donation> expired = donationRepo.findExpired(LocalDate.now());
        for (Donation d : expired) {
            d.setStatus("expired");
        }
        if (!expired.isEmpty()) {
            donationRepo.saveAll(expired);
        }
    }

    // ── 1. POST /api/donations ──

    @PostMapping("/donations")
    public ResponseEntity<Map<String, Object>> createDonation(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Required fields
        String[] required = {"food_name", "category", "quantity", "unit",
                "expiry_date", "storage", "donor_name", "donor_email", "donor_zip"};
        for (String field : required) {
            Object val = body.get(field);
            if (val == null || val.toString().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing required field: " + field));
            }
        }

        String category = body.get("category").toString();
        if (!ALLOWED_CATEGORIES.contains(category)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid category: " + category + ". Allowed: " + ALLOWED_CATEGORIES));
        }

        String unit = body.get("unit").toString();
        if (!ALLOWED_UNITS.contains(unit)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid unit: " + unit + ". Allowed: " + ALLOWED_UNITS));
        }

        String storage = body.get("storage").toString();
        if (!ALLOWED_STORAGE.contains(storage)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid storage: " + storage + ". Allowed: " + ALLOWED_STORAGE));
        }

        // Parse expiry date
        LocalDate expiryDate;
        try {
            expiryDate = LocalDate.parse(body.get("expiry_date").toString());
            if (expiryDate.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Expiry date cannot be in the past"));
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format. Use YYYY-MM-DD"));
        }

        // Parse quantity
        int quantity;
        try {
            quantity = Integer.parseInt(body.get("quantity").toString());
            if (quantity < 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "Quantity must be at least 1"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Quantity must be a positive integer"));
        }

        // Optional food_type validation
        String foodType = body.containsKey("food_type") ? body.get("food_type").toString() : null;
        if (foodType != null && !foodType.isBlank() && !ALLOWED_FOOD_TYPES.contains(foodType)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid food_type: " + foodType + ". Allowed: " + ALLOWED_FOOD_TYPES));
        }

        // Optional storage_method validation
        String storageMethod = body.containsKey("storage_method") ? body.get("storage_method").toString() : null;
        if (storageMethod != null && !storageMethod.isBlank() && !ALLOWED_STORAGE_METHODS.contains(storageMethod)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid storage_method: " + storageMethod + ". Allowed: " + ALLOWED_STORAGE_METHODS));
        }

        // Allergens
        @SuppressWarnings("unchecked")
        List<String> allergens = body.containsKey("allergens") ? (List<String>) body.get("allergens") : Collections.emptyList();
        for (String a : allergens) {
            if (!ALLOWED_ALLERGENS.contains(a)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid allergen: " + a + ". Allowed: " + ALLOWED_ALLERGENS));
            }
        }

        // Dietary tags
        @SuppressWarnings("unchecked")
        List<String> dietaryTags = body.containsKey("dietary_tags") ? (List<String>) body.get("dietary_tags") : Collections.emptyList();
        for (String t : dietaryTags) {
            if (!ALLOWED_DIETARY.contains(t)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid dietary tag: " + t + ". Allowed: " + ALLOWED_DIETARY));
            }
        }

        // Build entity
        Donation donation = new Donation();
        donation.setId(generateDonationId());
        donation.setFoodName(body.get("food_name").toString());
        donation.setCategory(category);
        donation.setFoodType(foodType);
        donation.setQuantity(quantity);
        donation.setUnit(unit);
        donation.setServingCount(body.containsKey("serving_count") && body.get("serving_count") != null
                ? Integer.parseInt(body.get("serving_count").toString()) : null);
        donation.setWeightLbs(body.containsKey("weight_lbs") && body.get("weight_lbs") != null
                ? Double.parseDouble(body.get("weight_lbs").toString()) : null);
        donation.setDescription(body.getOrDefault("description", "").toString());
        donation.setExpiryDate(expiryDate);
        donation.setStorage(storage);
        donation.setAllergens(joinTags(allergens));
        donation.setAllergenInfo(body.containsKey("allergen_info") ? (String) body.get("allergen_info") : null);
        donation.setDietaryTags(joinTags(dietaryTags));
        donation.setTemperatureAtPickup(body.containsKey("temperature_at_pickup") && body.get("temperature_at_pickup") != null
                ? Double.parseDouble(body.get("temperature_at_pickup").toString()) : null);
        donation.setStorageMethod(storageMethod);
        donation.setDonorName(body.get("donor_name").toString());
        donation.setDonorEmail(body.get("donor_email").toString());
        donation.setDonorPhone(body.getOrDefault("donor_phone", "").toString());
        donation.setDonorZip(body.get("donor_zip").toString());
        donation.setSpecialInstructions(body.getOrDefault("special_instructions", "").toString());
        donation.setPickupLocation(body.containsKey("pickup_location") ? (String) body.get("pickup_location") : null);
        donation.setZipCode(body.containsKey("zip_code") ? (String) body.get("zip_code") : null);
        donation.setStatus("posted");
        donation.setUserId(resolveUserId(userDetails));
        donation.setScanCount(0);
        donation.setArchived(false);

        // Parse pickup window
        if (body.containsKey("pickup_window_start") && body.get("pickup_window_start") != null) {
            try {
                donation.setPickupWindowStart(LocalDateTime.parse(body.get("pickup_window_start").toString()));
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid pickup_window_start format. Use ISO-8601 (e.g. 2025-06-01T09:00:00)"));
            }
        }
        if (body.containsKey("pickup_window_end") && body.get("pickup_window_end") != null) {
            try {
                donation.setPickupWindowEnd(LocalDateTime.parse(body.get("pickup_window_end").toString()));
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid pickup_window_end format. Use ISO-8601 (e.g. 2025-06-01T17:00:00)"));
            }
        }
        if (donation.getPickupWindowStart() != null && donation.getPickupWindowEnd() != null
                && !donation.getPickupWindowEnd().isAfter(donation.getPickupWindowStart())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "pickup_window_end must be after pickup_window_start"));
        }

        donationRepo.save(donation);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", donation.getId());
        response.put("message", "Donation created successfully");
        response.put("status", "posted");
        response.put("donation", toDict(donation));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── 2. GET /api/donations ──

    @GetMapping("/donations")
    public ResponseEntity<Map<String, Object>> listDonations(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String zip_code,
            @RequestParam(required = false) String food_type,
            @RequestParam(defaultValue = "false") String mine,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page,
            @AuthenticationPrincipal UserDetails userDetails) {

        autoExpire();

        List<Donation> all;
        boolean mineOnly = "true".equalsIgnoreCase(mine);

        if (mineOnly) {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Authentication required for mine=true"));
            }
            Long userId = resolveUserId(userDetails);
            all = "all".equals(status)
                    ? donationRepo.findByArchivedFalseAndUserId(userId)
                    : donationRepo.findByArchivedFalseAndStatusAndUserId(status, userId);
        } else if ("all".equals(status)) {
            all = zip_code != null ? donationRepo.findByArchivedFalseAndDonorZip(zip_code)
                    : donationRepo.findByArchivedFalse();
        } else {
            all = zip_code != null ? donationRepo.findByArchivedFalseAndStatusAndDonorZip(status, zip_code)
                    : donationRepo.findByArchivedFalseAndStatus(status);
        }

        // Filter by category/food_type (food_type param maps to category in this system)
        if (food_type != null && !food_type.isBlank()) {
            all = all.stream().filter(d -> food_type.equals(d.getCategory())).collect(Collectors.toList());
        }

        // Sort by created_at descending
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        int total = all.size();
        int fromIdx = Math.min((page - 1) * per_page, total);
        int toIdx = Math.min(fromIdx + per_page, total);
        List<Donation> page_donations = all.subList(fromIdx, toIdx);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("donations", page_donations.stream().map(this::toDict).collect(Collectors.toList()));
        response.put("total", total);
        response.put("page", page);
        response.put("per_page", per_page);
        response.put("pages", (total + per_page - 1) / per_page);
        return ResponseEntity.ok(response);
    }

    // ── 3. GET /api/donations/stats ──

    @GetMapping("/donations/stats")
    public ResponseEntity<Map<String, Object>> getDonationStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        autoExpire();

        long total = donationRepo.countByArchivedFalse();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("posted",     donationRepo.countByArchivedFalseAndStatus("posted"));
        stats.put("claimed",    donationRepo.countByArchivedFalseAndStatus("claimed"));
        stats.put("in_transit", donationRepo.countByArchivedFalseAndStatus("in_transit"));
        stats.put("delivered",  donationRepo.countByArchivedFalseAndStatus("delivered"));
        stats.put("confirmed",  donationRepo.countByArchivedFalseAndStatus("confirmed"));
        stats.put("expired",    donationRepo.countByArchivedFalseAndStatus("expired"));
        stats.put("cancelled",  donationRepo.countByArchivedFalseAndStatus("cancelled"));
        return ResponseEntity.ok(stats);
    }

    // ── 4. POST /api/donations/scan ──

    @PostMapping("/donations/scan")
    public ResponseEntity<Map<String, Object>> scanDonation(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !body.containsKey("scan_data")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing scan_data"));
        }

        String scanData = body.get("scan_data").toString().strip();
        Optional<Donation> opt = donationRepo.findById(scanData);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }

        Donation d = opt.get();
        d.setScanCount((d.getScanCount() == null ? 0 : d.getScanCount()) + 1);

        List<Map<String, String>> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();
        if (d.getExpiryDate() != null && d.getExpiryDate().isBefore(today)) {
            warnings.add(Map.of("type", "expired",
                    "message", "This donation expired on " + d.getExpiryDate()));
            if ("posted".equals(d.getStatus())) {
                d.setStatus("expired");
            }
        } else if (d.getExpiryDate() != null) {
            long days = ChronoUnit.DAYS.between(today, d.getExpiryDate());
            if (days <= 3) {
                warnings.add(Map.of("type", "expiring_soon",
                        "message", "Expires in " + days + " day(s)"));
            }
        }
        if (d.isArchived()) {
            warnings.add(Map.of("type", "archived", "message", "This donation has been archived"));
        }

        donationRepo.save(d);

        Map<String, Object> result = toDict(d);
        result.put("warnings", warnings);
        result.put("scan_count", d.getScanCount());
        return ResponseEntity.ok(result);
    }

    // ── 5. GET /api/donations/{id} ──

    @GetMapping("/donations/{id}")
    public ResponseEntity<Map<String, Object>> getDonation(@PathVariable String id) {
        Optional<Donation> opt = donationRepo.findById(id);
        if (opt.isEmpty() || opt.get().isArchived()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }
        Donation d = opt.get();
        d.setScanCount((d.getScanCount() == null ? 0 : d.getScanCount()) + 1);
        donationRepo.save(d);
        return ResponseEntity.ok(toDict(d));
    }

    // ── 6. DELETE /api/donations/{id} ──

    @DeleteMapping("/donations/{id}")
    public ResponseEntity<Map<String, Object>> deleteDonation(@PathVariable String id) {
        Optional<Donation> opt = donationRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }
        Donation d = opt.get();
        if (Arrays.asList("in_transit", "delivered").contains(d.getStatus())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Cannot archive a donation with status: " + d.getStatus()));
        }
        d.setArchived(true);
        donationRepo.save(d);
        return ResponseEntity.ok(Map.of("message", "Donation archived successfully"));
    }

    // ── 7. PATCH /api/donations/{id}/status ──

    @PatchMapping("/donations/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Donation> opt = donationRepo.findById(id);
        if (opt.isEmpty() || opt.get().isArchived()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }

        if (!body.containsKey("status")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing required field: status"));
        }

        Donation d = opt.get();
        String newStatus = body.get("status").toString();
        List<String> allowed = VALID_TRANSITIONS.getOrDefault(d.getStatus(), Collections.emptyList());
        if (!allowed.contains(newStatus)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Cannot transition from '" + d.getStatus() + "' to '" + newStatus + "'. Allowed: " + allowed));
        }

        String changedBy = getUserName(userDetails);
        String notes = body.containsKey("notes") ? body.get("notes").toString() : null;

        d.setStatus(newStatus);
        if ("claimed".equals(newStatus)) {
            d.setClaimedBy(changedBy);
            d.setClaimedAt(LocalDateTime.now());
        } else if ("delivered".equals(newStatus)) {
            d.setDeliveredBy(changedBy);
            d.setDeliveredAt(LocalDateTime.now());
        }
        donationRepo.save(d);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Status updated to " + newStatus);
        response.put("donation_id", id);
        response.put("status", newStatus);
        if (notes != null) response.put("notes", notes);
        return ResponseEntity.ok(response);
    }

    // ── 8. POST /api/donation/{id}/accept (legacy) ──

    @PostMapping("/donation/{id}/accept")
    public ResponseEntity<Map<String, Object>> legacyAccept(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Donation> opt = donationRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }
        Donation d = opt.get();
        if (d.isArchived()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation has been archived"));
        }

        // Auto-expire check
        if ("posted".equals(d.getStatus()) && d.getExpiryDate() != null && d.getExpiryDate().isBefore(LocalDate.now())) {
            d.setStatus("expired");
            donationRepo.save(d);
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot accept an expired donation"));
        }

        if (Arrays.asList("claimed", "in_transit", "delivered", "confirmed").contains(d.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Donation already " + d.getStatus()));
        }
        if ("expired".equals(d.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot accept an expired donation"));
        }
        if ("cancelled".equals(d.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot accept a cancelled donation"));
        }

        String acceptedBy = "";
        if (body != null && body.containsKey("accepted_by")) {
            acceptedBy = body.get("accepted_by").toString();
        }
        if (acceptedBy.isBlank()) {
            String name = getUserName(userDetails);
            acceptedBy = name != null ? name : "";
        }

        d.setStatus("claimed");
        d.setClaimedBy(acceptedBy);
        d.setClaimedAt(LocalDateTime.now());
        d.setAcceptedBy(acceptedBy);
        d.setAcceptedAt(d.getClaimedAt());
        donationRepo.save(d);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Donation accepted");
        response.put("donation_id", id);
        response.put("status", "claimed");
        response.put("accepted_by", acceptedBy);
        return ResponseEntity.ok(response);
    }

    // ── 9. POST /api/donation/{id}/deliver (legacy) ──

    @PostMapping("/donation/{id}/deliver")
    public ResponseEntity<Map<String, Object>> legacyDeliver(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<Donation> opt = donationRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation not found"));
        }
        Donation d = opt.get();
        if (d.isArchived()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Donation has been archived"));
        }

        if ("delivered".equals(d.getStatus()) || "confirmed".equals(d.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Donation already " + d.getStatus()));
        }
        if ("expired".equals(d.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot deliver an expired donation"));
        }
        if ("cancelled".equals(d.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot deliver a cancelled donation"));
        }

        String deliveredBy = "";
        if (body != null && body.containsKey("delivered_by")) {
            deliveredBy = body.get("delivered_by").toString();
        }
        if (deliveredBy.isBlank()) {
            String name = getUserName(userDetails);
            deliveredBy = name != null ? name : "";
        }

        d.setStatus("delivered");
        d.setDeliveredBy(deliveredBy);
        d.setDeliveredAt(LocalDateTime.now());
        donationRepo.save(d);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Donation marked as delivered");
        response.put("donation_id", id);
        response.put("status", "delivered");
        response.put("delivered_at", d.getDeliveredAt().toString());
        return ResponseEntity.ok(response);
    }
}
