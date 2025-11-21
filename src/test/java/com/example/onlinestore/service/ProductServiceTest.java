package com.example.onlinestore.service;

import com.example.onlinestore.model.Product;
import com.example.onlinestore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct = new Product(1L, "Test Product", 19.99);
        testProduct2 = new Product(2L, "Another Product", 29.99);
    }

    // ========== listAll() tests ==========

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

    @Test
    @DisplayName("listAll should return empty list when no products exist")
    void listAll_ShouldReturnEmptyList_WhenNoProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> actualProducts = productService.listAll();

        // Assert
        assertNotNull(actualProducts);
        assertTrue(actualProducts.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listAll should return single product when only one exists")
    void listAll_ShouldReturnSingleProduct() {
        // Arrange
        List<Product> singleProduct = Collections.singletonList(testProduct);
        when(productRepository.findAll()).thenReturn(singleProduct);

        // Act
        List<Product> actualProducts = productService.listAll();

        // Assert
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals(testProduct, actualProducts.get(0));
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listAll should propagate repository exceptions")
    void listAll_ShouldPropagateException_WhenRepositoryThrows() {
        // Arrange
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.listAll());
        verify(productRepository, times(1)).findAll();
    }

    // ========== getOrThrow() tests ==========

    @Test
    @DisplayName("getOrThrow should return product when found by id")
    void getOrThrow_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act
        Product actualProduct = productService.getOrThrow(productId);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(testProduct.getId(), actualProduct.getId());
        assertEquals(testProduct.getName(), actualProduct.getName());
        assertEquals(testProduct.getPrice(), actualProduct.getPrice());
        verify(productRepository, times(1)).findById(productId);
    }

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

    @Test
    @DisplayName("getOrThrow should handle null id gracefully")
    void getOrThrow_ShouldHandleNullId() {
        // Arrange
        when(productRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.getOrThrow(null));
        verify(productRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("getOrThrow should throw exception for zero id when not found")
    void getOrThrow_ShouldThrowException_ForZeroId() {
        // Arrange
        Long zeroId = 0L;
        when(productRepository.findById(zeroId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.getOrThrow(zeroId));
        verify(productRepository, times(1)).findById(zeroId);
    }

    @Test
    @DisplayName("getOrThrow should handle negative ids")
    void getOrThrow_ShouldHandleNegativeId() {
        // Arrange
        Long negativeId = -1L;
        when(productRepository.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.getOrThrow(negativeId));
        verify(productRepository, times(1)).findById(negativeId);
    }

    @Test
    @DisplayName("getOrThrow should propagate repository exceptions")
    void getOrThrow_ShouldPropagateException_WhenRepositoryThrows() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.getOrThrow(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    // ========== create() tests ==========

    @Test
    @DisplayName("create should save and return product with generated id")
    void create_ShouldSaveAndReturnProduct() {
        // Arrange
        Product newProduct = new Product(null, "New Product", 39.99);
        Product savedProduct = new Product(3L, "New Product", 39.99);
        when(productRepository.save(newProduct)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(newProduct);

        // Assert
        assertNotNull(actualProduct);
        assertNotNull(actualProduct.getId());
        assertEquals(3L, actualProduct.getId());
        assertEquals("New Product", actualProduct.getName());
        assertEquals(39.99, actualProduct.getPrice());
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("create should handle product with existing id")
    void create_ShouldHandleProductWithExistingId() {
        // Arrange
        Product existingProduct = new Product(1L, "Existing Product", 49.99);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        // Act
        Product actualProduct = productService.create(existingProduct);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(1L, actualProduct.getId());
        assertEquals("Existing Product", actualProduct.getName());
        verify(productRepository, times(1)).save(existingProduct);
    }

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

    @Test
    @DisplayName("create should accept product with empty name")
    void create_ShouldAcceptProductWithEmptyName() {
        // Arrange - Note: No validation in service layer
        Product invalidProduct = new Product(null, "", 19.99);
        Product savedProduct = new Product(5L, "", 19.99);
        when(productRepository.save(invalidProduct)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(invalidProduct);

        // Assert
        assertNotNull(actualProduct);
        assertEquals("", actualProduct.getName());
        verify(productRepository, times(1)).save(invalidProduct);
    }

    @Test
    @DisplayName("create should accept product with zero price")
    void create_ShouldAcceptProductWithZeroPrice() {
        // Arrange - Note: No validation in service layer
        Product zeroPrice = new Product(null, "Free Product", 0.0);
        Product savedProduct = new Product(6L, "Free Product", 0.0);
        when(productRepository.save(zeroPrice)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(zeroPrice);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(0.0, actualProduct.getPrice());
        verify(productRepository, times(1)).save(zeroPrice);
    }

    @Test
    @DisplayName("create should accept product with negative price")
    void create_ShouldAcceptProductWithNegativePrice() {
        // Arrange - Note: No validation in service layer
        Product negativePrice = new Product(null, "Discounted", -10.0);
        Product savedProduct = new Product(7L, "Discounted", -10.0);
        when(productRepository.save(negativePrice)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(negativePrice);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(-10.0, actualProduct.getPrice());
        verify(productRepository, times(1)).save(negativePrice);
    }

    @Test
    @DisplayName("create should accept product with very large price")
    void create_ShouldAcceptProductWithLargePrice() {
        // Arrange
        Product expensiveProduct = new Product(null, "Luxury Item", Double.MAX_VALUE);
        Product savedProduct = new Product(8L, "Luxury Item", Double.MAX_VALUE);
        when(productRepository.save(expensiveProduct)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(expensiveProduct);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(Double.MAX_VALUE, actualProduct.getPrice());
        verify(productRepository, times(1)).save(expensiveProduct);
    }

    @Test
    @DisplayName("create should handle null product gracefully")
    void create_ShouldHandleNullProduct() {
        // Arrange
        when(productRepository.save(null)).thenThrow(new NullPointerException());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> productService.create(null));
        verify(productRepository, times(1)).save(null);
    }

    @Test
    @DisplayName("create should propagate repository exceptions")
    void create_ShouldPropagateException_WhenRepositoryThrows() {
        // Arrange
        Product newProduct = new Product(null, "Test", 19.99);
        when(productRepository.save(newProduct)).thenThrow(new RuntimeException("Save failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.create(newProduct));
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("create should handle product with very long name")
    void create_ShouldHandleProductWithLongName() {
        // Arrange
        String longName = "A".repeat(10000);
        Product longNameProduct = new Product(null, longName, 19.99);
        Product savedProduct = new Product(9L, longName, 19.99);
        when(productRepository.save(longNameProduct)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(longNameProduct);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(longName, actualProduct.getName());
        verify(productRepository, times(1)).save(longNameProduct);
    }

    @Test
    @DisplayName("create should handle product with special characters in name")
    void create_ShouldHandleProductWithSpecialCharacters() {
        // Arrange
        String specialName = "Test@#$%^&*()_+-=[]{}|;':\",./<>?";
        Product specialProduct = new Product(null, specialName, 19.99);
        Product savedProduct = new Product(10L, specialName, 19.99);
        when(productRepository.save(specialProduct)).thenReturn(savedProduct);

        // Act
        Product actualProduct = productService.create(specialProduct);

        // Assert
        assertNotNull(actualProduct);
        assertEquals(specialName, actualProduct.getName());
        verify(productRepository, times(1)).save(specialProduct);
    }
}