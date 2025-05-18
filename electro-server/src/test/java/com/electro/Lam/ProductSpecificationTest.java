package com.electro.Lam;

import com.electro.entity.product.Specification;
import com.electro.repository.product.SpecificationRepository;
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

public class ProductSpecificationTest {

    @Mock
    private SpecificationRepository specificationRepository;

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
    public void testCreateSpecification() {
        // Arrange
        Specification specification = new Specification();
        specification.setName("Weight");
        specification.setCode("SPEC001");
        specification.setDescription("Specification for product weight");
        specification.setStatus(1); // Active status
        System.out.println("Input: " + toJson(specification));

        when(specificationRepository.save(specification)).thenReturn(specification);

        // Act
        Specification savedSpecification = specificationRepository.save(specification);
        System.out.println("Output: " + toJson(savedSpecification));

        // Assert
        assertNotNull(savedSpecification, "Saved specification should not be null");
        assertEquals("Weight", savedSpecification.getName(), "Specification name should match");
        assertEquals("SPEC001", savedSpecification.getCode(), "Specification code should match");
        assertEquals("Specification for product weight", savedSpecification.getDescription(), "Specification description should match");
        assertEquals(1, savedSpecification.getStatus(), "Specification status should match");
        verify(specificationRepository, times(1)).save(specification);
    }

    @Test
    public void testUpdateSpecification() {
        // Arrange
        Specification existingSpecification = new Specification();
        existingSpecification.setId(1L);
        existingSpecification.setName("Weight");
        existingSpecification.setCode("SPEC001");
        existingSpecification.setDescription("Specification for product weight");
        existingSpecification.setStatus(1); // Active status
        System.out.println("Input (Existing Specification): " + toJson(existingSpecification));

        when(specificationRepository.findById(1L)).thenReturn(Optional.of(existingSpecification));

        // Act
        Optional<Specification> specificationOptional = specificationRepository.findById(1L);
        assertTrue(specificationOptional.isPresent(), "Specification should exist");
        Specification specificationToUpdate = specificationOptional.get();
        specificationToUpdate.setName("Dimensions");
        specificationToUpdate.setCode("SPEC002");
        specificationToUpdate.setDescription("Specification for product dimensions");
        specificationToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Specification): " + toJson(specificationToUpdate));

        when(specificationRepository.save(specificationToUpdate)).thenReturn(specificationToUpdate);
        Specification updatedSpecification = specificationRepository.save(specificationToUpdate);
        System.out.println("Output: " + toJson(updatedSpecification));

        // Assert
        assertNotNull(updatedSpecification, "Updated specification should not be null");
        assertEquals("Dimensions", updatedSpecification.getName(), "Updated name should match");
        assertEquals("SPEC002", updatedSpecification.getCode(), "Updated code should match");
        assertEquals("Specification for product dimensions", updatedSpecification.getDescription(), "Updated description should match");
        assertEquals(0, updatedSpecification.getStatus(), "Updated status should match");
        verify(specificationRepository, times(1)).findById(1L);
        verify(specificationRepository, times(1)).save(specificationToUpdate);
    }

    @Test
    public void testDeleteSpecification() {
        // Arrange
        Long specificationId = 1L;
        System.out.println("Input (Specification ID to delete): " + specificationId);
        doNothing().when(specificationRepository).deleteById(specificationId);

        // Act
        specificationRepository.deleteById(specificationId);
        System.out.println("Output: Specification with ID " + specificationId + " deleted successfully.");

        // Assert
        verify(specificationRepository, times(1)).deleteById(specificationId);
    }

    @Test
    public void testGetAllSpecifications() {
        // Arrange
        Specification specification = new Specification();
        specification.setName("Weight");
        specification.setCode("SPEC001");
        specification.setDescription("Specification for product weight");
        specification.setStatus(1); // Active status
        List<Specification> specifications = Collections.singletonList(specification);
        System.out.println("Mocked Output (All Specifications): " + toJson(specifications));

        when(specificationRepository.findAll()).thenReturn(specifications);

        // Act
        List<Specification> result = specificationRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Specifications list should not be null");
        assertEquals(1, result.size(), "Specifications list size should be 1");
        assertEquals("Weight", result.get(0).getName(), "Specification name should match");
        assertEquals(1, result.get(0).getStatus(), "Specification status should match");
        verify(specificationRepository, times(1)).findAll();
    }

    @Test
    public void testGetSpecificationById() {
        // Arrange
        Long specificationId = 1L;
        Specification specification = new Specification();
        specification.setId(specificationId);
        specification.setName("Weight");
        specification.setCode("SPEC001");
        specification.setDescription("Specification for product weight");
        specification.setStatus(1); // Active status
        System.out.println("Input (Specification ID): " + specificationId);
        System.out.println("Mocked Output (Specification): " + toJson(specification));

        when(specificationRepository.findById(specificationId)).thenReturn(Optional.of(specification));

        // Act
        Optional<Specification> specificationOptional = specificationRepository.findById(specificationId);
        System.out.println("Output: " + toJson(specificationOptional.orElse(null)));

        // Assert
        assertTrue(specificationOptional.isPresent(), "Specification should exist");
        assertEquals("Weight", specificationOptional.get().getName(), "Specification name should match");
        assertEquals(1, specificationOptional.get().getStatus(), "Specification status should match");
        verify(specificationRepository, times(1)).findById(specificationId);
    }

    @Test
    public void testGetSpecificationById_NotFound() {
        // Arrange
        Long specificationId = 1L;
        System.out.println("Input (Specification ID): " + specificationId);
        System.out.println("Mocked Output: Specification not found.");

        when(specificationRepository.findById(specificationId)).thenReturn(Optional.empty());

        // Act
        Optional<Specification> specificationOptional = specificationRepository.findById(specificationId);
        System.out.println("Output: " + toJson(specificationOptional.orElse(null)));

        // Assert
        assertFalse(specificationOptional.isPresent(), "Specification should not exist");
        verify(specificationRepository, times(1)).findById(specificationId);
    }
}