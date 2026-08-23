package com.example.PosSystem.repository;

import com.example.PosSystem.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Finds all products owned by a specific user
    List<Product> findByOwnerUsername(String ownerUsername);

    // Finds a specific product name owned by a specific user (for checkout)
    List<Product> findByNameAndOwnerUsername(String name, String ownerUsername);
}