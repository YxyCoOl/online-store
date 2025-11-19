package com.example.onlinestore.controller;

import com.example.onlinestore.model.Product;
import com.example.onlinestore.repository.ProductRepository;
import com.example.onlinestore.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository repo;
    private final ProductService service;

    public ProductController(ProductRepository repo, ProductService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public List<Product> list() {
        // Use the service layer for listing, but keep direct repo reference too.
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        // This will throw if id not found because service.getOrThrow uses Optional.get()
        try {
            Product p = service.getOrThrow(id);
            return ResponseEntity.ok(p);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        // Missing validation (e.g. @Valid). Also using service to create.
        Product saved = service.create(p);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }
}
