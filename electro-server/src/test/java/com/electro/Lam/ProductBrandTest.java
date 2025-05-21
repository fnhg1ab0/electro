package com.electro.Lam;

import com.electro.entity.product.Brand;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
//@Transactional
public class ProductBrandTest {

    @Autowired
    private BrandRepository brandRepository;

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
        // Xóa dữ liệu cũ để đảm bảo môi trường sạch
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
//        // Clear inventory related entities
//        storageLocationRepository.deleteAll();
//        variantInventoryLimitRepository.deleteAll();
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
////        brandRepository.deleteAll();
//        tagRepository.deleteAll();
    }    /**
     * Test Case ID: BIT001
     * Tên test: testGetAllBrands
     * Mục tiêu: Kiểm tra việc lấy tất cả thương hiệu sản phẩm
     * Đầu vào: Hai thương hiệu "Samsung" và "Apple" đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các thương hiệu có ít nhất 2 thương hiệu
     * Ghi chú: Kiểm tra chức năng lấy tất cả thương hiệu
     */
    @Test
    public void testGetAllBrands() {
        // Arrange
        // Đảm bảo xóa dữ liệu trước khi thực hiện test

        Brand brand1 = new Brand();
        brand1.setName("Samsung");
        brand1.setCode("SAM123_TEST"); // Thêm hậu tố để đảm bảo code là duy nhất
        brand1.setDescription("Electronics brand");
        brand1.setStatus(1);
        brand1.setProducts(new ArrayList<>()); // Khởi tạo rõ ràng danh sách Products rỗng

        Brand brand2 = new Brand();
        brand2.setName("Apple");
        brand2.setCode("APL456_TEST"); // Thêm hậu tố để đảm bảo code là duy nhất
        brand2.setDescription("Technology brand");
        brand2.setStatus(1);
        brand2.setProducts(new ArrayList<>()); // Khởi tạo rõ ràng danh sách Products rỗng

        brandRepository.save(brand1);
        brandRepository.save(brand2);
        System.out.println("Input: Database with two brands - 'Samsung' and 'Apple'");

        // Act
        List<Brand> brands = brandRepository.findAll();
        System.out.println("Expected Output: List containing at least 2 brands");

        // Assert
        assertTrue(brands.size() >= 2, "There should be at least 2 brands");
    }    /**
     * Test Case ID: BIT002
     * Tên test: testCreateBrand
     * Mục tiêu: Kiểm tra việc tạo mới thương hiệu sản phẩm
     * Đầu vào: Một đối tượng Brand với name="Samsung", code="SAM123", 
     *          description="Electronics brand", status=1
     * Đầu ra mong đợi: Brand được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo thương hiệu
     */
    @Test
    public void testCreateBrand() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        brand.setDescription("Electronics brand");
        brand.setStatus(1);
        System.out.println("Input: Brand [name=Samsung, code=SAM123, description=Electronics brand, status=1]");

        // Act
        Brand savedBrand = brandRepository.save(brand);
        System.out.println("Expected Output: Saved Brand with non-null ID and matching attributes");

        // Assert
        assertNotNull(savedBrand.getId(), "Saved brand ID should not be null");
        assertEquals("Samsung", savedBrand.getName(), "Brand name should match");
        assertEquals("SAM123", savedBrand.getCode(), "Brand code should match");
        assertEquals("Electronics brand", savedBrand.getDescription(), "Brand description should match");
        assertEquals(1, savedBrand.getStatus(), "Brand status should match");
    }    /**
     * Test Case ID: BIT003
     * Tên test: testUpdateBrand
     * Mục tiêu: Kiểm tra việc cập nhật thông tin thương hiệu sản phẩm
     * Đầu vào: Thương hiệu đã tồn tại với name="Samsung" và cập nhật thành name="Updated Samsung",
     *          description="Updated description"
     * Đầu ra mong đợi: Brand được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật thương hiệu
     */
    @Test
    public void testUpdateBrand() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        brand.setDescription("Electronics brand");
        brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        System.out.println("Input: Existing Brand [id=" + savedBrand.getId() + 
                          "] with updates [name=Updated Samsung, description=Updated description]");

        // Act
        savedBrand.setName("Updated Samsung");
        savedBrand.setDescription("Updated description");
        Brand updatedBrand = brandRepository.save(savedBrand);
        System.out.println("Expected Output: Updated Brand with new values [name=Updated Samsung, description=Updated description]");

        // Assert
        assertEquals("Updated Samsung", updatedBrand.getName(), "Updated name should match");
        assertEquals("Updated description", updatedBrand.getDescription(), "Updated description should match");
    }    /**
     * Test Case ID: BIT004
     * Tên test: testDeleteBrand
     * Mục tiêu: Kiểm tra việc xóa thương hiệu sản phẩm
     * Đầu vào: ID của thương hiệu đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Thương hiệu bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa thương hiệu
     */
    @Test
    public void testDeleteBrand() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        brand.setDescription("Electronics brand");
        brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        System.out.println("Input: Brand ID to delete = " + savedBrand.getId());

        // Act
        brandRepository.deleteById(savedBrand.getId());
        System.out.println("Expected Output: Brand with ID = " + savedBrand.getId() + " no longer exists in database");

        // Assert
        Optional<Brand> deletedBrand = brandRepository.findById(savedBrand.getId());
        assertFalse(deletedBrand.isPresent(), "Brand should be deleted");
    }    /**
     * Test Case ID: BIT005
     * Tên test: testGetBrandByIdb
     * Mục tiêu: Kiểm tra việc tìm kiếm thương hiệu theo ID
     * Đầu vào: ID của thương hiệu đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy thương hiệu với name="Samsung"
     * Ghi chú: Kiểm tra chức năng tìm kiếm thương hiệu theo ID
     */
    @Test
    public void testGetBrandById() {
        // Arrange
        Brand brand = new Brand();
        brand.setName("Samsung");
        brand.setCode("SAM123");
        brand.setDescription("Electronics brand");
        brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        System.out.println("Input: Search for brand with id=" + savedBrand.getId());

        // Act
        Optional<Brand> foundBrand = brandRepository.findById(savedBrand.getId());
        System.out.println("Expected Output: Found brand with name=" + foundBrand.get().getName());

        // Assert
        assertTrue(foundBrand.isPresent(), "Brand should exist");
        assertEquals("Samsung", foundBrand.get().getName(), "Brand name should match");
    }    /**
     * Test Case ID: BIT006
     * Tên test: testGetBrandById_NotFound
     * Mục tiêu: Kiểm tra việc tìm kiếm thương hiệu với ID không tồn tại
     * Đầu vào: ID 999L không tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Không tìm thấy thương hiệu nào (empty Optional)
     * Ghi chú: Kiểm tra xử lý khi không tìm thấy dữ liệuBỈ
     */
    @Test
    public void testGetBrandById_NotFound() {
        // Act
        System.out.println("Input: Search for brand with non-existent id=999");
        Optional<Brand> foundBrand = brandRepository.findById(999L);
        System.out.println("Expected Output: No brand found (empty Optional)");

        // Assert
        assertFalse(foundBrand.isPresent(), "Brand should not exist");
    }

    }

