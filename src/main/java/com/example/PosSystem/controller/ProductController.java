package com.example.PosSystem.controller;

import com.example.PosSystem.Model.Product;
import com.example.PosSystem.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // THIS IS THE MISSING ENDPOINT: It handles /api/products/ram
    @GetMapping("/{username}")
    public List<Product> getUserProducts(@PathVariable String username) {
        return productRepository.findByOwnerUsername(username);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}