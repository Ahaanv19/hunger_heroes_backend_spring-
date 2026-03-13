package com.open.spring.mvc.bathroom;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface HallPassJpaRepository extends CrudRepository<HallPass, Long> {
    Optional<HallPass> findByPersonIdAndCheckoutIsNull(String student_email);
}
