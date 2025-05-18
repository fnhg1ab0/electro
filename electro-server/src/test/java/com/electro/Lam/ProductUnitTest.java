package com.electro.Lam;

import com.electro.entity.product.Unit;
import com.electro.repository.product.UnitRepository;
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

public class ProductUnitTest {

    @Mock
    private UnitRepository unitRepository;

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
    public void testCreateUnit() {
        // Arrange
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1); // Active status
        System.out.println("Input: " + toJson(unit));

        when(unitRepository.save(unit)).thenReturn(unit);

        // Act
        Unit savedUnit = unitRepository.save(unit);
        System.out.println("Output: " + toJson(savedUnit));

        // Assert
        assertNotNull(savedUnit, "Saved unit should not be null");
        assertEquals("Kilogram", savedUnit.getName(), "Unit name should match");
        assertEquals(1, savedUnit.getStatus(), "Unit status should match");
        verify(unitRepository, times(1)).save(unit);
    }

    @Test
    public void testUpdateUnit() {
        // Arrange
        Unit existingUnit = new Unit();
        existingUnit.setId(1L);
        existingUnit.setName("Kilogram");
        existingUnit.setStatus(1); // Active status
        System.out.println("Input (Existing Unit): " + toJson(existingUnit));

        when(unitRepository.findById(1L)).thenReturn(Optional.of(existingUnit));

        // Act
        Optional<Unit> unitOptional = unitRepository.findById(1L);
        assertTrue(unitOptional.isPresent(), "Unit should exist");
        Unit unitToUpdate = unitOptional.get();
        unitToUpdate.setName("Gram");
        unitToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Unit): " + toJson(unitToUpdate));

        when(unitRepository.save(unitToUpdate)).thenReturn(unitToUpdate);
        Unit updatedUnit = unitRepository.save(unitToUpdate);
        System.out.println("Output: " + toJson(updatedUnit));

        // Assert
        assertNotNull(updatedUnit, "Updated unit should not be null");
        assertEquals("Gram", updatedUnit.getName(), "Updated name should match");
        assertEquals(0, updatedUnit.getStatus(), "Updated status should match");
        verify(unitRepository, times(1)).findById(1L);
        verify(unitRepository, times(1)).save(unitToUpdate);
    }

    @Test
    public void testDeleteUnit() {
        // Arrange
        Long unitId = 1L;
        System.out.println("Input (Unit ID to delete): " + unitId);
        doNothing().when(unitRepository).deleteById(unitId);

        // Act
        unitRepository.deleteById(unitId);
        System.out.println("Output: Unit with ID " + unitId + " deleted successfully.");

        // Assert
        verify(unitRepository, times(1)).deleteById(unitId);
    }

    @Test
    public void testGetAllUnits() {
        // Arrange
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1); // Active status
        List<Unit> units = Collections.singletonList(unit);
        System.out.println("Mocked Output (All Units): " + toJson(units));

        when(unitRepository.findAll()).thenReturn(units);

        // Act
        List<Unit> result = unitRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Units list should not be null");
        assertEquals(1, result.size(), "Units list size should be 1");
        assertEquals("Kilogram", result.get(0).getName(), "Unit name should match");
        assertEquals(1, result.get(0).getStatus(), "Unit status should match");
        verify(unitRepository, times(1)).findAll();
    }

    @Test
    public void testGetUnitById() {
        // Arrange
        Long unitId = 1L;
        Unit unit = new Unit();
        unit.setId(unitId);
        unit.setName("Kilogram");
        unit.setStatus(1); // Active status
        System.out.println("Input (Unit ID): " + unitId);
        System.out.println("Mocked Output (Unit): " + toJson(unit));

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        // Act
        Optional<Unit> unitOptional = unitRepository.findById(unitId);
        System.out.println("Output: " + toJson(unitOptional.orElse(null)));

        // Assert
        assertTrue(unitOptional.isPresent(), "Unit should exist");
        assertEquals("Kilogram", unitOptional.get().getName(), "Unit name should match");
        assertEquals(1, unitOptional.get().getStatus(), "Unit status should match");
        verify(unitRepository, times(1)).findById(unitId);
    }

    @Test
    public void testGetUnitById_NotFound() {
        // Arrange
        Long unitId = 1L;
        System.out.println("Input (Unit ID): " + unitId);
        System.out.println("Mocked Output: Unit not found.");

        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        // Act
        Optional<Unit> unitOptional = unitRepository.findById(unitId);
        System.out.println("Output: " + toJson(unitOptional.orElse(null)));

        // Assert
        assertFalse(unitOptional.isPresent(), "Unit should not exist");
        verify(unitRepository, times(1)).findById(unitId);
    }
}