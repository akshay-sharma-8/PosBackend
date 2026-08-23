package com.example.PosSystem.controller;

import com.example.PosSystem.Model.*;
import com.example.PosSystem.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        BigDecimal totalWholesale = BigDecimal.ZERO;
        boolean parsedAsJson = false;

        // Try to parse products as JSON array first
        // e.g. [{"name":"Apple","quantity":2},{"name":"Milk","quantity":1}]
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cartItems = mapper.readTree(transaction.getProducts());

            if (cartItems != null && cartItems.isArray()) {
                parsedAsJson = true;

                for (JsonNode item : cartItems) {
                    String name = item.has("name") ? item.get("name").asText().trim() : "";
                    int quantity = item.has("quantity") ? item.get("quantity").asInt() : 1;

                    if (name.isEmpty() || quantity <= 0) continue;

                    List<Product> found = productRepository.findByNameAndOwnerUsername(name, transaction.getOwnerUsername());
                    if (found.isEmpty()) {
                        // Product not in catalog — skip but don't crash
                        continue;
                    }

                    Product product = found.get(0);

                    // Check stock
                    if (product.getQuantity() < quantity) {
                        return ResponseEntity.badRequest()
                                .body("Insufficient stock for: " + name
                                        + " (available: " + product.getQuantity()
                                        + ", requested: " + quantity + ")");
                    }

                    // Deduct stock
                    product.setQuantity(product.getQuantity() - quantity);
                    productRepository.save(product);

                    // Accumulate wholesale cost
                    BigDecimal wholesale = product.getWholesalePrice() != null
                            ? product.getWholesalePrice()
                            : BigDecimal.ZERO;
                    totalWholesale = totalWholesale.add(wholesale.multiply(new BigDecimal(quantity)));
                }
            }
        } catch (Exception ignored) {
            // products field is not JSON — will fall through to plain string logic below
        }

        // FALLBACK: plain string product name (e.g. "Apple")
        if (!parsedAsJson) {
            String productName = transaction.getProducts() != null ? transaction.getProducts().trim() : "";
            if (!productName.isEmpty()) {
                List<Product> found = productRepository.findByNameAndOwnerUsername(productName, transaction.getOwnerUsername());
                if (!found.isEmpty()) {
                    Product product = found.get(0);
                    if (product.getQuantity() < 1) {
                        return ResponseEntity.badRequest()
                                .body("Insufficient stock for: " + productName);
                    }
                    product.setQuantity(product.getQuantity() - 1);
                    productRepository.save(product);
                    totalWholesale = product.getWholesalePrice() != null
                            ? product.getWholesalePrice()
                            : BigDecimal.ZERO;
                }
            }
        }

        transaction.setTotalWholesaleCost(totalWholesale);
        transaction.setTransactionTimestamp(LocalDateTime.now());
        transaction.setDate(LocalDate.now());
        return ResponseEntity.ok(transactionRepository.save(transaction));
    }

    // GET all transactions for a user
    @GetMapping("/user/{username}")
    public List<Transaction> getUserTransactions(@PathVariable String username) {
        return transactionRepository.findByOwnerUsername(username);
    }

    // DELETE single transaction by id
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // DELETE all transactions for a user
    @DeleteMapping("/user/{username}")
    @Transactional
    public ResponseEntity<Void> deleteAllUserTransactions(@PathVariable String username) {
        transactionRepository.deleteByOwnerUsername(username);
        return ResponseEntity.ok().build();
    }

    // PROFIT endpoints
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