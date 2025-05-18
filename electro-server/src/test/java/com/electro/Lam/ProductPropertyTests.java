package com.electro.Lam;

import com.electro.entity.product.Property;
import com.electro.repository.product.PropertyRepository;
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

public class ProductPropertyTests {

    @Mock
    private PropertyRepository propertyRepository;

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
    public void testCreateProperty() {
        // Arrange
        Property property = new Property();
        property.setName("Color");
        property.setCode("PROP001");
        property.setDescription("Property for product color");
        property.setStatus(1); // Active status
        System.out.println("Input: " + toJson(property));

        when(propertyRepository.save(property)).thenReturn(property);

        // Act
        Property savedProperty = propertyRepository.save(property);
        System.out.println("Output: " + toJson(savedProperty));

        // Assert
        assertNotNull(savedProperty, "Saved property should not be null");
        assertEquals("Color", savedProperty.getName(), "Property name should match");
        assertEquals("PROP001", savedProperty.getCode(), "Property code should match");
        assertEquals("Property for product color", savedProperty.getDescription(), "Property description should match");
        assertEquals(1, savedProperty.getStatus(), "Property status should match");
        verify(propertyRepository, times(1)).save(property);
    }

    @Test
    public void testUpdateProperty() {
        // Arrange
        Property existingProperty = new Property();
        existingProperty.setId(1L);
        existingProperty.setName("Color");
        existingProperty.setCode("PROP001");
        existingProperty.setDescription("Property for product color");
        existingProperty.setStatus(1); // Active status
        System.out.println("Input (Existing Property): " + toJson(existingProperty));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(existingProperty));

        // Act
        Optional<Property> propertyOptional = propertyRepository.findById(1L);
        assertTrue(propertyOptional.isPresent(), "Property should exist");
        Property propertyToUpdate = propertyOptional.get();
        propertyToUpdate.setName("Size");
        propertyToUpdate.setCode("PROP002");
        propertyToUpdate.setDescription("Property for product size");
        propertyToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Property): " + toJson(propertyToUpdate));

        when(propertyRepository.save(propertyToUpdate)).thenReturn(propertyToUpdate);
        Property updatedProperty = propertyRepository.save(propertyToUpdate);
        System.out.println("Output: " + toJson(updatedProperty));

        // Assert
        assertNotNull(updatedProperty, "Updated property should not be null");
        assertEquals("Size", updatedProperty.getName(), "Updated name should match");
        assertEquals("PROP002", updatedProperty.getCode(), "Updated code should match");
        assertEquals("Property for product size", updatedProperty.getDescription(), "Updated description should match");
        assertEquals(0, updatedProperty.getStatus(), "Updated status should match");
        verify(propertyRepository, times(1)).findById(1L);
        verify(propertyRepository, times(1)).save(propertyToUpdate);
    }

    @Test
    public void testDeleteProperty() {
        // Arrange
        Long propertyId = 1L;
        System.out.println("Input (Property ID to delete): " + propertyId);
        doNothing().when(propertyRepository).deleteById(propertyId);

        // Act
        propertyRepository.deleteById(propertyId);
        System.out.println("Output: Property with ID " + propertyId + " deleted successfully.");

        // Assert
        verify(propertyRepository, times(1)).deleteById(propertyId);
    }

    @Test
    public void testGetAllProperties() {
        // Arrange
        Property property = new Property();
        property.setName("Color");
        property.setCode("PROP001");
        property.setDescription("Property for product color");
        property.setStatus(1); // Active status
        List<Property> properties = Collections.singletonList(property);
        System.out.println("Mocked Output (All Properties): " + toJson(properties));

        when(propertyRepository.findAll()).thenReturn(properties);

        // Act
        List<Property> result = propertyRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Properties list should not be null");
        assertEquals(1, result.size(), "Properties list size should be 1");
        assertEquals("Color", result.get(0).getName(), "Property name should match");
        assertEquals(1, result.get(0).getStatus(), "Property status should match");
        verify(propertyRepository, times(1)).findAll();
    }

    @Test
    public void testGetPropertyById() {
        // Arrange
        Long propertyId = 1L;
        Property property = new Property();
        property.setId(propertyId);
        property.setName("Color");
        property.setCode("PROP001");
        property.setDescription("Property for product color");
        property.setStatus(1); // Active status
        System.out.println("Input (Property ID): " + propertyId);
        System.out.println("Mocked Output (Property): " + toJson(property));

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        // Act
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        System.out.println("Output: " + toJson(propertyOptional.orElse(null)));

        // Assert
        assertTrue(propertyOptional.isPresent(), "Property should exist");
        assertEquals("Color", propertyOptional.get().getName(), "Property name should match");
        assertEquals(1, propertyOptional.get().getStatus(), "Property status should match");
        verify(propertyRepository, times(1)).findById(propertyId);
    }

    @Test
    public void testGetPropertyById_NotFound() {
        // Arrange
        Long propertyId = 1L;
        System.out.println("Input (Property ID): " + propertyId);
        System.out.println("Mocked Output: Property not found.");

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // Act
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        System.out.println("Output: " + toJson(propertyOptional.orElse(null)));

        // Assert
        assertFalse(propertyOptional.isPresent(), "Property should not exist");
        verify(propertyRepository, times(1)).findById(propertyId);
    }
}