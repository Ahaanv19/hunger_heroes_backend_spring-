package com.open.spring.mvc.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonationJpaRepository extends JpaRepository<Donation, String> {

    List<Donation> findByArchivedFalse();

    List<Donation> findByArchivedFalseAndStatus(String status);

    List<Donation> findByArchivedFalseAndDonorZip(String donorZip);

    List<Donation> findByArchivedFalseAndStatusAndDonorZip(String status, String donorZip);

    List<Donation> findByArchivedFalseAndUserId(Long userId);

    List<Donation> findByArchivedFalseAndStatusAndUserId(String status, Long userId);

    @Query("SELECT d FROM Donation d WHERE d.archived = false AND d.status = 'posted' AND d.expiryDate < :today")
    List<Donation> findExpired(@Param("today") LocalDate today);

    long countByArchivedFalseAndStatus(String status);

    long countByArchivedFalse();
}
