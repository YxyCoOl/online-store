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

    /**
     * Create a ProductController with the required repository and service dependencies.
     *
     * @param repo    repository used for product persistence operations
     * @param service service that provides product business operations used by the controller
     */
    public ProductController(ProductRepository repo, ProductService service) {
        this.repo = repo;
        this.service = service;
    }

    /**
     * Retrieve all products.
     *
     * @return a list containing all Product entities
     */
    @GetMapping
    public List<Product> list() {
        // Use the service layer for listing, but keep direct repo reference too.
        return service.listAll();
    }

    /**
     * Retrieve a product by its identifier.
     *
     * If a product with the given id exists, responds with HTTP 200 and the product in the body;
     * otherwise responds with HTTP 404 and an empty body.
     *
     * @param id the product identifier
     * @return a ResponseEntity containing the product and HTTP 200 if found, or HTTP 404 with no body otherwise
     */
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

    /**
     * Create a new product and return its persisted representation.
     *
     * @param p the product data to create
     * @return a ResponseEntity with HTTP 201 Created, the Location header set to "/api/products/{id}", and the created Product in the response body
     */
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        // Missing validation (e.g. @Valid). Also using service to create.
        Product saved = service.create(p);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }
}