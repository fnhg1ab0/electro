package com.electro.Lam;

import com.electro.entity.product.Specification;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.client.WishRepository;
import com.electro.repository.general.ImageRepository;
import com.electro.repository.inventory.*;
import com.electro.repository.order.OrderVariantRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.SpecificationRepository;
import com.electro.repository.product.TagRepository;
import com.electro.repository.product.VariantRepository;
import com.electro.repository.promotion.PromotionRepository;
import com.electro.repository.review.ReviewRepository;
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
 * Integration tests for Specification entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
public class ProductSpecificationTest {

    @Autowired
    private SpecificationRepository specificationRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private VariantRepository variantRepository;
    
    @Autowired
    private CartVariantRepository cartVariantRepository;
    
    @Autowired
    private CountVariantRepository countVariantRepository;
    
    @Autowired
    private DocketVariantRepository docketVariantRepository;
    
    @Autowired
    private PurchaseOrderVariantRepository purchaseOrderVariantRepository;
    
    @Autowired
    private OrderVariantRepository orderVariantRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private WishRepository wishRepository;
    
    @Autowired
    private PreorderRepository preorderRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private ProductInventoryLimitRepository productInventoryLimitRepository;

    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    public void setUp() {
        // Temporarily disable foreign key checks to handle complex dependencies
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
        
//        // Clear existing data before each test
//        wishRepository.deleteAll();
//        preorderRepository.deleteAll();
//        reviewRepository.deleteAll();
//
//        // Clear variants and associated entities
//        cartVariantRepository.deleteAll();
//        orderVariantRepository.deleteAll();
//        countVariantRepository.deleteAll();
//        docketVariantRepository.deleteAll();
//        purchaseOrderVariantRepository.deleteAll();
//
//        // Clear product inventory limits
//        productInventoryLimitRepository.deleteAll();
//
//        // Clear images
//        imageRepository.deleteAll();
//
//        // Clear promotion products (join tables for many-to-many relationships)
//        promotionRepository.findAll().forEach(promotion -> {
//            promotion.getProducts().clear();
//            promotionRepository.save(promotion);
//        });
//
//        // Clear variants
//        variantRepository.deleteAll();
//
//        // Clear product_tag join table
//        productRepository.findAll().forEach(product -> {
//            product.getTags().clear();
//            productRepository.save(product);
//        });
//
//        // Now we can safely clear products
//        productRepository.deleteAll();
//
//        // Clear specifications
//        specificationRepository.deleteAll();
//
//        tagRepository.deleteAll();
//
//        // Re-enable foreign key checks
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
    }

