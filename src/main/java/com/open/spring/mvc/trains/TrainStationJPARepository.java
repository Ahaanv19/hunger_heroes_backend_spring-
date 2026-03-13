package com.open.spring.mvc.trains;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainStationJPARepository extends JpaRepository<TrainStation,Long>{
    TrainStation getById(Long id);
    boolean existsById(Long id);
    List<TrainStation> getAllByPosition(Float position);
    List<TrainStation> findAll();
}

