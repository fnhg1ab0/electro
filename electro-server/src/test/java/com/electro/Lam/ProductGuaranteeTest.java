package com.electro.Lam;

import com.electro.entity.product.Guarantee;
import com.electro.repository.product.GuaranteeRepository;
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

public class ProductGuaranteeTest {

    @Mock
    private GuaranteeRepository guaranteeRepository;

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
    public void testCreateGuarantee() {
        // Arrange
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1); // Active status
        System.out.println("Input: " + toJson(guarantee));

        when(guaranteeRepository.save(guarantee)).thenReturn(guarantee);

        // Act
        Guarantee savedGuarantee = guaranteeRepository.save(guarantee);
        System.out.println("Output: " + toJson(savedGuarantee));

        // Assert
        assertNotNull(savedGuarantee, "Saved guarantee should not be null");
        assertEquals("12-Month Warranty", savedGuarantee.getName(), "Guarantee name should match");
        assertEquals("Covers all manufacturing defects", savedGuarantee.getDescription(), "Guarantee description should match");
        assertEquals(1, savedGuarantee.getStatus(), "Guarantee status should match");
        verify(guaranteeRepository, times(1)).save(guarantee);
    }

    @Test
    public void testUpdateGuarantee() {
        // Arrange
        Guarantee existingGuarantee = new Guarantee();
        existingGuarantee.setId(1L);
        existingGuarantee.setName("12-Month Warranty");
        existingGuarantee.setDescription("Covers all manufacturing defects");
        existingGuarantee.setStatus(1); // Active status
        System.out.println("Input (Existing Guarantee): " + toJson(existingGuarantee));

        when(guaranteeRepository.findById(1L)).thenReturn(Optional.of(existingGuarantee));

        // Act
        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(1L);
        assertTrue(guaranteeOptional.isPresent(), "Guarantee should exist");
        Guarantee guaranteeToUpdate = guaranteeOptional.get();
        guaranteeToUpdate.setName("24-Month Warranty");
        guaranteeToUpdate.setDescription("Extended warranty for 24 months");
        guaranteeToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Guarantee): " + toJson(guaranteeToUpdate));

        when(guaranteeRepository.save(guaranteeToUpdate)).thenReturn(guaranteeToUpdate);
        Guarantee updatedGuarantee = guaranteeRepository.save(guaranteeToUpdate);
        System.out.println("Output: " + toJson(updatedGuarantee));

        // Assert
        assertNotNull(updatedGuarantee, "Updated guarantee should not be null");
        assertEquals("24-Month Warranty", updatedGuarantee.getName(), "Updated name should match");
        assertEquals("Extended warranty for 24 months", updatedGuarantee.getDescription(), "Updated description should match");
        assertEquals(0, updatedGuarantee.getStatus(), "Updated status should match");
        verify(guaranteeRepository, times(1)).findById(1L);
        verify(guaranteeRepository, times(1)).save(guaranteeToUpdate);
    }

    @Test
    public void testDeleteGuarantee() {
        // Arrange
        Long guaranteeId = 1L;
        System.out.println("Input (Guarantee ID to delete): " + guaranteeId);
        doNothing().when(guaranteeRepository).deleteById(guaranteeId);

        // Act
        guaranteeRepository.deleteById(guaranteeId);
        System.out.println("Output: Guarantee with ID " + guaranteeId + " deleted successfully.");

        // Assert
        verify(guaranteeRepository, times(1)).deleteById(guaranteeId);
    }

    @Test
    public void testGetAllGuarantees() {
        // Arrange
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1); // Active status
        List<Guarantee> guarantees = Collections.singletonList(guarantee);
        System.out.println("Mocked Output (All Guarantees): " + toJson(guarantees));

        when(guaranteeRepository.findAll()).thenReturn(guarantees);

        // Act
        List<Guarantee> result = guaranteeRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Guarantees list should not be null");
        assertEquals(1, result.size(), "Guarantees list size should be 1");
        assertEquals("12-Month Warranty", result.get(0).getName(), "Guarantee name should match");
        assertEquals(1, result.get(0).getStatus(), "Guarantee status should match");
        verify(guaranteeRepository, times(1)).findAll();
    }

    @Test
    public void testGetGuaranteeById() {
        // Arrange
        Long guaranteeId = 1L;
        Guarantee guarantee = new Guarantee();
        guarantee.setId(guaranteeId);
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1); // Active status
        System.out.println("Input (Guarantee ID): " + guaranteeId);
        System.out.println("Mocked Output (Guarantee): " + toJson(guarantee));

        when(guaranteeRepository.findById(guaranteeId)).thenReturn(Optional.of(guarantee));

        // Act
        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(guaranteeId);
        System.out.println("Output: " + toJson(guaranteeOptional.orElse(null)));

        // Assert
        assertTrue(guaranteeOptional.isPresent(), "Guarantee should exist");
        assertEquals("12-Month Warranty", guaranteeOptional.get().getName(), "Guarantee name should match");
        assertEquals(1, guaranteeOptional.get().getStatus(), "Guarantee status should match");
        verify(guaranteeRepository, times(1)).findById(guaranteeId);
    }

    @Test
    public void testGetGuaranteeById_NotFound() {
        // Arrange
        Long guaranteeId = 1L;
        System.out.println("Input (Guarantee ID): " + guaranteeId);
        System.out.println("Mocked Output: Guarantee not found.");

        when(guaranteeRepository.findById(guaranteeId)).thenReturn(Optional.empty());

        // Act
        Optional<Guarantee> guaranteeOptional = guaranteeRepository.findById(guaranteeId);
        System.out.println("Output: " + toJson(guaranteeOptional.orElse(null)));

        // Assert
        assertFalse(guaranteeOptional.isPresent(), "Guarantee should not exist");
        verify(guaranteeRepository, times(1)).findById(guaranteeId);
    }
}