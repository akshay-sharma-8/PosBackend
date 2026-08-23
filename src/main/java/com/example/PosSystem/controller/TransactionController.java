package com.example.PosSystem.controller;

import com.example.PosSystem.Model.*;
import com.example.PosSystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveTransaction(@RequestBody Transaction transaction) {
        List<Product> products = productRepository.findByNameAndOwnerUsername(transaction.getProducts(), transaction.getOwnerUsername());

        if (!products.isEmpty()) {
            Product product = products.get(0);
            transaction.setTotalWholesaleCost(product.getWholesalePrice() != null ? product.getWholesalePrice() : BigDecimal.ZERO);
            if (product.getQuantity() > 0) {
                product.setQuantity(product.getQuantity() - 1);
                productRepository.save(product);
            } else return ResponseEntity.badRequest().body("Insufficient inventory.");
        } else {
            transaction.setTotalWholesaleCost(BigDecimal.ZERO);
        }
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transaction.setDate(LocalDate.now());
        return ResponseEntity.ok(transactionRepository.save(transaction));
    }

    // THE MISSING ENDPOINTS: These handle the new /user/ram URLs
    @GetMapping("/user/{username}")
    public List<Transaction> getUserTransactions(@PathVariable String username) {
        return transactionRepository.findByOwnerUsername(username);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{username}")
    @Transactional
    public ResponseEntity<Void> deleteAllUserTransactions(@PathVariable String username) {
        transactionRepository.deleteByOwnerUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profit/day/{username}")
    public Double getDailyProfit(@PathVariable String username) {
        Double profit = transactionRepository.calculateProfitSince(LocalDate.now().atStartOfDay(), username);
        return profit != null ? profit : 0.0;
    }

    @GetMapping("/profit/week/{username}")
    public Double getWeeklyProfit(@PathVariable String username) {
        Double profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusDays(7), username);
        return profit != null ? profit : 0.0;
    }

    @GetMapping("/profit/month/{username}")
    public Double getMonthlyProfit(@PathVariable String username) {
        Double profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusMonths(1), username);
        return profit != null ? profit : 0.0;
    }

    @GetMapping("/profit/year/{username}")
    public Double getYearlyProfit(@PathVariable String username) {
        Double profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusYears(1), username);
        return profit != null ? profit : 0.0;
    }
}