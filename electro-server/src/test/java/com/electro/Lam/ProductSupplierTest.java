package com.electro.Lam;

import com.electro.entity.product.Supplier;
import com.electro.repository.product.SupplierRepository;
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

public class ProductSupplierTest {

    @Mock
    private SupplierRepository supplierRepository;

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
    public void testCreateSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001");
        supplier.setContactFullname("Nguyễn Văn A");
        supplier.setContactEmail("nva@example.com");
        supplier.setContactPhone("0123456789");
        supplier.setCompanyName("Công ty A");
        supplier.setTaxCode("123456789");
        supplier.setEmail("contact@congtya.com");
        supplier.setPhone("0987654321");
        supplier.setFax("0241234567");
        supplier.setWebsite("www.congtya.com");
        supplier.setDescription("Công ty chuyên cung cấp sản phẩm A");
        supplier.setNote("Ghi chú thêm về công ty");
        supplier.setStatus(1);
        System.out.println("Input: " + toJson(supplier));

        when(supplierRepository.save(supplier)).thenReturn(supplier);

        // Act
        Supplier savedSupplier = supplierRepository.save(supplier);
        System.out.println("Output: " + toJson(savedSupplier));

        // Assert
        assertNotNull(savedSupplier, "Saved supplier should not be null");
        assertEquals("Nhà cung cấp A", savedSupplier.getDisplayName(), "Supplier display name should match");
        assertEquals("SUP001", savedSupplier.getCode(), "Supplier code should match");
        assertEquals("Nguyễn Văn A", savedSupplier.getContactFullname(), "Contact fullname should match");
        assertEquals("nva@example.com", savedSupplier.getContactEmail(), "Contact email should match");
        assertEquals("0123456789", savedSupplier.getContactPhone(), "Contact phone should match");
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    public void testUpdateSupplier() {
        // Arrange
        Supplier existingSupplier = new Supplier();
        existingSupplier.setId(1L);
        existingSupplier.setDisplayName("Nhà cung cấp A");
        existingSupplier.setCode("SUP001");
        existingSupplier.setContactFullname("Nguyễn Văn A");
        existingSupplier.setContactEmail("nva@example.com");
        existingSupplier.setContactPhone("0123456789");
        System.out.println("Input (Existing Supplier): " + toJson(existingSupplier));

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(existingSupplier));

        // Act
        Optional<Supplier> supplierOptional = supplierRepository.findById(1L);
        assertTrue(supplierOptional.isPresent(), "Supplier should exist");
        Supplier supplierToUpdate = supplierOptional.get();
        supplierToUpdate.setDisplayName("Nhà cung cấp B");
        supplierToUpdate.setContactFullname("Trần Văn B");
        supplierToUpdate.setContactEmail("tvb@example.com");
        supplierToUpdate.setContactPhone("0987654321");
        System.out.println("Input (Updated Supplier): " + toJson(supplierToUpdate));

        when(supplierRepository.save(supplierToUpdate)).thenReturn(supplierToUpdate);
        Supplier updatedSupplier = supplierRepository.save(supplierToUpdate);
        System.out.println("Output: " + toJson(updatedSupplier));

        // Assert
        assertNotNull(updatedSupplier, "Updated supplier should not be null");
        assertEquals("Nhà cung cấp B", updatedSupplier.getDisplayName(), "Updated display name should match");
        assertEquals("Trần Văn B", updatedSupplier.getContactFullname(), "Updated contact fullname should match");
        assertEquals("tvb@example.com", updatedSupplier.getContactEmail(), "Updated contact email should match");
        assertEquals("0987654321", updatedSupplier.getContactPhone(), "Updated contact phone should match");
        verify(supplierRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).save(supplierToUpdate);
    }

    @Test
    public void testDeleteSupplier() {
        // Arrange
        Long supplierId = 1L;
        System.out.println("Input (Supplier ID to delete): " + supplierId);
        doNothing().when(supplierRepository).deleteById(supplierId);

        // Act
        supplierRepository.deleteById(supplierId);
        System.out.println("Output: Supplier with ID " + supplierId + " deleted successfully.");

        // Assert
        verify(supplierRepository, times(1)).deleteById(supplierId);
    }

    @Test
    public void testGetAllSuppliers() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001");
        List<Supplier> suppliers = Collections.singletonList(supplier);
        System.out.println("Mocked Output (All Suppliers): " + toJson(suppliers));

        when(supplierRepository.findAll()).thenReturn(suppliers);

        // Act
        List<Supplier> result = supplierRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Suppliers list should not be null");
        assertEquals(1, result.size(), "Suppliers list size should be 1");
        assertEquals("Nhà cung cấp A", result.get(0).getDisplayName(), "Supplier display name should match");
        assertEquals("SUP001", result.get(0).getCode(), "Supplier code should match");
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    public void testGetSupplierById() {
        // Arrange
        Long supplierId = 1L;
        Supplier supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001");
        System.out.println("Input (Supplier ID): " + supplierId);
        System.out.println("Mocked Output (Supplier): " + toJson(supplier));

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // Act
        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierId);
        System.out.println("Output: " + toJson(supplierOptional.orElse(null)));

        // Assert
        assertTrue(supplierOptional.isPresent(), "Supplier should exist");
        assertEquals("Nhà cung cấp A", supplierOptional.get().getDisplayName(), "Supplier display name should match");
        assertEquals("SUP001", supplierOptional.get().getCode(), "Supplier code should match");
        verify(supplierRepository, times(1)).findById(supplierId);
    }

    @Test
    public void testGetSupplierById_NotFound() {
        // Arrange
        Long supplierId = 1L;
        System.out.println("Input (Supplier ID): " + supplierId);
        System.out.println("Mocked Output: Supplier not found.");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // Act
        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierId);
        System.out.println("Output: " + toJson(supplierOptional.orElse(null)));

        // Assert
        assertFalse(supplierOptional.isPresent(), "Supplier should not exist");
        verify(supplierRepository, times(1)).findById(supplierId);
    }
}