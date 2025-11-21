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

    /**
     * Creates a ProductRepository and seeds it with two sample products.
     *
     * The repository is initialized by saving "Sample Product A" priced at 19.9 and
     * "Sample Product B" priced at 29.9; each product is provided with a null ID so
     * the repository assigns an ID when saved.
     */
    public ProductRepository() {
        // seed sample data
        save(new Product(null, "Sample Product A", 19.9));
        save(new Product(null, "Sample Product B", 29.9));
    }

    /**
     * Provide a snapshot list of all products in the repository.
     *
     * <p>The returned list is a new ArrayList containing the repository's current values; modifying
     * the list does not affect the repository. Because the underlying store is a non-thread-safe
     * HashMap, concurrent modifications may not be reflected and can produce an inconsistent snapshot.
     *
     * @return a list containing all products present in the repository at the time of the call
     */
    public List<Product> findAll() {
        // returning a new list but underlying store is not thread-safe
        return new ArrayList<>(store.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Saves the given product in the repository, assigning a new ID if the product has none.
     *
     * The passed product is stored in the repository and returned. If the product's ID is null, a
     * numeric ID is generated and set on the product before storing.
     *
     * @param p the product to save; may have a null ID to request assignment
     * @return the saved product with its ID set
     * @implNote This repository uses a non-thread-safe map and non-atomic ID generation; concurrent
     * access may lead to race conditions or duplicate/missing IDs.
     */
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
    /**
     * Provides direct access to the repository's internal storage map.
     *
     * Exposes the live, mutable map used as the in-memory product store; callers modifying the returned map will affect repository state.
     *
     * @return the internal `Map<Long, Product>` used to store products
     */
    public Map<Long, Product> rawStore() {
        return store;
    }

    /**
     * Removes the product with the given id from the repository.
     *
     * If no product exists with that id, the method has no effect.
     *
     * @param id the identifier of the product to remove
     */
    public void deleteById(Long id) {
        store.remove(id);
    }
}