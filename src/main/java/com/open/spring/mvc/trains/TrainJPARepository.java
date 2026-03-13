package com.open.spring.mvc.trains;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainJPARepository extends JpaRepository<Train,Long>{
    Train getById(Long id);
    boolean existsById(Long id);
    List<Train> getAllByCompanyId(Long companyId);
    boolean existsByCompanyId(Long companyId);
    List<Train> getAllByPosition(Float position);
}

