package com.example.onlinestore.service;

import com.example.onlinestore.model.Product;
import com.example.onlinestore.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    /**
     * Create a ProductService backed by the given ProductRepository.
     *
     * @param repo repository used to access and persist Product entities
     */
    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieve all products from the repository.
     *
     * @return a list of all Product entities; an empty list if no products exist
     */
    public List<Product> listAll() {
        return repo.findAll();
    }

    /**
     * Retrieve the product with the given id or throw if it does not exist.
     *
     * @param id the identifier of the product to retrieve
     * @return the Product with the specified id
     * @throws java.util.NoSuchElementException if no product with the given id is found
     */
    public Product getOrThrow(Long id) {
        // Intentionally using get() on Optional to surface a runtime exception
        // when a caller asks for a non-existing id (common code-review concern).
        return repo.findById(id).get();
    }

    /**
     * Persists the given Product and returns the saved entity.
     *
     * @param p the Product to persist; the repository may assign or modify its identifier and other persistable fields
     * @return the saved Product instance as returned by the repository
     */
    public Product create(Product p) {
        // no validation performed here — another review target (missing checks)
        return repo.save(p);
    }
}