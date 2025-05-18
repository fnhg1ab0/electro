package com.electro.Lam;

import com.electro.entity.product.Category;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.client.WishRepository;
import com.electro.repository.general.ImageRepository;
import com.electro.repository.inventory.CountVariantRepository;
import com.electro.repository.inventory.DocketVariantRepository;
import com.electro.repository.inventory.ProductInventoryLimitRepository;
import com.electro.repository.inventory.PurchaseOrderVariantRepository;
import com.electro.repository.inventory.StorageLocationRepository;
import com.electro.repository.inventory.VariantInventoryLimitRepository;
import com.electro.repository.order.OrderVariantRepository;
import com.electro.repository.product.CategoryRepository;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductCategoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

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
    private ProductRepository productRepository;
    
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
        // Xóa dữ liệu cũ để đảm bảo môi trường sạch - Clean up in proper order to respect foreign key constraints
        // First clear associations and dependent entities
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
        
        // Finally, clear categories and tags
        categoryRepository.deleteAll();
        tagRepository.deleteAll();
    }
    /**
     * Test Case ID: CIT001
     * Tên test: testCreateCategory
     * Mục tiêu: Kiểm tra việc tạo mới danh mục sản phẩm
     * Đầu vào: Một đối tượng Category với name="Electronics", slug="electronics", 
     *          description="Category for electronic products", status=1
     * Đầu ra mong đợi: Category được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo danh mục
     */
    @Test
    public void testCreateCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        System.out.println("Input: Category [name=Electronics, slug=electronics, description=Category for electronic products, status=1]");

        // Act
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Expected Output: Saved Category with non-null ID and matching attributes");

        // Assert
        assertNotNull(savedCategory.getId(), "Saved category ID should not be null");
        assertEquals("Electronics", savedCategory.getName(), "Category name should match");
        assertEquals("electronics", savedCategory.getSlug(), "Category slug should match");
        assertEquals("Category for electronic products", savedCategory.getDescription(), "Category description should match");
        assertEquals(1, savedCategory.getStatus(), "Category status should match");
    }    /**
     * Test Case ID: CIT002
     * Tên test: testUpdateCategory
     * Mục tiêu: Kiểm tra việc cập nhật thông tin danh mục sản phẩm
     * Đầu vào: Danh mục đã tồn tại với name="Electronics" và cập nhật thành name="Updated Electronics",
     *          description="Updated description"
     * Đầu ra mong đợi: Category được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật danh mục
     */
    @Test
    public void testUpdateCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Input: Existing Category [id=" + savedCategory.getId() + 
                          "] with updates [name=Updated Electronics, description=Updated description]");

        // Act
        savedCategory.setName("Updated Electronics");
        savedCategory.setDescription("Updated description");
        Category updatedCategory = categoryRepository.save(savedCategory);
        System.out.println("Expected Output: Updated Category with new values [name=Updated Electronics, description=Updated description]");

        // Assert
        assertEquals("Updated Electronics", updatedCategory.getName(), "Updated name should match");
        assertEquals("Updated description", updatedCategory.getDescription(), "Updated description should match");
        assertEquals(1, updatedCategory.getStatus(), "Category status should match");
    }    /**
     * Test Case ID: CIT003
     * Tên test: testDeleteCategory
     * Mục tiêu: Kiểm tra việc xóa danh mục sản phẩm
     * Đầu vào: ID của danh mục đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Danh mục bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa danh mục
     */
    @Test
    public void testDeleteCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Input: Category ID to delete = " + savedCategory.getId());

        // Act
        categoryRepository.deleteById(savedCategory.getId());
        System.out.println("Expected Output: Category with ID = " + savedCategory.getId() + " no longer exists in database");

        // Assert
        Optional<Category> deletedCategory = categoryRepository.findById(savedCategory.getId());
        assertFalse(deletedCategory.isPresent(), "Category should be deleted");
    }    /**
     * Test Case ID: CIT004
     * Tên test: testFindAllCategories
     * Mục tiêu: Kiểm tra việc lấy tất cả danh mục sản phẩm
     * Đầu vào: Hai danh mục "Electronics" và "Furniture" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các danh mục có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả danh mục
     */
    @Test
    public void testFindAllCategories() {
        // Arrange
        Category category1 = new Category();
        category1.setName("Electronics");
        category1.setSlug("electronics");
        category1.setDescription("Category for electronic products");
        category1.setStatus(1); // Thiết lập giá trị cho status

        Category category2 = new Category();
        category2.setName("Furniture");
        category2.setSlug("furniture");
        category2.setDescription("Category for furniture products");
        category2.setStatus(1); // Thiết lập giá trị cho status

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        System.out.println("Input: Database with two categories - 'Electronics' and 'Furniture'");

        // Act
        List<Category> categories = categoryRepository.findAll();
        System.out.println("Expected Output: List containing 2 categories");

        // Assert
        assertEquals(2, categories.size(), "There should be 2 categories");
    }    /**
     * Test Case ID: CIT005
     * Tên test: testFindCategoryBySlug
     * Mục tiêu: Kiểm tra việc tìm kiếm danh mục theo slug
     * Đầu vào: Slug "electronics" của danh mục đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy danh mục với name="Electronics"
     * Ghi chú: Kiểm tra chức năng tìm kiếm danh mục theo slug
     */
    @Test
    public void testFindCategoryBySlug() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        categoryRepository.save(category);
        System.out.println("Input: Search for category with slug='electronics'");

        // Act
        Optional<Category> foundCategory = categoryRepository.findBySlug("electronics");
        System.out.println("Expected Output: Found category with name='Electronics'");

        // Assert
        assertTrue(foundCategory.isPresent(), "Category should exist");
        assertEquals("Electronics", foundCategory.get().getName(), "Category name should match");
    }    /**
     * Test Case ID: CIT006
     * Tên test: testFindCategoryBySlug_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm danh mục với slug không tồn tại
     * Đầu vào: Slug "non-existent-slug" không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy danh mục nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệu
     */
    @Test
    public void testFindCategoryBySlug_NotFound() {
        // Act
        System.out.println("Input: Search for category with non-existent slug='non-existent-slug'");
        Optional<Category> foundCategory = categoryRepository.findBySlug("non-existent-slug");
        System.out.println("Expected Output: No category found (empty Optional)");

        // Assert
        assertFalse(foundCategory.isPresent(), "Category should not exist");
    }    /**
     * Test Case ID: CIT007
     * Tên test: testFindCategoriesWithNoParent
     * Mục tiêu: Kiểm tra việc tìm kiếm danh mục không có danh mục cha
     * Đầu vào: Một danh mục cha "Parent Category" và một danh mục con "Child Category" 
     *          có tham chiếu đến danh mục cha
     * Đầu ra mong đợi: Danh sách chứa một danh mục (Parent Category) không có danh mục cha
     * Ghi chú: Kiểm tra chức năng tìm kiếm danh mục theo mối quan hệ phân cấp
     */
    @Test
    public void testFindCategoriesWithNoParent() {
        // Arrange
        Category parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setSlug("parent-category");
        parentCategory.setDescription("Parent category");
        parentCategory.setStatus(1); // Thiết lập giá trị cho status
        categoryRepository.save(parentCategory);

        Category childCategory = new Category();
        childCategory.setName("Child Category");
        childCategory.setSlug("child-category");
        childCategory.setDescription("Child category");
        childCategory.setStatus(1); // Thiết lập giá trị cho status
        childCategory.setParentCategory(parentCategory);
        categoryRepository.save(childCategory);
        System.out.println("Input: Database with one parent category 'Parent Category' and one child category 'Child Category'");

        // Act
        List<Category> categoriesWithoutParent = categoryRepository.findByParentCategoryIsNull();
        System.out.println("Expected Output: List containing only 1 category ('Parent Category') with no parent");

        // Assert
        assertEquals(1, categoriesWithoutParent.size(), "There should be 1 category without a parent");
        assertEquals("Parent Category", categoriesWithoutParent.get(0).getName(), "Category name should match");
    }
}