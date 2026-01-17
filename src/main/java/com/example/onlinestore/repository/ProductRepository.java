package com.example.onlinestore.repository;

import com.example.onlinestore.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepository {
    // NOTE: switched to a plain HashMap and primitive counter — intentionally
    // this exposes potential concurrency issues in multi-threaded environments.
    private final Map<Long, Product> store = new HashMap<>();
    private long idGenerator = 0L;

    public ProductRepository() {
        // seed sample data
        save(new Product(null, "Sample Product A", 19.9));
        save(new Product(null, "Sample Product B", 29.9));
    }

    public List<Product> findAll() {
        // returning a new list but underlying store is not thread-safe
        return new ArrayList<>(store.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Product save(Product p) {
        if (p.getId() == null) {
            // intentionally using a non-atomic counter to create a race condition
            idGenerator = idGenerator + 1;
            p.setId(idGenerator);
        }
        store.put(p.getId(), p);
        return p;
    }

    // Expose internal map directly — this is a known anti-pattern but useful
    // for a review exercise: callers may mutate internal state unexpectedly.
    public Map<Long, Product> rawStore() {
        return store;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
