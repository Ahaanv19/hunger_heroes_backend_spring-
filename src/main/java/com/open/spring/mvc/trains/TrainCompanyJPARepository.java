package com.open.spring.mvc.trains;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainCompanyJPARepository extends JpaRepository<TrainCompany,Long>{
    TrainCompany getById(Long id);
    Optional<TrainCompany> findByCompanyName(String companyName);
    boolean existsById(Long id);
}
