package com.example.PosSystem.controller;

import com.example.PosSystem.Model.*;
import com.example.PosSystem.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/transactions", "/api/transaction"})
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @PostMapping({"", "/", "/save", "/saveTransaction"})
    @Transactional
    public ResponseEntity<?> saveTransaction(@RequestBody Transaction transaction) {
        if (transaction == null) {
            return ResponseEntity.badRequest().body("Transaction details are required.");
        }

        BigDecimal totalWholesale = BigDecimal.ZERO;
        boolean parsedAsJson = false;

        // Try to parse products as JSON array first (if you ever use a cart system)
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cartItems = mapper.readTree(transaction.getProducts());

            if (cartItems != null && cartItems.isArray() && cartItems.size() > 0) {
                parsedAsJson = true;

                for (JsonNode item : cartItems) {
                    String name = item.has("name") ? item.get("name").asText().trim() : "";
                    int quantity = item.has("quantity") ? item.get("quantity").asInt() : 1;

                    if (name.isEmpty() || quantity <= 0) continue;

                    // Support case-insensitive name match if exact match fails
                    List<Product> found = productRepository.findByNameAndOwnerUsername(name, transaction.getOwnerUsername());
                    if (found.isEmpty()) {
                        // try to find by lowercasing manually in Java just in case
                        List<Product> allProducts = productRepository.findByOwnerUsername(transaction.getOwnerUsername());
                        found = allProducts.stream().filter(p -> p.getName().equalsIgnoreCase(name)).toList();
                    }

                    if (found.isEmpty()) continue; 

                    Product product = found.get(0);

                    // Check stock
                    if (product.getQuantity() < quantity) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for: " + name
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
        } catch (JsonProcessingException | IllegalArgumentException ignored) {
            // products field is not JSON — will fall through to plain string logic below
        }

        // FALLBACK: plain string product name (Current Android App Behavior)
        if (!parsedAsJson) {
            String productName = transaction.getProducts() != null ? transaction.getProducts().trim() : "";
            
            // Get quantity from Android app, default to 1 if not provided
            int qty = transaction.getSoldQuantity() > 0 ? transaction.getSoldQuantity() : 1;
            
            if (!productName.isEmpty()) {
                // Find exactly
                List<Product> found = productRepository.findByNameAndOwnerUsername(productName, transaction.getOwnerUsername());
                
                // If not found exactly, try case-insensitive
                if (found.isEmpty()) {
                    List<Product> allProducts = productRepository.findByOwnerUsername(transaction.getOwnerUsername());
                    found = allProducts.stream().filter(p -> p.getName().equalsIgnoreCase(productName)).toList();
                }
                
                if (!found.isEmpty()) {
                    Product product = found.get(0);
                    
                    if (product.getQuantity() < qty) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for: " + productName);
                    }
                    
                    // Deduct EXACT quantity
                    product.setQuantity(product.getQuantity() - qty);
                    productRepository.save(product);
                    
                    // Calculate total wholesale cost (Price * Quantity)
                    BigDecimal wholesale = product.getWholesalePrice() != null
                            ? product.getWholesalePrice()
                            : BigDecimal.ZERO;
                    totalWholesale = wholesale.multiply(new BigDecimal(qty));
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
    public BigDecimal getDailyProfit(@PathVariable String username) {
        BigDecimal profit = transactionRepository.calculateProfitSince(LocalDate.now().atStartOfDay(), username);
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @GetMapping("/profit/week/{username}")
    public BigDecimal getWeeklyProfit(@PathVariable String username) {
        BigDecimal profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusDays(7), username);
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @GetMapping("/profit/month/{username}")
    public BigDecimal getMonthlyProfit(@PathVariable String username) {
        BigDecimal profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusMonths(1), username);
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @GetMapping("/profit/year/{username}")
    public BigDecimal getYearlyProfit(@PathVariable String username) {
        BigDecimal profit = transactionRepository.calculateProfitSince(LocalDateTime.now().minusYears(1), username);
        return profit != null ? profit : BigDecimal.ZERO;
    }
}