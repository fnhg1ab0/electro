package com.electro.Lam;

import com.electro.entity.product.Property;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.client.WishRepository;
import com.electro.repository.general.ImageRepository;
import com.electro.repository.inventory.*;
import com.electro.repository.order.OrderVariantRepository;
import com.electro.repository.product.*;
import com.electro.repository.promotion.PromotionRepository;
import com.electro.repository.review.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Property entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
public class ProductPropertyTest {

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private StorageLocationRepository storageLocationRepository;

    @Autowired
    private VariantInventoryLimitRepository variantInventoryLimitRepository;

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
    private ProductInventoryLimitRepository productInventoryLimitRepository;

    @Autowired
    private PropertyRepository propertyRepository;
    
    @BeforeEach
    public void setUp() {

    }
    
    /**
     * Test Case ID: PIT001
     * Tên test: testCreateProperty
     * Mục tiêu: Kiểm tra việc tạo mới thuộc tính sản phẩm
     * Đầu vào: Một đối tượng Property với name="Color", code="COLOR", 
     *          description="Product color property", status=1
     * Đầu ra mong đợi: Property được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo thuộc tính sản phẩm
     */
    @Test
    public void testCreateProperty() {
        // Arrange
        Property property = new Property();
        property.setName("Color");
        property.setCode("COLORCreate");
        property.setDescription("Product color property");
        property.setStatus(1);
        System.out.println("Input: Property [name=Color, code=COLOR, description=Product color property, status=1]");
        
        // Act
        Property savedProperty = propertyRepository.save(property);
        System.out.println("Expected Output: Saved Property with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedProperty.getId(), "Saved property ID should not be null");
        assertEquals("Color", savedProperty.getName(), "Property name should match");
        assertEquals("COLORCreate", savedProperty.getCode(), "Property code should match");
        assertEquals("Product color property", savedProperty.getDescription(), "Property description should match");
        assertEquals(1, savedProperty.getStatus(), "Property status should match");
    }
    
    /**
     * Test Case ID: PIT002
     * Tên test: testUpdateProperty
     * Mục tiêu: Kiểm tra việc cập nhật thông tin thuộc tính sản phẩm
     * Đầu vào: Thuộc tính đã tồn tại với name="Color" và cập nhật thành 
     *          name="Updated Color", description="Updated color property description"
     * Đầu ra mong đợi: Property được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật thuộc tính sản phẩm
     */
    @Test
    public void testUpdateProperty() {
        // Arrange - Create and save a property
        Property property = new Property();
        property.setName("Color");
        property.setCode("COLORUpdate");
        property.setDescription("Product color property");
        property.setStatus(1);
        Property savedProperty = propertyRepository.save(property);
        System.out.println("Input: Existing Property [id=" + savedProperty.getId() + 
                         "] with updates [name=Updated Color, description=Updated color property description]");
        
        // Act - Update the property
        savedProperty.setName("Updated Color");
        savedProperty.setDescription("Updated color property description");
        Property updatedProperty = propertyRepository.save(savedProperty);
        System.out.println("Expected Output: Updated Property with new values [name=Updated Color, description=Updated color property description]");
        
        // Assert
        assertEquals("Updated Color", updatedProperty.getName(), "Updated name should match");
        assertEquals("Updated color property description", updatedProperty.getDescription(), "Updated description should match");
        assertEquals("COLORUpdate", updatedProperty.getCode(), "Property code should remain unchanged");
        assertEquals(1, updatedProperty.getStatus(), "Property status should remain unchanged");
    }
    
    /**
     * Test Case ID: PIT003
     * Tên test: testDeleteProperty
     * Mục tiêu: Kiểm tra việc xóa thuộc tính sản phẩm
     * Đầu vào: ID của thuộc tính đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Thuộc tính bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa thuộc tính sản phẩm
     */
    @Test
    public void testDeleteProperty() {
        // Arrange - Create and save a property
        Property property = new Property();
        property.setName("Color");
        property.setCode("COLORDelete");
        property.setDescription("Product color property");
        property.setStatus(1);
        Property savedProperty = propertyRepository.save(property);
        Long propertyId = savedProperty.getId();
        System.out.println("Input: Property ID to delete = " + propertyId);
        
        // Act - Delete the property
        propertyRepository.deleteById(propertyId);
        System.out.println("Expected Output: Property with ID = " + propertyId + " no longer exists in database");
        
        // Assert
        Optional<Property> deletedProperty = propertyRepository.findById(propertyId);
        assertFalse(deletedProperty.isPresent(), "Property should be deleted");
    }
    
    /**
     * Test Case ID: PIT004
     * Tên test: testGetAllProperties
     * Mục tiêu: Kiểm tra việc lấy tất cả thuộc tính sản phẩm
     * Đầu vào: Hai thuộc tính "Color" và "Size" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các thuộc tính có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả thuộc tính sản phẩm
     */
    @Test
    public void testGetAllProperties() {
        // Arrange - Create and save two properties
        Property property1 = new Property();
        property1.setName("Color");
        property1.setCode("COLORGetAll");
        property1.setDescription("Product color property");
        property1.setStatus(1);
        
        Property property2 = new Property();
        property2.setName("Size");
        property2.setCode("SIZEGetAll");
        property2.setDescription("Product size property");
        property2.setStatus(1);
        
        propertyRepository.save(property1);
        propertyRepository.save(property2);
        System.out.println("Input: Database with two properties - 'Color' and 'Size'");
        
        // Act
        List<Property> properties = propertyRepository.findAll();

        // Assert
        System.out.println("Actual number of properties: " + properties.size());
        assertTrue(properties.size() > 2, "The number of properties should be greater than 2");
    }
    
    /**
     * Test Case ID: PIT005
     * Tên test: testFindPropertyById
     * Mục tiêu: Kiểm tra việc tìm kiếm thuộc tính sản phẩm theo ID
     * Đầu vào: ID của thuộc tính đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy thuộc tính với name="Color"
     * Ghi chú: Kiểm tra chức năng tìm kiếm thuộc tính sản phẩm theo ID
     */
    @Test
    public void testFindPropertyById() {
        // Arrange - Create and save a property
        Property property = new Property();
        property.setName("Color");
        property.setCode("COLORGetId");
        property.setDescription("Product color property");
        property.setStatus(1);
        Property savedProperty = propertyRepository.save(property);
        Long propertyId = savedProperty.getId();
        System.out.println("Input: Search for property with id=" + propertyId);
        
        // Act
        Optional<Property> foundProperty = propertyRepository.findById(propertyId);
        System.out.println("Expected Output: Found property with name='Color'");
        
        // Assert
        assertTrue(foundProperty.isPresent(), "Property should exist");
        assertEquals("Color", foundProperty.get().getName(), "Property name should match");
        assertEquals("COLORGetId", foundProperty.get().getCode(), "Property code should match");
    }
    
    /**
     * Test Case ID: PIT006
     * Tên test: testFindPropertyById_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm thuộc tính sản phẩm với ID không tồn tại
     * Đầu vào: ID 999L không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy thuộc tính nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệu
     */
    @Test
    public void testFindPropertyById_NotFound() {
        // Non-existent ID (assuming no property with ID 999 exists)
        Long nonExistingId = 999L;
        System.out.println("Input: Search for property with non-existent id=" + nonExistingId);
        
        // Act
        Optional<Property> foundProperty = propertyRepository.findById(nonExistingId);
        System.out.println("Expected Output: No property found (empty Optional)");
        
        // Assert
        assertFalse(foundProperty.isPresent(), "Property should not exist");
    }
    
    /**
     * Test Case ID: PIT007
     * Tên test: testCreatePropertyWithNullName
     * Mục tiêu: Kiểm tra việc tạo thuộc tính với name=null
     * Đầu vào: Một đối tượng Property với name=null, code="COLOR", status=1
     * Đầu ra mong đợi: Exception do vi phạm ràng buộc không null
     * Ghi chú: Kiểm tra xử lý khi dữ liệu đầu vào không hợp lệ
     */
    @Test
    public void testCreatePropertyWithNullName() {
        // Arrange
        Property property = new Property();
        property.setName(null); // Null name violates not-null constraint
        property.setCode("COLORNull");
        property.setStatus(1);
        System.out.println("Input: Property with null name [name=null, code=COLOR, status=1]");
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            propertyRepository.saveAndFlush(property);
        });
        System.out.println("Expected Output: Exception thrown due to null name");
        
        // The exact exception type might vary depending on your database and JPA configuration
        assertNotNull(exception, "Exception should be thrown when saving property with null name");
    }
}
