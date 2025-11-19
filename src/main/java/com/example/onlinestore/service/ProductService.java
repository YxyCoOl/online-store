package com.example.onlinestore.service;

import com.example.onlinestore.model.Product;
import com.example.onlinestore.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> listAll() {
        return repo.findAll();
    }

    public Product getOrThrow(Long id) {
        // Intentionally using get() on Optional to surface a runtime exception
        // when a caller asks for a non-existing id (common code-review concern).
        return repo.findById(id).get();
    }

    public Product create(Product p) {
        // no validation performed here — another review target (missing checks)
        return repo.save(p);
    }
}
