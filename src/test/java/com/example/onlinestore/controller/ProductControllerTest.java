package com.example.onlinestore.controller;

import com.example.onlinestore.model.Product;
import com.example.onlinestore.repository.ProductRepository;
import com.example.onlinestore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product(1L, "Test Product 1", 19.99);
        testProduct2 = new Product(2L, "Test Product 2", 29.99);
    }

    // ========== list() tests ==========

    @Test
    @DisplayName("list should return all products from service")
    void list_ShouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(testProduct1, testProduct2);
        when(productService.listAll()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productController.list();

        // Assert
        assertNotNull(actualProducts);
        assertEquals(2, actualProducts.size());
        assertEquals(expectedProducts, actualProducts);
        verify(productService, times(1)).listAll();
        verifyNoInteractions(productRepository); // Should use service, not repo directly
    }

    @Test
    @DisplayName("list should return empty list when no products exist")
    void list_ShouldReturnEmptyList_WhenNoProducts() {
        // Arrange
        when(productService.listAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> actualProducts = productController.list();

        // Assert
        assertNotNull(actualProducts);
        assertTrue(actualProducts.isEmpty());
        verify(productService, times(1)).listAll();
    }

    @Test
    @DisplayName("list should return single product when only one exists")
    void list_ShouldReturnSingleProduct() {
        // Arrange
        List<Product> singleProduct = Collections.singletonList(testProduct1);
        when(productService.listAll()).thenReturn(singleProduct);

        // Act
        List<Product> actualProducts = productController.list();

        // Assert
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals(testProduct1, actualProducts.get(0));
        verify(productService, times(1)).listAll();
    }

    @Test
    @DisplayName("list should return large list of products")
    void list_ShouldReturnLargeList() {
        // Arrange
        List<Product> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(new Product((long) i, "Product " + i, i * 1.0));
        }
        when(productService.listAll()).thenReturn(largeList);

        // Act
        List<Product> actualProducts = productController.list();

        // Assert
        assertNotNull(actualProducts);
        assertEquals(1000, actualProducts.size());
        verify(productService, times(1)).listAll();
    }

    @Test
    @DisplayName("list should propagate service exceptions")
    void list_ShouldPropagateException_WhenServiceThrows() {
        // Arrange
        when(productService.listAll()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productController.list());
        verify(productService, times(1)).listAll();
    }

    @Test
    @DisplayName("list should handle products with null fields")
    void list_ShouldHandleProductsWithNullFields() {
        // Arrange
        Product nullNameProduct = new Product(1L, null, 19.99);
        when(productService.listAll()).thenReturn(Collections.singletonList(nullNameProduct));

        // Act
        List<Product> actualProducts = productController.list();

        // Assert
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertNull(actualProducts.get(0).getName());
        verify(productService, times(1)).listAll();
    }

    // ========== get() tests ==========

    @Test
    @DisplayName("get should return 200 OK with product when found")
    void get_ShouldReturn200_WhenProductFound() {
        // Arrange
        Long productId = 1L;
        when(productService.getOrThrow(productId)).thenReturn(testProduct1);

        // Act
        ResponseEntity<Product> response = productController.get(productId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProduct1.getId(), response.getBody().getId());
        assertEquals(testProduct1.getName(), response.getBody().getName());
        assertEquals(testProduct1.getPrice(), response.getBody().getPrice());
        verify(productService, times(1)).getOrThrow(productId);
    }

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

    @Test
    @DisplayName("get should return 404 for null id causing RuntimeException")
    void get_ShouldReturn404_ForNullId() {
        // Arrange
        when(productService.getOrThrow(null)).thenThrow(new NoSuchElementException());

        // Act
        ResponseEntity<Product> response = productController.get(null);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getOrThrow(null);
    }

    @Test
    @DisplayName("get should return 404 for zero id when not found")
    void get_ShouldReturn404_ForZeroId() {
        // Arrange
        Long zeroId = 0L;
        when(productService.getOrThrow(zeroId)).thenThrow(new NoSuchElementException());

        // Act
        ResponseEntity<Product> response = productController.get(zeroId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getOrThrow(zeroId);
    }

    @Test
    @DisplayName("get should return 404 for negative id when not found")
    void get_ShouldReturn404_ForNegativeId() {
        // Arrange
        Long negativeId = -1L;
        when(productService.getOrThrow(negativeId)).thenThrow(new NoSuchElementException());

        // Act
        ResponseEntity<Product> response = productController.get(negativeId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getOrThrow(negativeId);
    }

    @Test
    @DisplayName("get should catch any RuntimeException and return 404")
    void get_ShouldReturn404_ForAnyRuntimeException() {
        // Arrange
        Long productId = 1L;
        when(productService.getOrThrow(productId)).thenThrow(new IllegalStateException("Invalid state"));

        // Act
        ResponseEntity<Product> response = productController.get(productId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(productService, times(1)).getOrThrow(productId);
    }

    @Test
    @DisplayName("get should catch IllegalArgumentException and return 404")
    void get_ShouldReturn404_ForIllegalArgumentException() {
        // Arrange
        Long productId = 1L;
        when(productService.getOrThrow(productId)).thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act
        ResponseEntity<Product> response = productController.get(productId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getOrThrow(productId);
    }

    @Test
    @DisplayName("get should catch NullPointerException and return 404")
    void get_ShouldReturn404_ForNullPointerException() {
        // Arrange
        Long productId = 1L;
        when(productService.getOrThrow(productId)).thenThrow(new NullPointerException());

        // Act
        ResponseEntity<Product> response = productController.get(productId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productService, times(1)).getOrThrow(productId);
    }

    @Test
    @DisplayName("get should return product with null name")
    void get_ShouldReturnProduct_WithNullName() {
        // Arrange
        Product nullNameProduct = new Product(1L, null, 19.99);
        when(productService.getOrThrow(1L)).thenReturn(nullNameProduct);

        // Act
        ResponseEntity<Product> response = productController.get(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getName());
        verify(productService, times(1)).getOrThrow(1L);
    }

    @Test
    @DisplayName("get should return product with zero price")
    void get_ShouldReturnProduct_WithZeroPrice() {
        // Arrange
        Product zeroPrice = new Product(1L, "Free Item", 0.0);
        when(productService.getOrThrow(1L)).thenReturn(zeroPrice);

        // Act
        ResponseEntity<Product> response = productController.get(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0.0, response.getBody().getPrice());
        verify(productService, times(1)).getOrThrow(1L);
    }

    @Test
    @DisplayName("get should return product with negative price")
    void get_ShouldReturnProduct_WithNegativePrice() {
        // Arrange
        Product negativePrice = new Product(1L, "Discounted", -10.0);
        when(productService.getOrThrow(1L)).thenReturn(negativePrice);

        // Act
        ResponseEntity<Product> response = productController.get(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(-10.0, response.getBody().getPrice());
        verify(productService, times(1)).getOrThrow(1L);
    }

    @Test
    @DisplayName("get should handle very large id")
    void get_ShouldHandle_VeryLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        Product product = new Product(largeId, "Large ID", 19.99);
        when(productService.getOrThrow(largeId)).thenReturn(product);

        // Act
        ResponseEntity<Product> response = productController.get(largeId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(largeId, response.getBody().getId());
        verify(productService, times(1)).getOrThrow(largeId);
    }

    // ========== create() tests ==========

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

    @Test
    @DisplayName("create should handle product with null id")
    void create_ShouldHandleNullId() {
        // Arrange
        Product newProduct = new Product(null, "Product", 19.99);
        Product savedProduct = new Product(1L, "Product", 19.99);
        when(productService.create(newProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(newProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
        verify(productService, times(1)).create(newProduct);
    }

    @Test
    @DisplayName("create should accept product with existing id")
    void create_ShouldAcceptProductWithExistingId() {
        // Arrange
        Product productWithId = new Product(10L, "Existing ID", 49.99);
        when(productService.create(productWithId)).thenReturn(productWithId);

        // Act
        ResponseEntity<Product> response = productController.create(productWithId);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
        verify(productService, times(1)).create(productWithId);
    }

    @Test
    @DisplayName("create should accept product with null name - no validation")
    void create_ShouldAcceptNullName() {
        // Arrange - Controller does not validate
        Product invalidProduct = new Product(null, null, 19.99);
        Product savedProduct = new Product(1L, null, 19.99);
        when(productService.create(invalidProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(invalidProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody().getName());
        verify(productService, times(1)).create(invalidProduct);
    }

    @Test
    @DisplayName("create should accept product with empty name")
    void create_ShouldAcceptEmptyName() {
        // Arrange
        Product emptyNameProduct = new Product(null, "", 19.99);
        Product savedProduct = new Product(1L, "", 19.99);
        when(productService.create(emptyNameProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(emptyNameProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("", response.getBody().getName());
        verify(productService, times(1)).create(emptyNameProduct);
    }

    @Test
    @DisplayName("create should accept product with whitespace-only name")
    void create_ShouldAcceptWhitespaceName() {
        // Arrange
        Product whitespaceProduct = new Product(null, "   ", 19.99);
        Product savedProduct = new Product(1L, "   ", 19.99);
        when(productService.create(whitespaceProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(whitespaceProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("   ", response.getBody().getName());
        verify(productService, times(1)).create(whitespaceProduct);
    }

    @Test
    @DisplayName("create should accept product with zero price")
    void create_ShouldAcceptZeroPrice() {
        // Arrange
        Product zeroPrice = new Product(null, "Free", 0.0);
        Product savedProduct = new Product(1L, "Free", 0.0);
        when(productService.create(zeroPrice)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(zeroPrice);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(0.0, response.getBody().getPrice());
        verify(productService, times(1)).create(zeroPrice);
    }

    @Test
    @DisplayName("create should accept product with negative price")
    void create_ShouldAcceptNegativePrice() {
        // Arrange
        Product negativePrice = new Product(null, "Negative", -10.0);
        Product savedProduct = new Product(1L, "Negative", -10.0);
        when(productService.create(negativePrice)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(negativePrice);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(-10.0, response.getBody().getPrice());
        verify(productService, times(1)).create(negativePrice);
    }

    @Test
    @DisplayName("create should accept product with very large price")
    void create_ShouldAcceptLargePrice() {
        // Arrange
        Product largePrice = new Product(null, "Expensive", Double.MAX_VALUE);
        Product savedProduct = new Product(1L, "Expensive", Double.MAX_VALUE);
        when(productService.create(largePrice)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(largePrice);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Double.MAX_VALUE, response.getBody().getPrice());
        verify(productService, times(1)).create(largePrice);
    }

    @Test
    @DisplayName("create should accept product with NaN price")
    void create_ShouldAcceptNaNPrice() {
        // Arrange
        Product nanPrice = new Product(null, "NaN Price", Double.NaN);
        Product savedProduct = new Product(1L, "NaN Price", Double.NaN);
        when(productService.create(nanPrice)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(nanPrice);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(Double.isNaN(response.getBody().getPrice()));
        verify(productService, times(1)).create(nanPrice);
    }

    @Test
    @DisplayName("create should accept product with infinity price")
    void create_ShouldAcceptInfinityPrice() {
        // Arrange
        Product infPrice = new Product(null, "Infinity", Double.POSITIVE_INFINITY);
        Product savedProduct = new Product(1L, "Infinity", Double.POSITIVE_INFINITY);
        when(productService.create(infPrice)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(infPrice);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Double.POSITIVE_INFINITY, response.getBody().getPrice());
        verify(productService, times(1)).create(infPrice);
    }

    @Test
    @DisplayName("create should handle very long product name")
    void create_ShouldHandleLongName() {
        // Arrange
        String longName = "A".repeat(10000);
        Product longNameProduct = new Product(null, longName, 19.99);
        Product savedProduct = new Product(1L, longName, 19.99);
        when(productService.create(longNameProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(longNameProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(longName, response.getBody().getName());
        verify(productService, times(1)).create(longNameProduct);
    }

    @Test
    @DisplayName("create should handle special characters in name")
    void create_ShouldHandleSpecialCharacters() {
        // Arrange
        String specialName = "Product@#$%^&*()_+-=[]{}|;':\",./<>?";
        Product specialProduct = new Product(null, specialName, 19.99);
        Product savedProduct = new Product(1L, specialName, 19.99);
        when(productService.create(specialProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(specialProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(specialName, response.getBody().getName());
        verify(productService, times(1)).create(specialProduct);
    }

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

    @Test
    @DisplayName("create should propagate service exceptions")
    void create_ShouldPropagateException_WhenServiceThrows() {
        // Arrange
        Product newProduct = new Product(null, "Test", 19.99);
        when(productService.create(newProduct)).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productController.create(newProduct));
        verify(productService, times(1)).create(newProduct);
    }

    @Test
    @DisplayName("create should generate correct location URI for single digit id")
    void create_ShouldGenerateCorrectLocation_ForSingleDigitId() {
        // Arrange
        Product newProduct = new Product(null, "Test", 19.99);
        Product savedProduct = new Product(5L, "Test", 19.99);
        when(productService.create(newProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(newProduct);

        // Assert
        URI location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertEquals("/api/products/5", location.toString());
    }

    @Test
    @DisplayName("create should generate correct location URI for large id")
    void create_ShouldGenerateCorrectLocation_ForLargeId() {
        // Arrange
        Product newProduct = new Product(null, "Test", 19.99);
        Product savedProduct = new Product(999999L, "Test", 19.99);
        when(productService.create(newProduct)).thenReturn(savedProduct);

        // Act
        ResponseEntity<Product> response = productController.create(newProduct);

        // Assert
        URI location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertEquals("/api/products/999999", location.toString());
    }

    @Test
    @DisplayName("create should handle null product gracefully")
    void create_ShouldHandleNullProduct() {
        // Arrange
        when(productService.create(null)).thenThrow(new NullPointerException());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> productController.create(null));
        verify(productService, times(1)).create(null);
    }

    // ========== Constructor tests ==========

    @Test
    @DisplayName("Controller should be instantiated with repository and service")
    void controller_ShouldBeInstantiated() {
        // Assert
        assertNotNull(productController);
    }

    // ========== Integration-style tests (verifying behavior across methods) ==========

    @Test
    @DisplayName("Multiple list calls should use service each time")
    void multipleLists_ShouldCallServiceEachTime() {
        // Arrange
        when(productService.listAll()).thenReturn(Arrays.asList(testProduct1, testProduct2));

        // Act
        productController.list();
        productController.list();
        productController.list();

        // Assert
        verify(productService, times(3)).listAll();
    }

    @Test
    @DisplayName("Multiple get calls with same id should call service each time")
    void multipleGets_ShouldCallServiceEachTime() {
        // Arrange
        Long productId = 1L;
        when(productService.getOrThrow(productId)).thenReturn(testProduct1);

        // Act
        productController.get(productId);
        productController.get(productId);

        // Assert
        verify(productService, times(2)).getOrThrow(productId);
    }

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

    @Test
    @DisplayName("Controller should delegate to service, not repository directly for get")
    void get_ShouldNotUseRepositoryDirectly() {
        // Arrange
        when(productService.getOrThrow(1L)).thenReturn(testProduct1);

        // Act
        productController.get(1L);

        // Assert
        verify(productService, times(1)).getOrThrow(1L);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("Controller should delegate to service, not repository directly for create")
    void create_ShouldNotUseRepositoryDirectly() {
        // Arrange
        Product newProduct = new Product(null, "New", 19.99);
        Product savedProduct = new Product(1L, "New", 19.99);
        when(productService.create(newProduct)).thenReturn(savedProduct);

        // Act
        productController.create(newProduct);

        // Assert
        verify(productService, times(1)).create(newProduct);
        verifyNoInteractions(productRepository);
    }
}