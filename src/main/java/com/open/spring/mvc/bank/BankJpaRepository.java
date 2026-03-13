package com.open.spring.mvc.bank;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BankJpaRepository extends JpaRepository<Bank, Long> {
    // Find bank by person_id
    Bank findByPersonId(Long personId);
    Bank findByUid(String uid);    
    List<Bank>  findByUidContainingIgnoreCase(String uid);
    // Find top 10 banks ordered by balance in descending order (for leaderboard)
    List<Bank> findTop10ByOrderByBalanceDesc();
    @Query("SELECT p FROM Bank p ORDER BY CAST(p.balance AS double) DESC LIMIT 5")
    List<Bank> findTop5ByOrderByBalanceDesc();
    
    // Alternative deletion methods for troubleshooting
    @Modifying
    @Transactional
    @Query("DELETE FROM Bank b")
    void deleteAllBanks();
    
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE bank RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateBankTable();
    
    // For debugging - check if there are any orphaned records
    @Query("SELECT COUNT(b) FROM Bank b WHERE b.person IS NULL")
    long countOrphanedBanks();
}