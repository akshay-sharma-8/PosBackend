package com.example.PosSystem.repository;

import com.example.PosSystem.Model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Finds all transactions for a specific user
    List<Transaction> findByOwnerUsername(String ownerUsername);

    // Deletes all transactions for a specific user
    void deleteByOwnerUsername(String ownerUsername);

    // Calculates profit ONLY for the requested user
    @Query("SELECT SUM(COALESCE(t.finalAmount, 0) - COALESCE(t.totalWholesaleCost, 0)) FROM Transaction t WHERE t.transactionTimestamp >= :startDate AND t.ownerUsername = :username")
    BigDecimal calculateProfitSince(@Param("startDate") LocalDateTime startDate, @Param("username") String username);
}