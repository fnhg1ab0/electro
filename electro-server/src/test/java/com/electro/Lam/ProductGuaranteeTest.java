package com.electro.Lam;

import com.electro.entity.product.Guarantee;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.client.WishRepository;
import com.electro.repository.general.ImageRepository;
import com.electro.repository.inventory.*;
import com.electro.repository.order.OrderVariantRepository;
import com.electro.repository.product.GuaranteeRepository;
import com.electro.repository.product.ProductRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Guarantee entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductGuaranteeTest {
    
    @Autowired
    private GuaranteeRepository guaranteeRepository;

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
    private TagRepository tagRepository;

    @Autowired
    private ProductInventoryLimitRepository productInventoryLimitRepository;
    
    @BeforeEach
    public void setUp() {
        // Clear existing data before each test
        wishRepository.deleteAll();
        preorderRepository.deleteAll();
        reviewRepository.deleteAll();

        // Clear variants and associated entities
        cartVariantRepository.deleteAll();
        orderVariantRepository.deleteAll();
        countVariantRepository.deleteAll();
        docketVariantRepository.deleteAll();
        purchaseOrderVariantRepository.deleteAll();

        // Clear inventory related entities
        storageLocationRepository.deleteAll();
        variantInventoryLimitRepository.deleteAll();

        // Clear product inventory limits
        productInventoryLimitRepository.deleteAll();

        // Clear images
        imageRepository.deleteAll();

        // Clear promotion products (join tables for many-to-many relationships)
        promotionRepository.findAll().forEach(promotion -> {
            promotion.getProducts().clear();
            promotionRepository.save(promotion);
        });

        // Clear variants
        variantRepository.deleteAll();

        // Clear product_tag join table
        productRepository.findAll().forEach(product -> {
            product.getTags().clear();
            productRepository.save(product);
        });

        // Now we can safely clear products
        productRepository.deleteAll();
        guaranteeRepository.deleteAll();

        tagRepository.deleteAll();
    }
    
    /**
     * Test Case ID: GIT001
     * Tên test: testCreateGuarantee
     * Mục tiêu: Kiểm tra việc tạo mới bảo hành sản phẩm
     * Đầu vào: Một đối tượng Guarantee với name="12-Month Warranty", 
     *          description="Covers all manufacturing defects", status=1
     * Đầu ra mong đợi: Guarantee được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo bảo hành
     */
    @Test
    public void testCreateGuarantee() {
        // Arrange
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1); // Active status
        System.out.println("Input: Guarantee [name=12-Month Warranty, description=Covers all manufacturing defects, status=1]");
        
        // Act
        Guarantee savedGuarantee = guaranteeRepository.save(guarantee);
        System.out.println("Expected Output: Saved Guarantee with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedGuarantee.getId(), "Saved guarantee ID should not be null");
        assertEquals("12-Month Warranty", savedGuarantee.getName(), "Guarantee name should match");
        assertEquals("Covers all manufacturing defects", savedGuarantee.getDescription(), "Guarantee description should match");
        assertEquals(1, savedGuarantee.getStatus(), "Guarantee status should match");
    }
    
    /**
     * Test Case ID: GIT002
     * Tên test: testUpdateGuarantee
     * Mục tiêu: Kiểm tra việc cập nhật thông tin bảo hành sản phẩm
     * Đầu vào: Bảo hành đã tồn tại với name="12-Month Warranty" và cập nhật thành name="24-Month Warranty",
     *          description="Extended warranty for 24 months"
     * Đầu ra mong đợi: Guarantee được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật bảo hành
     */
    @Test
    public void testUpdateGuarantee() {
        // Arrange - Create and save a guarantee
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1);
        Guarantee savedGuarantee = guaranteeRepository.save(guarantee);
        System.out.println("Input: Existing Guarantee [id=" + savedGuarantee.getId() + 
                          "] with updates [name=24-Month Warranty, description=Extended warranty for 24 months]");
        
        // Act - Update the guarantee
        savedGuarantee.setName("24-Month Warranty");
        savedGuarantee.setDescription("Extended warranty for 24 months");
        Guarantee updatedGuarantee = guaranteeRepository.save(savedGuarantee);
        System.out.println("Expected Output: Updated Guarantee with new values [name=24-Month Warranty, description=Extended warranty for 24 months]");
        
        // Assert
        assertEquals("24-Month Warranty", updatedGuarantee.getName(), "Updated name should match");
        assertEquals("Extended warranty for 24 months", updatedGuarantee.getDescription(), "Updated description should match");
    }
    
    /**
     * Test Case ID: GIT003
     * Tên test: testDeleteGuarantee
     * Mục tiêu: Kiểm tra việc xóa bảo hành sản phẩm
     * Đầu vào: ID của bảo hành đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Bảo hành bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa bảo hành
     */
    @Test
    public void testDeleteGuarantee() {
        // Arrange - Create and save a guarantee
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1);
        Guarantee savedGuarantee = guaranteeRepository.save(guarantee);
        Long guaranteeId = savedGuarantee.getId();
        System.out.println("Input: Guarantee ID to delete = " + guaranteeId);
        
        // Act - Delete the guarantee
        guaranteeRepository.deleteById(guaranteeId);
        System.out.println("Expected Output: Guarantee with ID = " + guaranteeId + " no longer exists in database");
        
        // Assert
        Optional<Guarantee> deletedGuarantee = guaranteeRepository.findById(guaranteeId);
        assertFalse(deletedGuarantee.isPresent(), "Guarantee should be deleted");
    }
    
    /**
     * Test Case ID: GIT004
     * Tên test: testGetAllGuarantees
     * Mục tiêu: Kiểm tra việc lấy tất cả bảo hành sản phẩm
     * Đầu vào: Hai bảo hành "12-Month Warranty" và "24-Month Warranty" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các bảo hành có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả bảo hành
     */
    @Test
    public void testGetAllGuarantees() {
        // Arrange - Create and save two guarantees
        Guarantee guarantee1 = new Guarantee();
        guarantee1.setName("12-Month Warranty");
        guarantee1.setDescription("Covers all manufacturing defects");
        guarantee1.setStatus(1);
        
        Guarantee guarantee2 = new Guarantee();
        guarantee2.setName("24-Month Warranty");
        guarantee2.setDescription("Extended warranty for 24 months");
        guarantee2.setStatus(1);
        
        guaranteeRepository.save(guarantee1);
        guaranteeRepository.save(guarantee2);
        System.out.println("Input: Database with two guarantees - '12-Month Warranty' and '24-Month Warranty'");
        
        // Act
        List<Guarantee> guarantees = guaranteeRepository.findAll();
        System.out.println("Expected Output: List containing 2 guarantees");
        
        // Assert
        assertEquals(2, guarantees.size(), "There should be 2 guarantees");
    }
    
    /**
     * Test Case ID: GIT005
     * Tên test: testGetGuaranteeById
     * Mục tiêu: Kiểm tra việc tìm kiếm bảo hành theo ID
     * Đầu vào: ID của bảo hành đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy bảo hành với name="12-Month Warranty"
     * Ghi chú: Kiểm tra chức năng tìm kiếm bảo hành theo ID
     */
    @Test
    public void testGetGuaranteeById() {
        // Arrange - Create and save a guarantee
        Guarantee guarantee = new Guarantee();
        guarantee.setName("12-Month Warranty");
        guarantee.setDescription("Covers all manufacturing defects");
        guarantee.setStatus(1);
        Guarantee savedGuarantee = guaranteeRepository.save(guarantee);
        Long guaranteeId = savedGuarantee.getId();
        System.out.println("Input: Search for guarantee with id=" + guaranteeId);
        
        // Act
        Optional<Guarantee> foundGuarantee = guaranteeRepository.findById(guaranteeId);
        System.out.println("Expected Output: Found guarantee with name='12-Month Warranty'");
        
        // Assert
        assertTrue(foundGuarantee.isPresent(), "Guarantee should exist");
        assertEquals("12-Month Warranty", foundGuarantee.get().getName(), "Guarantee name should match");
        assertEquals("Covers all manufacturing defects", foundGuarantee.get().getDescription(), "Guarantee description should match");
    }
    
    /**
     * Test Case ID: GIT006
     * Tên test: testGetGuaranteeById_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm bảo hành với ID không tồn tại
     * Đầu vào: ID không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy bảo hành nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệu
     */
    @Test
    public void testGetGuaranteeById_NotFound() {
        // Non-existent ID (assuming no guarantee with ID 999 exists)
        Long nonExistingId = 999L;
        System.out.println("Input: Search for guarantee with non-existent id=" + nonExistingId);
        
        // Act
        Optional<Guarantee> foundGuarantee = guaranteeRepository.findById(nonExistingId);
        System.out.println("Expected Output: No guarantee found (empty Optional)");
        
        // Assert
        assertFalse(foundGuarantee.isPresent(), "Guarantee should not exist");
    }
}
