package com.example.onlinestore.repository;

import com.example.onlinestore.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {
    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public ProductRepository() {
        // seed sample data
        save(new Product(null, "Sample Product A", 19.9));
        save(new Product(null, "Sample Product B", 29.9));
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Product save(Product p) {
        if (p.getId() == null) {
            p.setId(idGenerator.incrementAndGet());
        }
        store.put(p.getId(), p);
        return p;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
