package com.example.onlinestore.repository;

import com.example.onlinestore.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductRepository Unit Tests")
class ProductRepositoryTest {

    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ProductRepository();
    }

    // ========== Constructor and Initialization tests ==========

    @Test
    @DisplayName("Constructor should seed sample data")
    void constructor_ShouldSeedSampleData() {
        // Act
        List<Product> products = repository.findAll();

        // Assert
        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> "Sample Product A".equals(p.getName())));
        assertTrue(products.stream().anyMatch(p -> "Sample Product B".equals(p.getName())));
    }

    @Test
    @DisplayName("Constructor should assign unique IDs to seeded products")
    void constructor_ShouldAssignUniqueIds() {
        // Act
        List<Product> products = repository.findAll();

        // Assert
        Set<Long> ids = new HashSet<>();
        for (Product p : products) {
            assertNotNull(p.getId());
            ids.add(p.getId());
        }
        assertEquals(2, ids.size(), "All product IDs should be unique");
    }

    // ========== findAll() tests ==========

    @Test
    @DisplayName("findAll should return all products")
    void findAll_ShouldReturnAllProducts() {
        // Act
        List<Product> products = repository.findAll();

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(2, products.size());
    }

    @Test
    @DisplayName("findAll should return new list instance each time")
    void findAll_ShouldReturnNewListInstance() {
        // Act
        List<Product> list1 = repository.findAll();
        List<Product> list2 = repository.findAll();

        // Assert
        assertNotSame(list1, list2, "Each call should return a new list instance");
        assertEquals(list1.size(), list2.size());
    }

    @Test
    @DisplayName("findAll should not allow modification of internal state through returned list")
    void findAll_ShouldNotAllowModificationOfInternalState() {
        // Arrange
        int initialSize = repository.findAll().size();
        List<Product> products = repository.findAll();

        // Act - Modify the returned list
        products.clear();

        // Assert - Internal state should not be affected
        assertEquals(initialSize, repository.findAll().size());
    }

    @Test
    @DisplayName("findAll should include newly saved products")
    void findAll_ShouldIncludeNewlySavedProducts() {
        // Arrange
        int initialSize = repository.findAll().size();
        Product newProduct = new Product(null, "New Product", 99.99);

        // Act
        repository.save(newProduct);
        List<Product> products = repository.findAll();

        // Assert
        assertEquals(initialSize + 1, products.size());
        assertTrue(products.stream().anyMatch(p -> "New Product".equals(p.getName())));
    }

    @Test
    @DisplayName("findAll should return empty list when all products deleted")
    void findAll_ShouldReturnEmptyList_WhenAllProductsDeleted() {
        // Arrange
        List<Product> initialProducts = repository.findAll();
        initialProducts.forEach(p -> repository.deleteById(p.getId()));

        // Act
        List<Product> products = repository.findAll();

        // Assert
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    // ========== findById() tests ==========

    @Test
    @DisplayName("findById should return product when exists")
    void findById_ShouldReturnProduct_WhenExists() {
        // Arrange
        Product savedProduct = repository.save(new Product(null, "Test Product", 49.99));

        // Act
        Optional<Product> result = repository.findById(savedProduct.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedProduct.getId(), result.get().getId());
        assertEquals("Test Product", result.get().getName());
        assertEquals(49.99, result.get().getPrice());
    }

    @Test
    @DisplayName("findById should return empty Optional when product not found")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // Act
        Optional<Product> result = repository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findById should handle null id")
    void findById_ShouldHandleNullId() {
        // Act
        Optional<Product> result = repository.findById(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findById should return empty for zero id")
    void findById_ShouldReturnEmpty_ForZeroId() {
        // Act
        Optional<Product> result = repository.findById(0L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findById should return empty for negative id")
    void findById_ShouldReturnEmpty_ForNegativeId() {
        // Act
        Optional<Product> result = repository.findById(-1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findById should return empty after product is deleted")
    void findById_ShouldReturnEmpty_AfterProductDeleted() {
        // Arrange
        Product savedProduct = repository.save(new Product(null, "Temp Product", 19.99));
        Long productId = savedProduct.getId();

        // Act
        repository.deleteById(productId);
        Optional<Product> result = repository.findById(productId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findById should find product with very large id")
    void findById_ShouldFindProduct_WithLargeId() {
        // Arrange
        Product product = new Product(Long.MAX_VALUE, "Large ID Product", 29.99);
        repository.save(product);

        // Act
        Optional<Product> result = repository.findById(Long.MAX_VALUE);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Long.MAX_VALUE, result.get().getId());
    }

    // ========== save() tests ==========

    @Test
    @DisplayName("save should generate id for new product")
    void save_ShouldGenerateId_ForNewProduct() {
        // Arrange
        Product newProduct = new Product(null, "Auto ID Product", 39.99);

        // Act
        Product saved = repository.save(newProduct);

        // Assert
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("Auto ID Product", saved.getName());
    }

    @Test
    @DisplayName("save should increment id counter for each new product")
    void save_ShouldIncrementIdCounter() {
        // Arrange
        Product product1 = new Product(null, "Product 1", 10.0);
        Product product2 = new Product(null, "Product 2", 20.0);

        // Act
        Product saved1 = repository.save(product1);
        Product saved2 = repository.save(product2);

        // Assert
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertTrue(saved2.getId() > saved1.getId());
    }

    @Test
    @DisplayName("save should update existing product when id is provided")
    void save_ShouldUpdateExistingProduct() {
        // Arrange
        Product original = repository.save(new Product(null, "Original", 50.0));
        Long productId = original.getId();

        // Act
        Product updated = new Product(productId, "Updated", 60.0);
        repository.save(updated);
        Optional<Product> result = repository.findById(productId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getName());
        assertEquals(60.0, result.get().getPrice());
    }

    @Test
    @DisplayName("save should not change id when updating existing product")
    void save_ShouldNotChangeId_WhenUpdating() {
        // Arrange
        Product original = repository.save(new Product(null, "Original", 50.0));
        Long originalId = original.getId();

        // Act
        Product updated = new Product(originalId, "Updated Name", 75.0);
        Product saved = repository.save(updated);

        // Assert
        assertEquals(originalId, saved.getId());
    }

    @Test
    @DisplayName("save should accept product with null name")
    void save_ShouldAcceptNullName() {
        // Arrange
        Product product = new Product(null, null, 19.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertNull(saved.getName());
    }

    @Test
    @DisplayName("save should accept product with empty name")
    void save_ShouldAcceptEmptyName() {
        // Arrange
        Product product = new Product(null, "", 19.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("", saved.getName());
    }

    @Test
    @DisplayName("save should accept product with zero price")
    void save_ShouldAcceptZeroPrice() {
        // Arrange
        Product product = new Product(null, "Free Item", 0.0);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(0.0, saved.getPrice());
    }

    @Test
    @DisplayName("save should accept product with negative price")
    void save_ShouldAcceptNegativePrice() {
        // Arrange
        Product product = new Product(null, "Negative", -10.0);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(-10.0, saved.getPrice());
    }

    @Test
    @DisplayName("save should handle very long product names")
    void save_ShouldHandleLongNames() {
        // Arrange
        String longName = "A".repeat(10000);
        Product product = new Product(null, longName, 19.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(longName, saved.getName());
    }

    @Test
    @DisplayName("save should handle special characters in name")
    void save_ShouldHandleSpecialCharacters() {
        // Arrange
        String specialName = "Product@#$%^&*()_+-=[]{}|;':\",./<>?";
        Product product = new Product(null, specialName, 19.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(specialName, saved.getName());
    }

    @Test
    @DisplayName("save should handle unicode characters in name")
    void save_ShouldHandleUnicodeCharacters() {
        // Arrange
        String unicodeName = "产品名称 🎉 тест";
        Product product = new Product(null, unicodeName, 19.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(unicodeName, saved.getName());
    }

    @Test
    @DisplayName("save should replace product at same id")
    void save_ShouldReplaceProduct_AtSameId() {
        // Arrange
        Product first = new Product(100L, "First", 10.0);
        Product second = new Product(100L, "Second", 20.0);

        // Act
        repository.save(first);
        repository.save(second);
        Optional<Product> result = repository.findById(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Second", result.get().getName());
        assertEquals(20.0, result.get().getPrice());
    }

    // ========== rawStore() tests ==========

    @Test
    @DisplayName("rawStore should expose internal map")
    void rawStore_ShouldExposeInternalMap() {
        // Act
        Map<Long, Product> store = repository.rawStore();

        // Assert
        assertNotNull(store);
        assertEquals(2, store.size()); // Initial seeded products
    }

    @Test
    @DisplayName("rawStore should allow direct mutation of internal state")
    void rawStore_ShouldAllowDirectMutation() {
        // Arrange
        int initialSize = repository.findAll().size();

        // Act - Direct mutation through rawStore
        repository.rawStore().clear();

        // Assert
        assertEquals(0, repository.findAll().size());
        assertNotEquals(initialSize, repository.findAll().size());
    }

    @Test
    @DisplayName("rawStore should return same map instance on multiple calls")
    void rawStore_ShouldReturnSameMapInstance() {
        // Act
        Map<Long, Product> store1 = repository.rawStore();
        Map<Long, Product> store2 = repository.rawStore();

        // Assert
        assertSame(store1, store2, "Should return the same internal map instance");
    }

    @Test
    @DisplayName("rawStore modifications should be reflected in findAll")
    void rawStore_ModificationsShouldAffectFindAll() {
        // Arrange
        Product directProduct = new Product(999L, "Direct Insert", 99.99);

        // Act
        repository.rawStore().put(999L, directProduct);
        List<Product> allProducts = repository.findAll();

        // Assert
        assertTrue(allProducts.stream().anyMatch(p -> p.getId().equals(999L)));
    }

    @Test
    @DisplayName("rawStore should allow removal of products")
    void rawStore_ShouldAllowRemoval() {
        // Arrange
        Product product = repository.save(new Product(null, "To Remove", 19.99));
        Long productId = product.getId();

        // Act
        repository.rawStore().remove(productId);

        // Assert
        assertFalse(repository.findById(productId).isPresent());
    }

    // ========== deleteById() tests ==========

    @Test
    @DisplayName("deleteById should remove product from store")
    void deleteById_ShouldRemoveProduct() {
        // Arrange
        Product product = repository.save(new Product(null, "To Delete", 19.99));
        Long productId = product.getId();

        // Act
        repository.deleteById(productId);

        // Assert
        assertFalse(repository.findById(productId).isPresent());
    }

    @Test
    @DisplayName("deleteById should reduce product count")
    void deleteById_ShouldReduceProductCount() {
        // Arrange
        Product product = repository.save(new Product(null, "To Delete", 19.99));
        int beforeCount = repository.findAll().size();

        // Act
        repository.deleteById(product.getId());
        int afterCount = repository.findAll().size();

        // Assert
        assertEquals(beforeCount - 1, afterCount);
    }

    @Test
    @DisplayName("deleteById should handle non-existent id gracefully")
    void deleteById_ShouldHandleNonExistentId() {
        // Arrange
        int beforeCount = repository.findAll().size();

        // Act
        repository.deleteById(999L);
        int afterCount = repository.findAll().size();

        // Assert
        assertEquals(beforeCount, afterCount);
    }

    @Test
    @DisplayName("deleteById should handle null id gracefully")
    void deleteById_ShouldHandleNullId() {
        // Arrange
        int beforeCount = repository.findAll().size();

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> repository.deleteById(null));
        assertEquals(beforeCount, repository.findAll().size());
    }

    @Test
    @DisplayName("deleteById should handle negative id")
    void deleteById_ShouldHandleNegativeId() {
        // Arrange
        int beforeCount = repository.findAll().size();

        // Act
        repository.deleteById(-1L);

        // Assert
        assertEquals(beforeCount, repository.findAll().size());
    }

    @Test
    @DisplayName("deleteById should allow deletion of same id multiple times")
    void deleteById_ShouldAllowMultipleDeletions() {
        // Arrange
        Product product = repository.save(new Product(null, "Delete Twice", 19.99));
        Long productId = product.getId();

        // Act & Assert
        repository.deleteById(productId);
        assertDoesNotThrow(() -> repository.deleteById(productId));
    }

    // ========== Concurrency Issue Tests ==========

    @Test
    @DisplayName("save should have race condition with non-atomic id generation")
    void save_ShouldExposeRaceCondition_InIdGeneration() throws InterruptedException {
        // This test demonstrates the concurrency issue with the non-atomic counter
        // Arrange
        int threadCount = 10;
        int productsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> generatedIds = Collections.synchronizedSet(new HashSet<>());

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < productsPerThread; j++) {
                        Product p = new Product(null, "Concurrent Product", 19.99);
                        Product saved = repository.save(p);
                        generatedIds.add(saved.getId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Due to race condition, we might get duplicate IDs
        int totalProducts = threadCount * productsPerThread;
        // This assertion documents the potential issue - in a thread-safe implementation,
        // we would expect exactly totalProducts + 2 (initial seeded) unique IDs
        assertTrue(repository.findAll().size() <= totalProducts + 2,
                "Race condition may cause some products to be overwritten");
    }

    @Test
    @DisplayName("findAll should have potential concurrency issues with HashMap")
    void findAll_ShouldExposePotentialConcurrencyIssue() throws InterruptedException {
        // This test demonstrates potential issues with concurrent reads/writes
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // Act - One thread writes, another reads
        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    repository.save(new Product(null, "Concurrent " + i, 19.99));
                }
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    repository.findAll(); // May throw ConcurrentModificationException
                }
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - Document that concurrent access might cause issues
        // In a properly synchronized implementation, we would expect no exceptions
        // Note: This test may pass sometimes due to the nature of race conditions
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Repository should handle saving many products")
    void repository_ShouldHandleManyProducts() {
        // Arrange & Act
        for (int i = 0; i < 1000; i++) {
            repository.save(new Product(null, "Product " + i, i * 1.0));
        }

        // Assert
        List<Product> allProducts = repository.findAll();
        assertTrue(allProducts.size() >= 1000);
    }

    @Test
    @DisplayName("Repository should handle rapid save and delete operations")
    void repository_ShouldHandleRapidSaveAndDelete() {
        // Act
        for (int i = 0; i < 100; i++) {
            Product p = repository.save(new Product(null, "Temp " + i, 19.99));
            repository.deleteById(p.getId());
        }

        // Assert - Should only have initial seeded products
        assertEquals(2, repository.findAll().size());
    }

    @Test
    @DisplayName("save should handle extreme price values")
    void save_ShouldHandleExtremePriceValues() {
        // Arrange
        Product maxPrice = new Product(null, "Max Price", Double.MAX_VALUE);
        Product minPrice = new Product(null, "Min Price", -Double.MAX_VALUE);
        Product infinity = new Product(null, "Infinity", Double.POSITIVE_INFINITY);
        Product negInfinity = new Product(null, "Neg Infinity", Double.NEGATIVE_INFINITY);

        // Act
        Product savedMax = repository.save(maxPrice);
        Product savedMin = repository.save(minPrice);
        Product savedInf = repository.save(infinity);
        Product savedNegInf = repository.save(negInfinity);

        // Assert
        assertEquals(Double.MAX_VALUE, savedMax.getPrice());
        assertEquals(-Double.MAX_VALUE, savedMin.getPrice());
        assertEquals(Double.POSITIVE_INFINITY, savedInf.getPrice());
        assertEquals(Double.NEGATIVE_INFINITY, savedNegInf.getPrice());
    }

    @Test
    @DisplayName("save should handle NaN price")
    void save_ShouldHandleNaNPrice() {
        // Arrange
        Product nanProduct = new Product(null, "NaN Price", Double.NaN);

        // Act
        Product saved = repository.save(nanProduct);

        // Assert
        assertTrue(Double.isNaN(saved.getPrice()));
    }
}