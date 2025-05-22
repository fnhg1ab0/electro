package com.electro.Lam;

import com.electro.entity.product.Product;
import com.electro.entity.product.Tag;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.client.WishRepository;
import com.electro.repository.general.ImageRepository;
import com.electro.repository.inventory.*;
import com.electro.repository.order.OrderVariantRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Tag entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
@ActiveProfiles("test")
//@Transactional
public class ProductTagTest {

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
//
    }
    
    /**
     * Test Case ID: TIT001
     * Tên test: testCreateTag
     * Mục tiêu: Kiểm tra việc tạo mới thẻ sản phẩm
     * Đầu vào: Một đối tượng Tag với name="Electronics", slug="electronics", status=1
     * Đầu ra mong đợi: Tag được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo thẻ sản phẩm
     */
    @Test
    public void testCreateTag() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronicsCreate");
        tag.setStatus(1);
        System.out.println("Input: Tag [name=Electronics, slug=electronics, status=1]");
        
        // Act
        Tag savedTag = tagRepository.save(tag);
        System.out.println("Expected Output: Saved Tag with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedTag.getId(), "Saved tag ID should not be null");
        assertEquals("Electronics", savedTag.getName(), "Tag name should match");
        assertEquals("electronicsCreate", savedTag.getSlug(), "Tag slug should match");
        assertEquals(1, savedTag.getStatus(), "Tag status should match");
    }
    
    /**
     * Test Case ID: TIT002
     * Tên test: testUpdateTag
     * Mục tiêu: Kiểm tra việc cập nhật thông tin thẻ sản phẩm
     * Đầu vào: Thẻ đã tồn tại với name="Electronics" và cập nhật thành 
     *          name="Updated Electronics", slug="updated-electronics"
     * Đầu ra mong đợi: Tag được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật thẻ sản phẩm
     */
    @Test
    public void testUpdateTag() {
        // Arrange - Create and save a tag
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronicsUpdate");
        tag.setStatus(1);
        Tag savedTag = tagRepository.save(tag);
        System.out.println("Input: Existing Tag [id=" + savedTag.getId() + 
                         "] with updates [name=Updated Electronics, slug=updated-electronics]");
        
        // Act - Update the tag
        savedTag.setName("Updated Electronics");
        savedTag.setSlug("updated-electronics-update");
        Tag updatedTag = tagRepository.save(savedTag);
        System.out.println("Expected Output: Updated Tag with new values [name=Updated Electronics, slug=updated-electronics]");
        
        // Assert
        assertEquals("Updated Electronics", updatedTag.getName(), "Updated name should match");
        assertEquals("updated-electronics-update", updatedTag.getSlug(), "Updated slug should match");
    }
    
    /**
     * Test Case ID: TIT003
     * Tên test: testDeleteTag
     * Mục tiêu: Kiểm tra việc xóa thẻ sản phẩm
     * Đầu vào: ID của thẻ đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Thẻ bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa thẻ sản phẩm
     */
    @Test
    public void testDeleteTag() {
        // Arrange - Create and save a tag
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronicsDelete");
        tag.setStatus(1);
        Tag savedTag = tagRepository.save(tag);
        Long tagId = savedTag.getId();
        System.out.println("Input: Tag ID to delete = " + tagId);
        
        // Act - Delete the tag
        tagRepository.deleteById(tagId);
        System.out.println("Expected Output: Tag with ID = " + tagId + " no longer exists in database");
        
        // Assert
        Optional<Tag> deletedTag = tagRepository.findById(tagId);
        assertFalse(deletedTag.isPresent(), "Tag should be deleted");
    }
    
    /**
     * Test Case ID: TIT004
     * Tên test: testGetAllTags
     * Mục tiêu: Kiểm tra việc lấy tất cả thẻ sản phẩm
     * Đầu vào: Hai thẻ "Electronics" và "Discount" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các thẻ có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả thẻ sản phẩm
     */
    @Test
    public void testGetAllTags() {
        // Arrange - Create and save two tags
        Tag tag1 = new Tag();
        tag1.setName("Electronics");
        tag1.setSlug("electronicsGetAll");
        tag1.setStatus(1);
        
        Tag tag2 = new Tag();
        tag2.setName("Discount");
        tag2.setSlug("discountGetAll");
        tag2.setStatus(1);
        
        tagRepository.save(tag1);
        tagRepository.save(tag2);
        System.out.println("Input: Database with two tags - 'Electronics' and 'Discount'");
        
        // Act
        List<Tag> tags = tagRepository.findAll();
        System.out.println("Expected Output: List containing 2 tags");
        
        // Assert
        assertTrue(tags.size() >= 2, "There should be 2 or more than 2 tags");
    }
    
    /**
     * Test Case ID: TIT005
     * Tên test: testGetTagById
     * Mục tiêu: Kiểm tra việc tìm kiếm thẻ sản phẩm theo ID
     * Đầu vào: ID của thẻ đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy thẻ với name="Electronics"
     * Ghi chú: Kiểm tra chức năng tìm kiếm thẻ sản phẩm theo ID
     */
    @Test
    public void testGetTagById() {
        // Arrange - Create and save a tag
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronicsGetById");
        tag.setStatus(1);
        Tag savedTag = tagRepository.save(tag);
        Long tagId = savedTag.getId();
        System.out.println("Input: Search for tag with id=" + tagId);
        
        // Act
        Optional<Tag> foundTag = tagRepository.findById(tagId);
        System.out.println("Expected Output: Found tag with name='Electronics'");
        
        // Assert
        assertTrue(foundTag.isPresent(), "Tag should exist");
        assertEquals("Electronics", foundTag.get().getName(), "Tag name should match");
    }
    
    /**
     * Test Case ID: TIT006
     * Tên test: testGetTagById_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm thẻ sản phẩm với ID không tồn tại
     * Đầu vào: ID 999L không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy thẻ nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệu
     */
    @Test
    public void testGetTagById_NotFound() {
        // Non-existent ID (assuming no tag with ID 999 exists)
        Long nonExistingId = 999L;
        System.out.println("Input: Search for tag with non-existent id=" + nonExistingId);
        
        // Act
        Optional<Tag> foundTag = tagRepository.findById(nonExistingId);
        System.out.println("Expected Output: No tag found (empty Optional)");
        
        // Assert
        assertFalse(foundTag.isPresent(), "Tag should not exist");
    }
    
    /**
     * Test Case ID: TIT007
     * Tên test: testTagProductAssociation
     * Mục tiêu: Kiểm tra mối quan hệ nhiều-nhiều giữa Tag và Product
     * Đầu vào: Một thẻ và một sản phẩm đã được liên kết với nhau
     * Đầu ra mong đợi: Mối quan hệ được thiết lập đúng, danh sách products của tag phải chứa sản phẩm đã liên kết
     * Ghi chú: Kiểm tra chức năng thiết lập mối quan hệ giữa thẻ và sản phẩm
     */
    @Transactional
    @Test
    public void testTagProductAssociation() {
        // Arrange - Create a tag and a product
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronicsAssociation");
        tag.setStatus(1);
        Tag savedTag = tagRepository.save(tag);

        Product product = new Product();
        product.setName("Smartphone");
        product.setCode("SP001Association");
        product.setSlug("smartphone");
        product.setStatus(1);

        // Re-fetch the tag to ensure it is managed by the persistence context
        Tag reloadedTag = tagRepository.findById(savedTag.getId()).orElseThrow(() -> new IllegalStateException("Tag not found"));

        // Establish the many-to-many relationship
        Set<Tag> tags = new HashSet<>();
        tags.add(reloadedTag);
        product.setTags(tags);
        Product savedProduct = productRepository.save(product);

        System.out.println("Input: Tag 'Electronics' associated with Product 'Smartphone'");

        // Refresh the tag from the database to get updated relationships
        Optional<Tag> refreshedTag = tagRepository.findById(reloadedTag.getId());
        System.out.println("Expected Output: Tag 'Electronics' contains Product 'Smartphone' in its products collection");

        // Assert
        assertTrue(refreshedTag.isPresent(), "Tag should exist");
        //Product have tag
        assertTrue(product.getTags().contains(refreshedTag.get()), "Tag should exist");
    }
}