    /**
     * Test Case ID: SIT001
     * Tên test: testCreateSpecification
     * Mục tiêu: Kiểm tra việc tạo mới thông số sản phẩm
     * Đầu vào: Một đối tượng Specification với name="Screen Size", 
     *          code="SCREEN_SIZE", description="Display dimensions", status=1
     * Đầu ra mong đợi: Specification được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo thông số sản phẩm
     */
    @Test
    public void testCreateSpecification() {
        // Arrange
        Specification specification = new Specification();
        specification.setName("Screen Size");
        specification.setCode("SCREEN_SIZE_Create");
        specification.setDescription("Display dimensions");
        specification.setStatus(1); // Active status
        System.out.println("Input: Specification [name=Screen Size, code=SCREEN_SIZE, description=Display dimensions, status=1]");
        
        // Act
        Specification savedSpecification = specificationRepository.save(specification);
        System.out.println("Expected Output: Saved Specification with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedSpecification.getId(), "Saved specification ID should not be null");
        assertEquals("Screen Size", savedSpecification.getName(), "Specification name should match");
        assertEquals("SCREEN_SIZE_Create", savedSpecification.getCode(), "Specification code should match");
        assertEquals("Display dimensions", savedSpecification.getDescription(), "Specification description should match");
        assertEquals(1, savedSpecification.getStatus(), "Specification status should match");
    }
    
    /**
     * Test Case ID: SIT002
     * Tên test: testUpdateSpecification
     * Mục tiêu: Kiểm tra việc cập nhật thông tin thông số sản phẩm
     * Đầu vào: Thông số đã tồn tại với name="Screen Size" và cập nhật thành name="Display Size",
     *          description="Updated display dimensions"
     * Đầu ra mong đợi: Specification được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật thông số sản phẩm
     */
    @Test
    public void testUpdateSpecification() {
        // Arrange - Create and save a specification
        Specification specification = new Specification();
        specification.setName("Screen Size");
        specification.setCode("SCREEN_SIZE_Update");
        specification.setDescription("Display dimensions");
        specification.setStatus(1);
        Specification savedSpecification = specificationRepository.save(specification);
        System.out.println("Input: Existing Specification [id=" + savedSpecification.getId() + 
                          "] with updates [name=Display Size, description=Updated display dimensions]");
        
        // Act - Update the specification
        savedSpecification.setName("Display Size");
        savedSpecification.setDescription("Updated display dimensions");
        Specification updatedSpecification = specificationRepository.save(savedSpecification);
        System.out.println("Expected Output: Updated Specification with new values [name=Display Size, description=Updated display dimensions]");
        
        // Assert
        assertEquals("Display Size", updatedSpecification.getName(), "Updated name should match");
        assertEquals("Updated display dimensions", updatedSpecification.getDescription(), "Updated description should match");
        assertEquals("SCREEN_SIZE_Update", updatedSpecification.getCode(), "Code should remain unchanged");
        assertEquals(1, updatedSpecification.getStatus(), "Status should remain unchanged");
    }
    
    /**
     * Test Case ID: SIT003
     * Tên test: testDeleteSpecification
     * Mục tiêu: Kiểm tra việc xóa thông số sản phẩm
     * Đầu vào: ID của thông số sản phẩm đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Thông số sản phẩm bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa thông số sản phẩm
     */
    @Test
    public void testDeleteSpecification() {
        // Arrange - Create and save a specification
        Specification specification = new Specification();
        specification.setName("Screen Size");
        specification.setCode("SCREEN_SIZE_Delete");
        specification.setDescription("Display dimensions");
        specification.setStatus(1);
        Specification savedSpecification = specificationRepository.save(specification);
        Long specificationId = savedSpecification.getId();
        System.out.println("Input: Specification ID to delete = " + specificationId);
        
        // Act - Delete the specification
        specificationRepository.deleteById(specificationId);
        System.out.println("Expected Output: Specification with ID = " + specificationId + " no longer exists in database");
        
        // Assert
        Optional<Specification> deletedSpecification = specificationRepository.findById(specificationId);
        assertFalse(deletedSpecification.isPresent(), "Specification should be deleted");
    }
    
    /**
     * Test Case ID: SIT004
     * Tên test: testGetAllSpecifications
     * Mục tiêu: Kiểm tra việc lấy danh sách tất cả thông số sản phẩm
     * Đầu vào: Hai thông số sản phẩm đã được lưu trong cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách thông số sản phẩm chứa ít nhất 2 bản ghi
     * Ghi chú: Kiểm tra chức năng lấy tất cả thông số sản phẩm
     */
    @Test
    public void testGetAllSpecifications() {
        // Arrange - Create and save multiple specifications
        Specification specification1 = new Specification();
        specification1.setName("Screen Size");
        specification1.setCode("SCREEN_SIZE_GetAll");
        specification1.setDescription("Display dimensions");
        specification1.setStatus(1);
        specificationRepository.save(specification1);
        
        Specification specification2 = new Specification();
        specification2.setName("RAM");
        specification2.setCode("RAM_GetAll");
        specification2.setDescription("Memory capacity");
        specification2.setStatus(1);
        specificationRepository.save(specification2);
        System.out.println("Input: Two specifications saved to database");
        
        // Act - Get all specifications
        List<Specification> specifications = specificationRepository.findAll();
        System.out.println("Expected Output: List containing at least 2 specifications");
        
        // Assert
        assertTrue(specifications.size() >= 2, "Should have at least 2 specifications");
        assertTrue(specifications.stream().anyMatch(spec -> "SCREEN_SIZE_GetAll".equals(spec.getCode())),
                  "Should contain specification with code SCREEN_SIZE");
        assertTrue(specifications.stream().anyMatch(spec -> "RAM_GetAll".equals(spec.getCode())),
                  "Should contain specification with code RAM");
    }
    
    /**
     * Test Case ID: SIT005
     * Tên test: testGetSpecificationById
     * Mục tiêu: Kiểm tra việc lấy thông số sản phẩm theo ID
     * Đầu vào: ID của thông số sản phẩm đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Thông số sản phẩm được tìm thấy với ID tương ứng
     * Ghi chú: Kiểm tra chức năng tìm kiếm theo ID
     */
    @Test
    public void testGetSpecificationById() {
        // Arrange - Create and save a specification
        Specification specification = new Specification();
        specification.setName("Screen Size");
        specification.setCode("SCREEN_SIZE_GetById");
        specification.setDescription("Display dimensions");
        specification.setStatus(1);
        Specification savedSpecification = specificationRepository.save(specification);
        Long specificationId = savedSpecification.getId();
        System.out.println("Input: Specification ID to find = " + specificationId);
        
        // Act - Find the specification by ID
        Optional<Specification> foundSpecification = specificationRepository.findById(specificationId);
        System.out.println("Expected Output: Specification found with matching ID");
        
        // Assert
        assertTrue(foundSpecification.isPresent(), "Specification should be found");
        assertEquals(specificationId, foundSpecification.get().getId(), "Found specification ID should match");
        assertEquals("Screen Size", foundSpecification.get().getName(), "Specification name should match");
        assertEquals("SCREEN_SIZE_GetById", foundSpecification.get().getCode(), "Specification code should match");
    }
    
    /**
     * Test Case ID: SIT006
     * Tên test: testSpecificationCodeUniqueness
     * Mục tiêu: Kiểm tra tính duy nhất của trường code trong thông số sản phẩm
     * Đầu vào: Hai thông số sản phẩm cùng mã code
     * Đầu ra mong đợi: Exception về vi phạm ràng buộc duy nhất
     * Ghi chú: Kiểm tra ràng buộc duy nhất của trường code
     */
    @Test
    public void testSpecificationCodeUniqueness() {
        // Arrange - Create and save a specification
        Specification specification1 = new Specification();
        specification1.setName("Screen Size");
        specification1.setCode("DUPLICATE_CODE");
        specification1.setDescription("Display dimensions");
        specification1.setStatus(1);
        specificationRepository.save(specification1);
        System.out.println("Input: First Specification with code=DUPLICATE_CODE, then Second Specification with same code");
        
        // Create another specification with the same code
        Specification specification2 = new Specification();
        specification2.setName("Different Name");
        specification2.setCode("DUPLICATE_CODE");
        specification2.setDescription("Different description");
        specification2.setStatus(1);
        
        // Act & Assert - Expect an exception when saving with duplicate code
        Exception exception = assertThrows(Exception.class, () -> {
            specificationRepository.saveAndFlush(specification2);
        });
        System.out.println("Expected Output: Exception thrown due to unique constraint violation");
        
        // Additional verification
        assertTrue(exception.getMessage().contains("constraint") || 
                  exception.getCause().getMessage().contains("constraint"), 
                  "Exception should be related to constraint violation");
    }
    
    /**
     * Test Case ID: SIT007
     * Tên test: testDeactivateSpecification
     * Mục tiêu: Kiểm tra việc thay đổi trạng thái của thông số sản phẩm thành không hoạt động
     * Đầu vào: Thông số sản phẩm có status=1 (hoạt động)
     * Đầu ra mong đợi: Thông số sản phẩm được cập nhật với status=0 (không hoạt động)
     * Ghi chú: Kiểm tra chức năng thay đổi trạng thái
     */
    @Test
    public void testDeactivateSpecification() {
        // Arrange - Create and save a specification with active status
        Specification specification = new Specification();
        specification.setName("Screen Size");
        specification.setCode("SCREEN_SIZE_Deactivate");
        specification.setDescription("Display dimensions");
        specification.setStatus(1); // Active status
        Specification savedSpecification = specificationRepository.save(specification);
        System.out.println("Input: Active Specification [status=1] to be deactivated [status=0]");
        
        // Act - Deactivate the specification
        savedSpecification.setStatus(0); // Inactive status
        Specification updatedSpecification = specificationRepository.save(savedSpecification);
        System.out.println("Expected Output: Specification with updated status=0 (inactive)");
        
        // Assert
        assertEquals(0, updatedSpecification.getStatus(), "Specification status should be updated to inactive (0)");
    }
}
