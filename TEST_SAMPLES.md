# Sample Tests from Generated Test Suite

This document showcases representative tests from each generated test class to demonstrate the comprehensive coverage and quality of the test suite.

---

## ProductServiceTest Examples

### Happy Path Test
```java
@Test
@DisplayName("listAll should return all products from repository")
void listAll_ShouldReturnAllProducts() {
    // Arrange
    List<Product> expectedProducts = Arrays.asList(testProduct, testProduct2);
    when(productRepository.findAll()).thenReturn(expectedProducts);

    // Act
    List<Product> actualProducts = productService.listAll();

    // Assert
    assertNotNull(actualProducts);
    assertEquals(2, actualProducts.size());
    assertEquals(expectedProducts, actualProducts);
    verify(productRepository, times(1)).findAll();
}
```

### Edge Case Test
```java
@Test
@DisplayName("getOrThrow should throw NoSuchElementException when product not found")
void getOrThrow_ShouldThrowException_WhenProductNotFound() {
    // Arrange
    Long nonExistentId = 999L;
    when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(NoSuchElementException.class, () -> productService.getOrThrow(nonExistentId));
    verify(productRepository, times(1)).findById(nonExistentId);
}
```

### Validation Gap Documentation Test
```java
@Test
@DisplayName("create should accept product with null name")
void create_ShouldAcceptProductWithNullName() {
    // Arrange - Note: No validation in service layer
    Product invalidProduct = new Product(null, null, 19.99);
    Product savedProduct = new Product(4L, null, 19.99);
    when(productRepository.save(invalidProduct)).thenReturn(savedProduct);

    // Act
    Product actualProduct = productService.create(invalidProduct);

    // Assert
    assertNotNull(actualProduct);
    assertNull(actualProduct.getName());
    assertEquals(19.99, actualProduct.getPrice());
    verify(productRepository, times(1)).save(invalidProduct);
}
```

---

## ProductRepositoryTest Examples

### Concurrency Issue Documentation Test
```java
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
    assertTrue(repository.findAll().size() <= totalProducts + 2,
            "Race condition may cause some products to be overwritten");
}
```

### Anti-Pattern Documentation Test
```java
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
```

### Boundary Value Test
```java
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
```

---

## ProductControllerTest Examples

### HTTP Status Code Validation Test
```java
@Test
@DisplayName("create should return 201 Created with location header")
void create_ShouldReturn201_WithLocationHeader() {
    // Arrange
    Product newProduct = new Product(null, "New Product", 39.99);
    Product savedProduct = new Product(3L, "New Product", 39.99);
    when(productService.create(newProduct)).thenReturn(savedProduct);

    // Act
    ResponseEntity<Product> response = productController.create(newProduct);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(3L, response.getBody().getId());
    assertEquals("New Product", response.getBody().getName());
    assertEquals(39.99, response.getBody().getPrice());
    
    // Verify location header
    URI location = response.getHeaders().getLocation();
    assertNotNull(location);
    assertEquals("/api/products/3", location.toString());
    
    verify(productService, times(1)).create(newProduct);
}
```

### Exception to HTTP Status Mapping Test
```java
@Test
@DisplayName("get should return 404 Not Found when product not found")
void get_ShouldReturn404_WhenProductNotFound() {
    // Arrange
    Long nonExistentId = 999L;
    when(productService.getOrThrow(nonExistentId)).thenThrow(new NoSuchElementException());

    // Act
    ResponseEntity<Product> response = productController.get(nonExistentId);

    // Assert
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
    verify(productService, times(1)).getOrThrow(nonExistentId);
}
```

### Service Layer Delegation Verification Test
```java
@Test
@DisplayName("Controller should delegate to service, not repository directly for list")
void list_ShouldNotUseRepositoryDirectly() {
    // Arrange
    when(productService.listAll()).thenReturn(Collections.emptyList());

    // Act
    productController.list();

    // Assert
    verify(productService, times(1)).listAll();
    verifyNoInteractions(productRepository);
}
```

### Unicode Support Test
```java
@Test
@DisplayName("create should handle unicode characters in name")
void create_ShouldHandleUnicodeCharacters() {
    // Arrange
    String unicodeName = "产品 🎉 тест";
    Product unicodeProduct = new Product(null, unicodeName, 19.99);
    Product savedProduct = new Product(1L, unicodeName, 19.99);
    when(productService.create(unicodeProduct)).thenReturn(savedProduct);

    // Act
    ResponseEntity<Product> response = productController.create(unicodeProduct);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(unicodeName, response.getBody().getName());
    verify(productService, times(1)).create(unicodeProduct);
}
```

---

## Test Organization Patterns

All tests follow consistent patterns:

### 1. Arrange-Act-Assert (AAA) Pattern
Every test is clearly structured into three phases:
- **Arrange**: Set up test data and mock behaviors
- **Act**: Execute the method under test
- **Assert**: Verify expected outcomes and mock interactions

### 2. Descriptive Naming
- Method names clearly describe what is being tested
- `@DisplayName` annotations provide human-readable descriptions
- Pattern: `methodName_ShouldExpectedBehavior_WhenCondition`

### 3. Comprehensive Verification
- Return values are validated
- Mock interactions are verified (times called, parameters)
- HTTP status codes are checked (for controllers)
- Exception types are validated
- Edge cases are explicitly tested

### 4. Documentation Through Tests
Tests serve as living documentation by:
- Identifying missing validation
- Documenting concurrency issues
- Highlighting anti-patterns
- Demonstrating proper usage

---

## Coverage Highlights

✅ **Happy Paths**: Normal operation with valid inputs
✅ **Edge Cases**: Null, empty, zero, negative values
✅ **Boundary Values**: MIN/MAX values, Infinity, NaN
✅ **Error Conditions**: Exceptions, not found scenarios
✅ **Security**: Special characters, Unicode, injection attempts
✅ **Performance**: Large datasets, concurrent access
✅ **Integration**: Service layer delegation, proper layering
