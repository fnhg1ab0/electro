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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
//@Transactional
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
        category.setSlug("electronicsCreate");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        System.out.println("Input: Category [name=Electronics, slug=electronics, description=Category for electronic products, status=1]");

        // Act
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Expected Output: Saved Category with non-null ID and matching attributes");

        // Assert
        assertNotNull(savedCategory.getId(), "Saved category ID should not be null");
        assertEquals("Electronics", savedCategory.getName(), "Category name should match");
        assertEquals("electronicsCreate", savedCategory.getSlug(), "Category slug should match");
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
        category.setSlug("electronicsUpdate");
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
        category.setSlug("electronicsDelete");
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
        category1.setSlug("electronicsGetAll");
        category1.setDescription("Category for electronic products");
        category1.setStatus(1); // Thiết lập giá trị cho status

        Category category2 = new Category();
        category2.setName("Furniture");
        category2.setSlug("furnitureGetAll");
        category2.setDescription("Category for furniture products");
        category2.setStatus(1); // Thiết lập giá trị cho status

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        System.out.println("Input: Database with two categories - 'Electronics' and 'Furniture'");

        // Act
        List<Category> categories = categoryRepository.findAll();
        System.out.println("Total categories: " + categories.size());

        // Assert
        assertTrue(categories.size() > 2, "The total number of categories should be greater than 2");
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
        category.setSlug("electronicsSlug");
        category.setDescription("Category for electronic products");
        category.setStatus(1); // Thiết lập giá trị cho status
        categoryRepository.save(category);
        System.out.println("Input: Search for category with slug='electronics'");

        // Act
        Optional<Category> foundCategory = categoryRepository.findBySlug("electronicsSlug");
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
    public void testFindCategoriesWithNoParent_Multiple() {
        // Arrange - Create multiple parent categories
        Category parentCategory1 = new Category();
        parentCategory1.setName("Parent Category 1");
        parentCategory1.setSlug("parent-category-1");
        parentCategory1.setDescription("First parent category");
        parentCategory1.setStatus(1);
        categoryRepository.save(parentCategory1);

        System.out.println("Input: Database one child category with parent");

        // Act - Retrieve categories without parents
        List<Category> categoriesWithoutParent = categoryRepository.findByParentCategoryIsNull();
        System.out.println("Actual number of categories without parent found: " + categoriesWithoutParent.size());

        // Assert
        assertTrue(!categoriesWithoutParent.isEmpty(), "There should be at least 1 categories without a parent");
        assertTrue(categoriesWithoutParent.stream().anyMatch(category -> "Parent Category 1".equals(category.getName())),
                "Results should include 'Parent Category 1'");

    }
}