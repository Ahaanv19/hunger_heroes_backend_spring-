package com.open.spring.mvc.hardAssets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HardAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String localFileUUID;

    @Column(nullable = false)
    private String ownerUID;

    public HardAsset() {
    }

    public HardAsset(String fileName, String localFileUUID, String ownerUID) {
        this.fileName = fileName;
        this.localFileUUID = localFileUUID;
        this.ownerUID = ownerUID;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalFileUUID() {
        return localFileUUID;
    }
}