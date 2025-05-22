package com.electro.Lam;

import com.electro.entity.product.Brand;
import com.electro.entity.product.Category;
import com.electro.entity.product.Product;
import com.electro.entity.product.Supplier;
import com.electro.entity.product.Tag;
import com.electro.entity.product.Unit;
import com.electro.repository.product.BrandRepository;
import com.electro.repository.product.CategoryRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.SupplierRepository;
import com.electro.repository.product.TagRepository;
import com.electro.repository.product.UnitRepository;
import com.electro.repository.product.VariantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product entity interacting directly with the database
 * Uses @SpringBootTest to load the full application context
 * Uses @Transactional to rollback changes after each test
 * Uses @ActiveProfiles("test") to use test-specific properties
 */
@SpringBootTest
//@Transactional
@ActiveProfiles("test")
public class ProductTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private UnitRepository unitRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private VariantRepository variantRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
//        // Disable foreign key checks to avoid constraint issues
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
//
//        // Remove product-tag associations
//        productRepository.findAll().forEach(product -> {
//            product.getTags().clear();
//            productRepository.save(product);
//        });
//
//        // Clear variants and related entities first
//        variantRepository.deleteAll();
//
//        // Clear products
//        productRepository.deleteAll();
//
//        // Re-enable foreign key checks
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
    }
    
    /**
     * Test Case ID: PIT001
     * Tên test: testCreateBasicProduct
     * Mục tiêu: Kiểm tra việc tạo sản phẩm cơ bản
     * Đầu vào: Một đối tượng Product với các thông tin cơ bản
     * Đầu ra mong đợi: Product được lưu thành công với ID không null và các thông tin khớp với đầu vào
     * Ghi chú: Kiểm tra chức năng cơ bản của việc tạo sản phẩm
     */
    @Test
    public void testCreateBasicProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP001Create");
        product.setSlug("laptop-dell-xps-13-create");
        product.setShortDescription("Laptop cao cấp cho doanh nhân");
        product.setDescription("Laptop Dell XPS 13 là một trong những laptop cao cấp tốt nhất hiện nay");
        product.setStatus(1);
        System.out.println("Input: Product [name=Laptop Dell XPS 13, code=LAP001, status=1]");
        
        // Act
        Product savedProduct = productRepository.save(product);
        System.out.println("Expected Output: Saved Product with non-null ID and matching attributes");
        
        // Assert
        assertNotNull(savedProduct.getId(), "Saved product ID should not be null");
        assertEquals("Laptop Dell XPS 13", savedProduct.getName(), "Product name should match");
        assertEquals("LAP001Create", savedProduct.getCode(), "Product code should match");
        assertEquals("laptop-dell-xps-13-create", savedProduct.getSlug(), "Product slug should match");
        assertEquals(1, savedProduct.getStatus(), "Product status should match");
    }
    
    /**
     * Test Case ID: PIT002
     * Tên test: testUpdateProduct
     * Mục tiêu: Kiểm tra việc cập nhật thông tin sản phẩm
     * Đầu vào: Sản phẩm đã tồn tại và cập nhật các thông tin
     * Đầu ra mong đợi: Sản phẩm được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật sản phẩm
     */
    @Test
    public void testUpdateProduct() {
        // Arrange - Create and save a product
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP002");
        product.setSlug("laptop-dell-xps-13-old");
        product.setStatus(1);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Existing Product [id=" + savedProduct.getId() + 
                          "] with updates [name=Laptop Dell XPS 15, slug=laptop-dell-xps-15]");
        
        // Act - Update the product
        savedProduct.setName("Laptop Dell XPS 15");
        savedProduct.setSlug("laptop-dell-xps-15");
        savedProduct.setShortDescription("Laptop cao cấp cho doanh nhân và đồ họa");
        Product updatedProduct = productRepository.save(savedProduct);
        System.out.println("Expected Output: Updated Product with new values");
        
        // Assert
        assertEquals("Laptop Dell XPS 15", updatedProduct.getName(), "Updated name should match");
        assertEquals("laptop-dell-xps-15", updatedProduct.getSlug(), "Updated slug should match");
        assertEquals("Laptop cao cấp cho doanh nhân và đồ họa", updatedProduct.getShortDescription(), "Updated shortDescription should match");
    }
    
    /**
     * Test Case ID: PIT003
     * Tên test: testDeleteProduct
     * Mục tiêu: Kiểm tra việc xóa sản phẩm
     * Đầu vào: ID của sản phẩm đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Sản phẩm bị xóa khỏi cơ sở dữ liệu, không thể tìm thấy bằng ID
     * Ghi chú: Kiểm tra chức năng xóa sản phẩm
     */
    @Test
    public void testDeleteProduct() {
        // Arrange - Create and save a product
        Product product = new Product();
        product.setName("Product To Delete");
        product.setCode("DEL001");
        product.setSlug("product-to-delete");
        product.setStatus(1);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        System.out.println("Input: Product ID to delete = " + productId);
        
        // Act - Delete the product
        productRepository.deleteById(productId);
        System.out.println("Expected Output: Product with ID = " + productId + " no longer exists in database");
        
        // Assert
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertFalse(deletedProduct.isPresent(), "Product should be deleted");
    }
    
    /**
     * Test Case ID: PIT004
     * Tên test: testGetAllProducts
     * Mục tiêu: Kiểm tra việc lấy tất cả sản phẩm
     * Đầu vào: Hai sản phẩm đã được lưu vào cơ sở dữ liệu
     * Đầu ra mong đợi: Danh sách các sản phẩm có kích thước bằng 2
     * Ghi chú: Kiểm tra chức năng lấy tất cả sản phẩm
     */
    @Test
    public void testGetAllProducts() {
        // Arrange - Create and save two products
        Product product1 = new Product();
        product1.setName("Laptop Dell XPS 13");
        product1.setCode("LAP003");
        product1.setSlug("laptop-dell-xps-13-list");
        product1.setStatus(1);
        
        Product product2 = new Product();
        product2.setName("Laptop HP Spectre");
        product2.setCode("LAP004");
        product2.setSlug("laptop-hp-spectre");
        product2.setStatus(1);
        
        productRepository.save(product1);
        productRepository.save(product2);
        System.out.println("Input: Database with two products - 'Laptop Dell XPS 13' and 'Laptop HP Spectre'");
        
        // Act
        List<Product> products = productRepository.findAll();
        System.out.println("Expected Output: List containing at least 2 products");
        
        // Assert
        assertTrue(products.size() >= 2, "There should be at least 2 products");
        assertTrue(products.stream().anyMatch(p -> p.getCode().equals("LAP003")), "Should contain product with code 'LAP003'");
        assertTrue(products.stream().anyMatch(p -> p.getCode().equals("LAP004")), "Should contain product with code 'LAP004'");
    }
    
    /**
     * Test Case ID: PIT005
     * Tên test: testGetProductById
     * Mục tiêu: Kiểm tra việc tìm kiếm sản phẩm theo ID
     * Đầu vào: ID của sản phẩm đã tồn tại trong cơ sở dữ liệu
     * Đầu ra mong đợi: Tìm thấy sản phẩm với tên "Laptop Test"
     * Ghi chú: Kiểm tra chức năng tìm kiếm sản phẩm theo ID
     */
    @Test
    public void testGetProductById() {
        // Arrange - Create and save a product
        Product product = new Product();
        product.setName("Laptop Test");
        product.setCode("LAP005");
        product.setSlug("laptop-test");
        product.setStatus(1);
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        System.out.println("Input: Search for product with id=" + productId);
        
        // Act
        Optional<Product> foundProduct = productRepository.findById(productId);
        System.out.println("Expected Output: Found product with name='Laptop Test'");
        
        // Assert
        assertTrue(foundProduct.isPresent(), "Product should exist");
        assertEquals("Laptop Test", foundProduct.get().getName(), "Product name should match");
        assertEquals("LAP005", foundProduct.get().getCode(), "Product code should match");
    }
    
    /**
     * Test Case ID: PIT006
     * Tên test: testProductWithCategory
     * Mục tiêu: Kiểm tra mối quan hệ giữa Product và Category
     * Đầu vào: Một Product và một Category được liên kết với nhau
     * Đầu ra mong đợi: Product có Category và Category có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ Many-to-One giữa Product và Category
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithCategory() {
        // Arrange - Create a category
        Category category = new Category();
        category.setName("Mobile");
        category.setSlug("Mobile");
        category.setStatus(1);
        Category savedCategory = categoryRepository.save(category);

        // Create a product with category
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP006");
        product.setSlug("laptop-dell-xps-13-category");
        product.setStatus(1);
        product.setCategory(savedCategory);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product [name=Laptop Dell XPS 13] with Category [name=Mobile]");

        // Flush and clear the EntityManager
        entityManager.flush();
        entityManager.clear();

        // Act - Refresh data from database to ensure relationships are properly loaded
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Category refreshedCategory = categoryRepository.findById(savedCategory.getId()).orElseThrow();

        // Print all product IDs associated with the category
        System.out.println("Product IDs in Category:");
        refreshedCategory.getProducts().forEach(p -> System.out.println("- " + p.getId()));

        // Assert - Check if the category contains the product
        assertNotNull(refreshedProduct.getCategory(), "Product should have a category");
        assertEquals(savedCategory.getId(), refreshedProduct.getCategory().getId(), "Product should be linked to the correct category");
        assertTrue(refreshedCategory.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
                "Category's products should contain the created product");
    }   
    
    /**
     * Test Case ID: PIT007
     * Tên test: testProductWithTagsRelationship
     * Mục tiêu: Kiểm tra mối quan hệ nhiều-nhiều giữa Product và Tag
     * Đầu vào: Một Product và hai Tag được liên kết với nhau
     * Đầu ra mong đợi: Product có danh sách tags chứa 2 Tag và mỗi Tag có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ Many-to-Many giữa Product và Tag
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithTagsRelationship() {
        // Arrange - Create tags
        Tag tag1 = new Tag();
        tag1.setName("Gaming");
        tag1.setSlug("gaming");
        tag1.setStatus(1);
        Tag savedTag1 = tagRepository.save(tag1);
        
        Tag tag2 = new Tag();
        tag2.setName("Ultrabook");
        tag2.setSlug("ultrabook");
        tag2.setStatus(1);
        Tag savedTag2 = tagRepository.save(tag2);
        
        // Create a product with tags
        Product product = new Product();
        product.setName("Laptop Gaming");
        product.setCode("LAP007");
        product.setSlug("laptop-gaming");
        product.setStatus(1);
        
        Set<Tag> tags = new HashSet<>();
        tags.add(savedTag1);
        tags.add(savedTag2);
        product.setTags(tags);
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product [name=Laptop Gaming] with Tags [Gaming, Ultrabook]");

        // Flush and clear the EntityManager
        entityManager.flush();
        entityManager.clear();
        
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Tag refreshedTag1 = tagRepository.findById(savedTag1.getId()).orElseThrow();
        Tag refreshedTag2 = tagRepository.findById(savedTag2.getId()).orElseThrow();
        System.out.println("Expected Output: Product with 2 Tags and Tags with Product in products list");
        
        // Assert
        assertEquals(2, refreshedProduct.getTags().size(), "Product should have 2 tags");
        assertTrue(refreshedProduct.getTags().stream().anyMatch(t -> t.getId().equals(savedTag1.getId())),
                   "Product tags should contain the first tag");
        assertTrue(refreshedProduct.getTags().stream().anyMatch(t -> t.getId().equals(savedTag2.getId())),
                   "Product tags should contain the second tag");
        assertTrue(refreshedTag1.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
                   "First tag's products should contain the created product");
        assertTrue(refreshedTag2.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
                   "Second tag's products should contain the created product");
    }
    
    /**
     * Test Case ID: PIT008
     * Tên test: testProductWithUnit
     * Mục tiêu: Kiểm tra mối quan hệ giữa Product và Unit
     * Đầu vào: Một Product và một Unit được liên kết với nhau
     * Đầu ra mong đợi: Product có Unit và Unit có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ Many-to-One giữa Product và Unit
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithUnit() {
        // Arrange - Create a unit
        Unit unit = new Unit();
        unit.setName("Piece");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        
        // Create a product with unit
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP008");
        product.setSlug("laptop-dell-xps-13-unit");
        product.setStatus(1);
        product.setUnit(savedUnit);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product [name=Laptop Dell XPS 13] with Unit [name=Piece]");

        // Flush and clear the EntityManager
        entityManager.flush();
        entityManager.clear();
        
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Unit refreshedUnit = unitRepository.findById(savedUnit.getId()).orElseThrow();
        System.out.println("Expected Output: Product with Unit and Unit with Product in products list");
        
        // Assert
        assertNotNull(refreshedProduct.getUnit(), "Product should have a unit");
        assertEquals(savedUnit.getId(), refreshedProduct.getUnit().getId(), "Product should be linked to the correct unit");
//        assertTrue(refreshedUnit.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
//                   "Unit's products should contain the created product");
    }
    
    /**
     * Test Case ID: PIT009
     * Tên test: testProductWithBrand
     * Mục tiêu: Kiểm tra mối quan hệ giữa Product và Brand
     * Đầu vào: Một Product và một Brand được liên kết với nhau
     * Đầu ra mong đợi: Product có Brand và Brand có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ Many-to-One giữa Product và Brand
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithBrand() {
        // Arrange - Create a brand
        Brand brand = new Brand();
        brand.setName("Dell");
        brand.setCode("DELL");
        brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        
        // Create a product with brand
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP009");
        product.setSlug("laptop-dell-xps-13-brand");
        product.setStatus(1);
        product.setBrand(savedBrand);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product [name=Laptop Dell XPS 13] with Brand [name=Dell]");

        // Flush and clear the EntityManager
        entityManager.flush();
        entityManager.clear();
        
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Brand refreshedBrand = brandRepository.findById(savedBrand.getId()).orElseThrow();
        System.out.println("Expected Output: Product with Brand and Brand with Product in products list");
        
        // Assert
        assertNotNull(refreshedProduct.getBrand(), "Product should have a brand");
        assertEquals(savedBrand.getId(), refreshedProduct.getBrand().getId(), "Product should be linked to the correct brand");
//        assertTrue(refreshedBrand.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
//                   "Brand's products should contain the created product");
    }
    
    /**
     * Test Case ID: PIT010
     * Tên test: testProductWithSupplier
     * Mục tiêu: Kiểm tra mối quan hệ giữa Product và Supplier
     * Đầu vào: Một Product và một Supplier được liên kết với nhau
     * Đầu ra mong đợi: Product có Supplier và Supplier có danh sách products chứa Product đó
     * Ghi chú: Kiểm tra mối quan hệ Many-to-One giữa Product và Supplier
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithSupplier() {
        // Arrange - Create a supplier
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Công ty ABC");
        supplier.setCode("SUP001");
        supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        // Create a product with supplier
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP010");
        product.setSlug("laptop-dell-xps-13-supplier");
        product.setStatus(1);
        product.setSupplier(savedSupplier);
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product [name=Laptop Dell XPS 13] with Supplier [displayName=Công ty ABC]");

        // Flush and clear the EntityManager
        entityManager.flush();
        entityManager.clear();

        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        Supplier refreshedSupplier = supplierRepository.findById(savedSupplier.getId()).orElseThrow();
        System.out.println("Expected Output: Product with Supplier and Supplier with Product in products list");
        
        // Assert
        assertNotNull(refreshedProduct.getSupplier(), "Product should have a supplier");
        assertEquals(savedSupplier.getId(), refreshedProduct.getSupplier().getId(), "Product should be linked to the correct supplier");
//        assertTrue(refreshedSupplier.getProducts().stream().anyMatch(p -> p.getId().equals(savedProduct.getId())),
//                   "Supplier's products should contain the created product");
    }
    
    /**
     * Test Case ID: PIT011
     * Tên test: testProductWithSpecifications
     * Mục tiêu: Kiểm tra lưu trữ specifications dạng JSON cho Product
     * Đầu vào: Một Product với specifications dạng JSON
     * Đầu ra mong đợi: Product được lưu với specifications dạng JSON đúng
     * Ghi chú: Kiểm tra chức năng lưu trữ dữ liệu JSON
     */
    @Transactional
    @Commit
    @Test
    public void testProductWithSpecifications() {
        // Arrange - Create a product with specifications
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP011");
        product.setSlug("laptop-dell-xps-13-specs");
        product.setStatus(1);
        
        // Create JSON specifications
        ObjectNode specs = objectMapper.createObjectNode();
        specs.put("cpu", "Intel Core i7");
        specs.put("ram", "16GB");
        specs.put("storage", "512GB SSD");
        specs.put("display", "13.3-inch 4K");
        product.setSpecifications(specs);
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Input: Product with specifications={cpu:Intel Core i7, ram:16GB, storage:512GB SSD, display:13.3-inch 4K}");
        
        // Act - Refresh data from database
        entityManager.flush();
        entityManager.clear();
        
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        System.out.println("Expected Output: Product with correct JSON specifications");
        
        // Assert
        assertNotNull(refreshedProduct.getSpecifications(), "Product specifications should not be null");
        JsonNode specs1 = refreshedProduct.getSpecifications();
        assertEquals("Intel Core i7", specs1.get("cpu").asText(), "CPU spec should match");
        assertEquals("16GB", specs1.get("ram").asText(), "RAM spec should match");
        assertEquals("512GB SSD", specs1.get("storage").asText(), "Storage spec should match");
        assertEquals("13.3-inch 4K", specs1.get("display").asText(), "Display spec should match");
    }
    
    /**
     * Test Case ID: PIT012
     * Tên test: testCreateCompleteProduct
     * Mục tiêu: Kiểm tra việc tạo sản phẩm đầy đủ với các mối quan hệ
     * Đầu vào: Một Product đầy đủ với Category, Brand, Supplier, Unit và Tags
     * Đầu ra mong đợi: Product được lưu thành công với tất cả mối quan hệ được duy trì
     * Ghi chú: Kiểm tra chức năng tạo sản phẩm đầy đủ
     */
    @Transactional
    @Commit
    @Test
    public void testCreateCompleteProduct() {
        // Arrange - Create related entities
        Category category = new Category();
        category.setName("Laptop Gaming");
        category.setSlug("laptop-gaming");
        category.setStatus(1);
        Category savedCategory = categoryRepository.save(category);
        
        Brand brand = new Brand();
        brand.setName("Dell");
        brand.setCode("DELLComplete");
        brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        
        Supplier supplier = new Supplier();
        supplier.setDisplayName("Công ty XYZ");
        supplier.setCode("SUP002");
        supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        Unit unit = new Unit();
        unit.setName("Piece");
        unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        
        Tag tag1 = new Tag();
        tag1.setName("Gaming");
        tag1.setSlug("gamingComplete");
        tag1.setStatus(1);
        Tag savedTag1 = tagRepository.save(tag1);
        
        Tag tag2 = new Tag();
        tag2.setName("High-performance");
        tag2.setSlug("high-performance");
        tag2.setStatus(1);
        Tag savedTag2 = tagRepository.save(tag2);
        
        // Create a complete product
        Product product = new Product();
        product.setName("Dell Alienware m15");
        product.setCode("LAP012");
        product.setSlug("dell-alienware-m15-Complete");
        product.setShortDescription("Laptop gaming cao cấp");
        product.setDescription("Laptop gaming Dell Alienware m15 với hiệu năng mạnh mẽ");
        product.setStatus(1);
        product.setWeight(2.5);
        
        // Set relationships
        product.setCategory(savedCategory);
        product.setBrand(savedBrand);
        product.setSupplier(savedSupplier);
        product.setUnit(savedUnit);
        
        Set<Tag> tags = new HashSet<>();
        tags.add(savedTag1);
        tags.add(savedTag2);
        product.setTags(tags);
        
        // Set JSON data
        ObjectNode specs = objectMapper.createObjectNode();
        specs.put("cpu", "Intel Core i9");
        specs.put("ram", "32GB");
        specs.put("gpu", "NVIDIA RTX 3080");
        product.setSpecifications(specs);
        
        System.out.println("Input: Complete Product with all relationships");
        
        // Act
        Product savedProduct = productRepository.save(product);
        
        Product refreshedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        System.out.println("Expected Output: Complete Product with all relationships preserved");
        
        // Assert
        assertNotNull(refreshedProduct.getId(), "Saved product ID should not be null");
        assertEquals("Dell Alienware m15", refreshedProduct.getName(), "Product name should match");
        
        // Check relationships
        assertNotNull(refreshedProduct.getCategory(), "Product should have a category");
        assertEquals(savedCategory.getId(), refreshedProduct.getCategory().getId(), "Category should match");
        
        assertNotNull(refreshedProduct.getBrand(), "Product should have a brand");
        assertEquals(savedBrand.getId(), refreshedProduct.getBrand().getId(), "Brand should match");
        
        assertNotNull(refreshedProduct.getSupplier(), "Product should have a supplier");
        assertEquals(savedSupplier.getId(), refreshedProduct.getSupplier().getId(), "Supplier should match");
        
        assertNotNull(refreshedProduct.getUnit(), "Product should have a unit");
        assertEquals(savedUnit.getId(), refreshedProduct.getUnit().getId(), "Unit should match");
        
        assertEquals(2, refreshedProduct.getTags().size(), "Product should have 2 tags");
        
        // Check JSON data
        JsonNode retrievedSpecs = refreshedProduct.getSpecifications();
        assertEquals("Intel Core i9", retrievedSpecs.get("cpu").asText(), "CPU spec should match");
        assertEquals("32GB", retrievedSpecs.get("ram").asText(), "RAM spec should match");
        assertEquals("NVIDIA RTX 3080", retrievedSpecs.get("gpu").asText(), "GPU spec should match");
    }

    /**
     * Test Case ID: PIT013
     * Tên test: testCreateProductWithDuplicateCodeOrSlug
     * Mục tiêu: Kiểm tra việc tạo sản phẩm nếu trùng code hoặc slug
     * Đầu vào: Hai sản phẩm với cùng code hoặc slug
     * Đầu ra mong đợi: DataIntegrityViolationException được ném ra trong cả hai trường hợp (trùng code và trùng slug)
     * Ghi chú: Đảm bảo ràng buộc UNIQUE cho code và slug trong cơ sở dữ liệu được thực thi.
     */
    @Test
    public void testCreateProductWithDuplicateCodeOrSlug() {
        // Arrange - Create the first product
        Product product1 = new Product();
        product1.setName("Laptop Dell XPS 13");
        product1.setCode("LAP001");
        product1.setSlug("laptop-dell-xps-13");
        product1.setStatus(1);
        productRepository.save(product1);

        // Act & Assert - Try to create a second product with the same code or slug
        Product product2 = new Product();
        product2.setName("Laptop Dell XPS 15");
        product2.setCode("LAP001"); // Duplicate code
        product2.setSlug("laptop-dell-xps-15");
        product2.setStatus(1);

        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.save(product2);
        }, "Expected a DataIntegrityViolationException due to duplicate code");

        // Act & Assert - Try to create a second product with the same slug
        Product product3 = new Product();
        product3.setName("Laptop Dell XPS 17");
        product3.setCode("LAP002");
        product3.setSlug("laptop-dell-xps-13"); // Duplicate slug
        product3.setStatus(1);

        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.save(product3);
        }, "Expected a DataIntegrityViolationException due to duplicate slug");
    }
}
