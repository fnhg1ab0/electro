package com.electro.Lam;

import com.electro.entity.product.Supplier;
import com.electro.repository.product.SupplierRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Supplier entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
public class ProductSupplierTest {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testCreateSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001Create");
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

        // Act
        Supplier savedSupplier = supplierRepository.save(supplier);
        System.out.println("Output: " + toJson(savedSupplier));

        // Assert
        assertNotNull(savedSupplier.getId(), "Saved supplier ID should not be null");
        assertEquals("Nhà cung cấp A", savedSupplier.getDisplayName(), "Supplier display name should match");
        assertEquals("SUP001Create", savedSupplier.getCode(), "Supplier code should match");
    }


    @Test
    public void testUpdateSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001Update");
        supplier.setContactFullname("Nguyễn Văn A");
        supplier.setContactEmail("nva@example.com");
        supplier.setContactPhone("0123456789");
        supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);

        System.out.println("Input (Existing Supplier): " + toJson(savedSupplier));

        // Act
        savedSupplier.setDisplayName("Nhà cung cấp B");
        savedSupplier.setContactFullname("Trần Văn B");
        savedSupplier.setContactEmail("tvb@example.com");
        savedSupplier.setContactPhone("0987654321");
        Supplier updatedSupplier = supplierRepository.save(savedSupplier);

        System.out.println("Output: " + toJson(updatedSupplier));

        // Assert
        assertEquals("Nhà cung cấp B", updatedSupplier.getDisplayName(), "Updated display name should match");
        assertEquals("Trần Văn B", updatedSupplier.getContactFullname(), "Updated contact fullname should match");
        assertEquals("tvb@example.com", updatedSupplier.getContactEmail(), "Updated contact email should match");
    }

    @Test
    public void testDeleteSupplier() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001Delete");
        supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);
        Long supplierId = savedSupplier.getId();

        System.out.println("Input (Supplier ID to delete): " + supplierId);

        // Act
        supplierRepository.deleteById(supplierId);

        // Assert
        Optional<Supplier> deletedSupplier = supplierRepository.findById(supplierId);
        assertFalse(deletedSupplier.isPresent(), "Supplier should be deleted");
    }

    @Test
    public void testGetAllSuppliers() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001GetAll");
        supplier.setStatus(1);
        supplierRepository.save(supplier);

        // Act
        List<Supplier> suppliers = supplierRepository.findAll();

        // Assert
        assertTrue(suppliers.size() >= 1 , "Suppliers list size should be more than 1");
        assertEquals("Nhà cung cấp A", suppliers.get(0).getDisplayName(), "Supplier display name should match");
    }

    
    @Test
    public void testGetSupplierById() {
        // Arrange
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Nhà cung cấp A");
        supplier.setCode("SUP001GetById");
        supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);
        Long supplierId = savedSupplier.getId();

        // Act
        Optional<Supplier> foundSupplier = supplierRepository.findById(supplierId);

        // Assert
        assertTrue(foundSupplier.isPresent(), "Supplier should exist");
        assertEquals("Nhà cung cấp A", foundSupplier.get().getDisplayName(), "Supplier display name should match");
    }

    @Test
    public void testGetSupplierById_NotFound() {
        // Act
        Optional<Supplier> foundSupplier = supplierRepository.findById(999L); // Non-existent ID

        // Assert
        assertFalse(foundSupplier.isPresent(), "Supplier should not exist");
    }
}