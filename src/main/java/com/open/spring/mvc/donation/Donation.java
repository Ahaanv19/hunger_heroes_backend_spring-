package com.open.spring.mvc.donation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a food donation in the Hunger Heroes system.
 * Mirrors the Flask donation model from hunger_heroes_backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hh_donation")
public class Donation {

    /** Human-readable donation ID, e.g. DON-abc1-xyz2 */
    @Id
    private String id;

    // ── Food details ──
    private String foodName;
    private String category;
    private String foodType;
    private Integer quantity;
    private String unit;
    private Integer servingCount;
    private Double weightLbs;

    @Column(length = 2000)
    private String description;

    private LocalDate expiryDate;
    private String storage;

    /** Comma-separated allergen list (e.g. "gluten,dairy") */
    @Column(columnDefinition = "TEXT")
    private String allergens;

    @Column(columnDefinition = "TEXT")
    private String allergenInfo;

    /** Comma-separated dietary tag list (e.g. "vegan,halal") */
    @Column(columnDefinition = "TEXT")
    private String dietaryTags;

    private Double temperatureAtPickup;
    private String storageMethod;

    // ── Donor details ──
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private String donorZip;

    @Column(length = 2000)
    private String specialInstructions;

    private String pickupLocation;
    private String zipCode;
    private LocalDateTime pickupWindowStart;
    private LocalDateTime pickupWindowEnd;

    // ── Status tracking ──
    /** One of: posted, claimed, in_transit, delivered, confirmed, expired, cancelled */
    private String status;

    private LocalDateTime createdAt;

    private String claimedBy;
    private LocalDateTime claimedAt;

    private String deliveredBy;
    private LocalDateTime deliveredAt;

    /** Legacy field: same as claimedBy */
    private String acceptedBy;
    private LocalDateTime acceptedAt;

    private boolean archived;
    private Integer scanCount;

    // ── Relationships to Person ──
    private Long userId;
    private Long donorId;
    private Long receiverId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "posted";
        }
        if (scanCount == null) {
            scanCount = 0;
        }
    }
}
