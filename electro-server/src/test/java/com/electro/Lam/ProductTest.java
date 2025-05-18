package com.electro.Lam;

import com.electro.entity.product.Product;
import com.electro.repository.product.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductTest {

    @Mock
    private ProductRepository productRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register modules for JSON serialization
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }

    @Test
    public void testCreateProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("PROD001");
        product.setSlug("laptop-dell-xps-13");
        product.setShortDescription("High-performance laptop");
        product.setDescription("Detailed description of the Dell XPS 13");
        product.setStatus(1); // Active status
        System.out.println("Input: " + toJson(product));

        when(productRepository.save(product)).thenReturn(product);

        // Act
        Product savedProduct = productRepository.save(product);
        System.out.println("Output: " + toJson(savedProduct));

        // Assert
        assertNotNull(savedProduct, "Saved product should not be null");
        assertEquals("Laptop Dell XPS 13", savedProduct.getName(), "Product name should match");
        assertEquals("PROD001", savedProduct.getCode(), "Product code should match");
        assertEquals("laptop-dell-xps-13", savedProduct.getSlug(), "Product slug should match");
        assertEquals(1, savedProduct.getStatus(), "Product status should match");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void testUpdateProduct() {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Laptop Dell XPS 13");
        existingProduct.setCode("PROD001");
        existingProduct.setSlug("laptop-dell-xps-13");
        existingProduct.setShortDescription("High-performance laptop");
        existingProduct.setDescription("Detailed description of the Dell XPS 13");
        existingProduct.setStatus(1); // Active status
        System.out.println("Input (Existing Product): " + toJson(existingProduct));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // Act
        Optional<Product> productOptional = productRepository.findById(1L);
        assertTrue(productOptional.isPresent(), "Product should exist");
        Product productToUpdate = productOptional.get();
        productToUpdate.setName("Laptop Dell XPS 15");
        productToUpdate.setCode("PROD002");
        productToUpdate.setSlug("laptop-dell-xps-15");
        productToUpdate.setShortDescription("Updated high-performance laptop");
        productToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Product): " + toJson(productToUpdate));

        when(productRepository.save(productToUpdate)).thenReturn(productToUpdate);
        Product updatedProduct = productRepository.save(productToUpdate);
        System.out.println("Output: " + toJson(updatedProduct));

        // Assert
        assertNotNull(updatedProduct, "Updated product should not be null");
        assertEquals("Laptop Dell XPS 15", updatedProduct.getName(), "Updated name should match");
        assertEquals("PROD002", updatedProduct.getCode(), "Updated code should match");
        assertEquals("laptop-dell-xps-15", updatedProduct.getSlug(), "Updated slug should match");
        assertEquals(0, updatedProduct.getStatus(), "Updated status should match");
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(productToUpdate);
    }

    @Test
    public void testDeleteProduct() {
        // Arrange
        Long productId = 1L;
        System.out.println("Input (Product ID to delete): " + productId);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productRepository.deleteById(productId);
        System.out.println("Output: Product with ID " + productId + " deleted successfully.");

        // Assert
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    public void testGetAllProducts() {
        // Arrange
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("PROD001");
        product.setSlug("laptop-dell-xps-13");
        product.setStatus(1); // Active status
        List<Product> products = Collections.singletonList(product);
        System.out.println("Mocked Output (All Products): " + toJson(products));

        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Products list should not be null");
        assertEquals(1, result.size(), "Products list size should be 1");
        assertEquals("Laptop Dell XPS 13", result.get(0).getName(), "Product name should match");
        assertEquals(1, result.get(0).getStatus(), "Product status should match");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    public void testGetProductById() {
        // Arrange
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop Dell XPS 13");
        product.setCode("PROD001");
        product.setSlug("laptop-dell-xps-13");
        product.setStatus(1); // Active status
        System.out.println("Input (Product ID): " + productId);
        System.out.println("Mocked Output (Product): " + toJson(product));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        Optional<Product> productOptional = productRepository.findById(productId);
        System.out.println("Output: " + toJson(productOptional.orElse(null)));

        // Assert
        assertTrue(productOptional.isPresent(), "Product should exist");
        assertEquals("Laptop Dell XPS 13", productOptional.get().getName(), "Product name should match");
        assertEquals(1, productOptional.get().getStatus(), "Product status should match");
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    public void testGetProductById_NotFound() {
        // Arrange
        Long productId = 1L;
        System.out.println("Input (Product ID): " + productId);
        System.out.println("Mocked Output: Product not found.");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> productOptional = productRepository.findById(productId);
        System.out.println("Output: " + toJson(productOptional.orElse(null)));

        // Assert
        assertFalse(productOptional.isPresent(), "Product should not exist");
        verify(productRepository, times(1)).findById(productId);
    }
}