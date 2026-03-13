package com.open.spring.mvc.cryptoMining;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Energy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String supplierName;
    private double EEM;

    public Energy() {}

    // EEM --> Energy Efficiency Metric 

    public Energy(String supplierName, double EEM) {
        this.supplierName = supplierName;
        this.EEM = EEM;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    
    
    public double getEEM() {
        return EEM;
    }

    public void setEEM(double EEM) {
        this.EEM = EEM;
    }
}
