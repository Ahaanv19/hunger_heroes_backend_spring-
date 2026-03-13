package com.open.spring.mvc.cryptoMining;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class GPU {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double hashRate;  // MH/s
    private int powerConsumption;  // Watts
    private int temp;  // Celsius
    private double price;  // USD
    private String category;
    private boolean available = true;

    private int usageHours = 0; // The duration at which the GPU has been used

    // Formula for efficiency: hashRate / powerConsumption
    public double getEfficiency() {
        return hashRate / powerConsumption;
    }

    public double getHashRate() {
        return this.hashRate; 
    }

    // Simulate usage and periodic degradation
    public void runForHours(int hours) {
        this.usageHours += hours;

        // Degrade performance every 100 hours
        if (this.usageHours >= 100) {
            degradePerformance();
            this.usageHours = 0; // Reset counter after degradation
        }
    }

    // Performance degradation with time, price, and temp factors
    public void degradePerformance() {
        double efficiency = getEfficiency();
        
        // Higher efficiency GPUs degrade more
        double degradationFactor = 1 + (efficiency / 10);

        // Price resistance: Expensive GPUs degrade slower
        double priceResistance = 1 + (price / 5000); 

        // Temperature impact: Higher temps cause faster degradation
        double tempImpact = 1 + ((temp - 60) / 100.0); // If temp > 60°C, degrades faster

        // Adjust degradation factor
        double adjustedDegradation = (degradationFactor * tempImpact) / priceResistance;

        // Apply degradation
        this.hashRate /= adjustedDegradation;
        this.powerConsumption *= 1.02; // Increase power usage slightly over time
    }
}
