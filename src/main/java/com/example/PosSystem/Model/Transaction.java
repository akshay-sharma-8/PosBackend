package com.example.PosSystem.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW: Links this transaction record exclusively to the user
    private String ownerUsername;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String products;
    private String discountApplied;
    private String paymentMethod;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "transaction_timestamp")
    private LocalDateTime transactionTimestamp;

    private String customerName;
    private String customerPhone;
    private String transactionReference;

    // The quantity Android sends
    private int soldQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalWholesaleCost;
}