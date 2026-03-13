package com.open.spring.mvc.userStocks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStocksRepository extends JpaRepository<userStocksTable, Long> {
    // You can add custom methods if needed, but JpaRepository already provides save(), findById(), etc.
    userStocksTable findByEmail(String email);  // Fetch user by email
}