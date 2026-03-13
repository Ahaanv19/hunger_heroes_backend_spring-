package com.open.spring.mvc.plant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    
    // Find plant by user uid (each user has one plant)
    Plant findByUid(String uid);
}