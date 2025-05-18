package com.electro.Lam;

import com.electro.entity.product.Product;
import com.electro.entity.product.Unit;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Unit entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductUnitTest {
    
    @Autowired
    private UnitRepository unitRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    public void setUp() {
        // Disable foreign key checks to avoid constraint issues
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
        
        // Clear existing data before each test
        productRepository.findAll().forEach(product -> {
            product.setUnit(null);
            productRepository.save(product);
        });
        
        unitRepository.deleteAll();
        
        // Re-enable foreign key checks
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
    }
    
    /**
     * Test Case ID: UIT001
     * Tên test: testCreateUnit
     * Mục tiêu: Kiểm tra việc tạo mới đơn vị đo lường
     * Đầu vào: Một đối tượng Unit với name="Kilogram", status=1
     * Đầu ra mong đợi: Unit được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo đơn vị đo lường
     */
    @Test
    public void testCreateUnit() {
        // Arrange
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1);
        System.out.println("Input: Unit [name=Kilogram, status=1]");
        
        // Act
        Unit savedUnit = unitRepository.save(unit);
        System.out.println("Expected Output: Saved Unit with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedUnit.getId(), "Saved unit ID should not be null");
        assertEquals("Kilogram", savedUnit.getName(), "Unit name should match");
        assertEquals(1, savedUnit.getStatus(), "Unit status should match");
    }
    
    /**
     * Test Case ID: UIT002
     * Tên test: testUpdateUnit
     * Mục tiêu: Kiểm tra việc cập nhật thông tin đơn vị đo lường
     * Đầu vào: Đơn vị đo lường đã tồn tại với name="Kilogram" và cập nhật thành name="Kilograms", status=0
     * Đầu ra mong đợi: Unit được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật đơn vị đo lường
     */
    @Test
    public void testUpdateUnit() {
        // Arrange - Create and save a unit
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        System.out.println("Input: Existing Unit [id=" + savedUnit.getId() + 
                          "] with updates [name=Kilograms, status=0]");
        
        // Act - Update the unit
        savedUnit.setName("Kilograms");
        savedUnit.setStatus(0);
        Unit updatedUnit = unitRepository.save(savedUnit);
        System.out.println("Expected Output: Updated Unit with new values [name=Kilograms, status=0]");
        
        // Assert
        assertEquals("Kilograms", updatedUnit.getName(), "Updated name should match");
        assertEquals(0, updatedUnit.getStatus(), "Updated status should match");
    }
    
    /**
     * Test Case ID: UIT003
     * Tên test: testDeleteUnit
     * Mục tiêu: Kiểm tra việc xóa đơn vị đo lường
     * Đầu vào: ID của đơn vị đo lường đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Đơn vị đo lường bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa đơn vị đo lường
     */
    @Test
    public void testDeleteUnit() {
        // Arrange - Create and save a unit
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        Long unitId = savedUnit.getId();
        System.out.println("Input: Unit ID to delete = " + unitId);
        
        // Act - Delete the unit
        unitRepository.deleteById(unitId);
        System.out.println("Expected Output: Unit with ID = " + unitId + " no longer exists in database");
        
        // Assert
        Optional<Unit> deletedUnit = unitRepository.findById(unitId);
        assertFalse(deletedUnit.isPresent(), "Unit should be deleted");
    }
    
    /**
     * Test Case ID: UIT004
     * Tên test: testGetAllUnits
     * Mục tiêu: Kiểm tra việc lấy tất cả đơn vị đo lường
     * Đầu vào: Hai đơn vị đo lường "Kilogram" và "Piece" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các đơn vị đo lường có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả đơn vị đo lường
     */
    @Test
    public void testGetAllUnits() {
        // Arrange - Create and save two units
        Unit unit1 = new Unit();
        unit1.setName("Kilogram");
        unit1.setStatus(1);
        
        Unit unit2 = new Unit();
        unit2.setName("Piece");
        unit2.setStatus(1);
        
        unitRepository.save(unit1);
        unitRepository.save(unit2);
        System.out.println("Input: Database with two units - 'Kilogram' and 'Piece'");
        
        // Act
        List<Unit> units = unitRepository.findAll();
        System.out.println("Expected Output: List containing 2 units");
        
        // Assert
        assertTrue(units.size() >= 2, "There should be at least 2 units");
        assertTrue(units.stream().anyMatch(u -> u.getName().equals("Kilogram")), "Should contain 'Kilogram' unit");
        assertTrue(units.stream().anyMatch(u -> u.getName().equals("Piece")), "Should contain 'Piece' unit");
    }
    
    /**
     * Test Case ID: UIT005
     * Tên test: testGetUnitById
     * Mục tiêu: Kiểm tra việc tìm kiếm đơn vị đo lường theo ID
     * Đầu vào: ID của đơn vị đo lường đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy đơn vị đo lường với name="Kilogram"
     * Ghi chú: Kiểm tra chức năng tìm kiếm đơn vị đo lường theo ID
     */
    @Test
    public void testGetUnitById() {
        // Arrange - Create and save a unit
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        Long unitId = savedUnit.getId();
        System.out.println("Input: Search for unit with id=" + unitId);
        
        // Act
        Optional<Unit> foundUnit = unitRepository.findById(unitId);
        System.out.println("Expected Output: Found unit with name='Kilogram'");
        
        // Assert
        assertTrue(foundUnit.isPresent(), "Unit should exist");
        assertEquals("Kilogram", foundUnit.get().getName(), "Unit name should match");
        assertEquals(1, foundUnit.get().getStatus(), "Unit status should match");
    }
    
    /**
     * Test Case ID: UIT006
     * Tên test: testGetUnitById_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm đơn vị đo lường với ID không tồn tại
     * Đầu vào: ID không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy đơn vị đo lường nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệu
     */
    @Test
    public void testGetUnitById_NotFound() {
        // Non-existent ID (assuming no unit with ID 999 exists)
        Long nonExistingId = 999L;
        System.out.println("Input: Search for unit with non-existent id=" + nonExistingId);
        
        // Act
        Optional<Unit> foundUnit = unitRepository.findById(nonExistingId);
        System.out.println("Expected Output: No unit found (empty Optional)");
        
        // Assert
        assertFalse(foundUnit.isPresent(), "Unit should not exist");
    }
    
    /**
     * Test Case ID: UIT007
     * Tên test: testUnitProductRelationship
     * Mục tiêu: Kiểm tra mối quan hệ giữa Unit và Product
     * Đầu vào: Một Unit và một Product được liên kết với nhau
     * Đầu ra mong đợi: Product có Unit và Unit có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ One-to-Many giữa Unit và Product
     */
    @Test
    public void testUnitProductRelationship() {
        // Arrange
        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        
        Product product = new Product();
        product.setName("Rice");
        product.setCode("PRD001");
        product.setSlug("rice");
        product.setStatus(1);
        product.setUnit(savedUnit);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Unit [name=Kilogram] with Product [name=Rice]");
        
        // Act - Refresh data from database to ensure relationships are properly loaded
        entityManager.flush();
        entityManager.clear();
        
        Unit refreshedUnit = unitRepository.findById(savedUnit.getId()).orElseThrow();
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        System.out.println("Expected Output: Product with Unit and Unit with Product in products list");
        
        // Assert
        assertNotNull(refreshedProduct.getUnit(), "Product should have a unit");
        assertEquals(savedUnit.getId(), refreshedProduct.getUnit().getId(), "Product should be linked to the correct unit");
        assertTrue(refreshedUnit.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())), 
                  "Unit's products should contain the created product");
    }
    
    /**
     * Test Case ID: UIT008
     * Tên test: testCreateMultipleUnits
     * Mục tiêu: Kiểm tra việc tạo nhiều đơn vị đo lường
     * Đầu vào: Ba đơn vị đo lường với tên và trạng thái khác nhau
     * Đầu ra mong đợi: Tất cả đơn vị đo lường được lưu thành công với ID không null
     * Ghi chú: Kiểm tra chức năng lưu nhiều đơn vị đo lường
     */
    @Test
    public void testCreateMultipleUnits() {
        // Arrange
        Unit unit1 = new Unit().setName("Kilogram").setStatus(1);
        Unit unit2 = new Unit().setName("Piece").setStatus(1);
        Unit unit3 = new Unit().setName("Liter").setStatus(0);
        
        System.out.println("Input: Three units - 'Kilogram', 'Piece', 'Liter'");
        
        // Act
        Unit savedUnit1 = unitRepository.save(unit1);
        Unit savedUnit2 = unitRepository.save(unit2);
        Unit savedUnit3 = unitRepository.save(unit3);
        
        // Assert
        assertNotNull(savedUnit1.getId(), "Saved unit1 ID should not be null");
        assertNotNull(savedUnit2.getId(), "Saved unit2 ID should not be null");
        assertNotNull(savedUnit3.getId(), "Saved unit3 ID should not be null");
        
        List<Unit> units = unitRepository.findAll();
        assertTrue(units.size() >= 3, "There should be at least 3 units");
        
        System.out.println("Expected Output: Three units saved with non-null IDs");
    }
}
