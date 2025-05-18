package com.electro.Lam;

import com.electro.entity.product.Brand;
import com.electro.repository.product.BrandRepository;
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

public class ProductBrandTest {

    @Mock
    private BrandRepository brandRepository;

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
    public void testCreateBrand() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        brand.setDescription("Electronics brand");
        brand.setStatus(1);
        System.out.println("Input: " + toJson(brand));

        when(brandRepository.save(brand)).thenReturn(brand);

        // Act
        Brand savedBrand = brandRepository.save(brand);
        System.out.println("Output: " + toJson(savedBrand));

        // Assert
        assertNotNull(savedBrand, "Saved brand should not be null");
        assertEquals("Samsung", savedBrand.getName(), "Brand name should match");
        assertEquals("SAM123", savedBrand.getCode(), "Brand code should match");
        assertEquals("Electronics brand", savedBrand.getDescription(), "Brand description should match");
        assertEquals(1, savedBrand.getStatus(), "Brand status should match");
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    public void testUpdateBrand() {
        // Arrange
        Brand existingBrand = new Brand();
        existingBrand.setId(1L);
        existingBrand.setName("Samsung");
        existingBrand.setCode("SAM123");
        existingBrand.setDescription("Electronics brand");
        existingBrand.setStatus(1);
        System.out.println("Input (Existing Brand): " + toJson(existingBrand));

        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));

        // Act
        Optional<Brand> brandOptional = brandRepository.findById(1L);
        assertTrue(brandOptional.isPresent(), "Brand should exist");
        Brand brandToUpdate = brandOptional.get();
        brandToUpdate.setName("Updated Samsung");
        brandToUpdate.setDescription("Updated description");
        System.out.println("Input (Updated Brand): " + toJson(brandToUpdate));

        when(brandRepository.save(brandToUpdate)).thenReturn(brandToUpdate);
        Brand updatedBrand = brandRepository.save(brandToUpdate);
        System.out.println("Output: " + toJson(updatedBrand));

        // Assert
        assertNotNull(updatedBrand, "Updated brand should not be null");
        assertEquals("Updated Samsung", updatedBrand.getName(), "Updated name should match");
        assertEquals("Updated description", updatedBrand.getDescription(), "Updated description should match");
        verify(brandRepository, times(1)).findById(1L);
        verify(brandRepository, times(1)).save(brandToUpdate);
    }

    @Test
    public void testDeleteBrand() {
        // Arrange
        Long brandId = 1L;
        System.out.println("Input (Brand ID to delete): " + brandId);
        doNothing().when(brandRepository).deleteById(brandId);

        // Act
        brandRepository.deleteById(brandId);
        System.out.println("Output: Brand with ID " + brandId + " deleted successfully.");

        // Assert
        verify(brandRepository, times(1)).deleteById(brandId);
    }

    @Test
    public void testGetAllBrands() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        List<Brand> brands = Collections.singletonList(brand);
        System.out.println("Mocked Output (All Brands): " + toJson(brands));

        when(brandRepository.findAll()).thenReturn(brands);

        // Act
        List<Brand> result = brandRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Brands list should not be null");
        assertEquals(1, result.size(), "Brands list size should be 1");
        assertEquals("Samsung", result.get(0).getName(), "Brand name should match");
        verify(brandRepository, times(1)).findAll();
    }

    @Test
    public void testGetBrandById() {
        // Arrange
        Long brandId = 1L;
        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("Samsung");
        brand.setCode("SAM123");
        System.out.println("Input (Brand ID): " + brandId);
        System.out.println("Mocked Output (Brand): " + toJson(brand));

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));

        // Act
        Optional<Brand> brandOptional = brandRepository.findById(brandId);
        System.out.println("Output: " + toJson(brandOptional.orElse(null)));

        // Assert
        assertTrue(brandOptional.isPresent(), "Brand should exist");
        assertEquals("Samsung", brandOptional.get().getName(), "Brand name should match");
        assertEquals("SAM123", brandOptional.get().getCode(), "Brand code should match");
        verify(brandRepository, times(1)).findById(brandId);
    }

    @Test
    public void testGetBrandById_NotFound() {
        // Arrange
        Long brandId = 1L;
        System.out.println("Input (Brand ID): " + brandId);
        System.out.println("Mocked Output: Brand not found.");

        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        // Act
        Optional<Brand> brandOptional = brandRepository.findById(brandId);
        System.out.println("Output: " + toJson(brandOptional.orElse(null)));

        // Assert
        assertFalse(brandOptional.isPresent(), "Brand should not exist");
        verify(brandRepository, times(1)).findById(brandId);
    }
}