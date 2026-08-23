package com.example.PosSystem.controller;

import com.example.PosSystem.Model.Product;
import com.example.PosSystem.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET all products for a user
    // Android calls: GET /api/products/{username}  e.g. GET /api/products/akshay
    @GetMapping("/{username}")
    public List<Product> getUserProducts(@PathVariable String username) {
        return productRepository.findByOwnerUsername(username);
    }

    // CREATE a new product -> POST /api/products
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // UPDATE an existing product -> PUT /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        Product product = existing.get();
        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setWholesalePrice(updatedProduct.getWholesalePrice());
        product.setQuantity(updatedProduct.getQuantity());
        if (updatedProduct.getImageUrls() != null) {
            product.setImageUrls(updatedProduct.getImageUrls());
        }
        return ResponseEntity.ok(productRepository.save(product));
    }

    // DELETE a product by id -> DELETE /api/products/{id}
    // This works fine alongside GET /{username} because Spring matches
    // by BOTH the URL pattern AND the HTTP method (GET vs DELETE)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}