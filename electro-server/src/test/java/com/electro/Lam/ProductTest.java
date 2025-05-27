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
 * Uses @ActiveProfiles("test") for test-specific configuration
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProductTest {
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private UnitRepository unitRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private VariantRepository variantRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Setup or cleanup before each test if needed
    }

    /**
     * Test Case ID: PIT001
     * Tên test: testCreateBasicProduct
     * Mục tiêu: Kiểm tra việc tạo sản phẩm cơ bản
     * Đầu vào: Một Product với các thông tin cơ bản (name, code, slug, shortDescription, description, status)
     * Đầu ra mong đợi: Product được lưu thành công với ID không null và các thông tin khớp
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
        System.out.println("Input: ProductName" + product.getName() + ", code=" + product.getCode() + ", slug=" + product.getSlug() + ", status=" + product.getStatus());

        // Act
        Product savedProduct = productRepository.save(product);
        System.out.println("Expected Output: Product saved with ID=" + savedProduct.getId() + ", name=" + savedProduct.getName() + ", code=" + savedProduct.getCode() + ", slug=" + savedProduct.getSlug() + ", status=" + savedProduct.getStatus());

        // Assert
        assertNotNull(savedProduct.getId());
        assertEquals("Laptop Dell XPS 13", savedProduct.getName());
        assertEquals("LAP001Create", savedProduct.getCode());
        assertEquals("laptop-dell-xps-13-create", savedProduct.getSlug());
        assertEquals(1, savedProduct.getStatus());
    }

    /**
     * Test Case ID: PIT002
     * Tên test: testUpdateProduct
     * Mục tiêu: Kiểm tra việc cập nhật thông tin sản phẩm
     * Đầu vào: Sản phẩm đã tồn tại và cập nhật các thông tin (name, slug, shortDescription)
     * Đầu ra mong đợi: Sản phẩm được cập nhật thành công với thông tin mới
     * Ghi chú: Kiểm tra chức năng cập nhật sản phẩm
     */
    @Test
    public void testUpdateProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Laptop Dell XPS 13");
        product.setCode("LAP002");
        product.setSlug("laptop-dell-xps-13-old");
        product.setStatus(1);
        Product initial = productRepository.save(product);
        System.out.println("Input: existingId=" + initial.getId() + ", updates=[name=Laptop Dell XPS 15, slug=laptop-dell-xps-15, shortDescription=...]");

        // Act
        initial.setName("Laptop Dell XPS 15");
        initial.setSlug("laptop-dell-xps-15");
        initial.setShortDescription("Laptop cao cấp cho doanh nhân và đồ họa");
        Product updated = productRepository.save(initial);
        System.out.println("Expected Output: id=" + updated.getId() + ", name = " + updated.getName() + ", slug = " + updated.getSlug() + ", shortDescription = " + updated.getShortDescription());

        // Assert
        assertEquals("Laptop Dell XPS 15", updated.getName());
        assertEquals("laptop-dell-xps-15", updated.getSlug());
        assertEquals("Laptop cao cấp cho doanh nhân và đồ họa", updated.getShortDescription());
    }

    /**
     * Test Case ID: PIT003
     * Tên test: testDeleteProduct
     * Mục tiêu: Kiểm tra việc xóa sản phẩm
     * Đầu vào: ID của sản phẩm đã tồn tại
     * Đầu ra mong đợi: Product không còn tồn tại
     * Ghi chú: Kiểm tra chức năng xóa sản phẩm
     */
    @Test
    public void testDeleteProduct() {
        // Arrange
        Product product = new Product();
        product.setName("Product To Delete");
        product.setCode("DEL001");
        product.setSlug("product-to-delete");
        product.setStatus(1);
        Product saved = productRepository.save(product);
        System.out.println("Input: deleteId=" + saved.getId());

        // Act
        productRepository.deleteById(saved.getId());
        System.out.println("Expected Output: findById(" + saved.getId() + ") returns empty");

        // Assert
        assertFalse(productRepository.findById(saved.getId()).isPresent());
    }

    /**
     * Test Case ID: PIT004
     * Tên test: testGetAllProducts
     * Mục tiêu: Kiểm tra việc lấy tất cả sản phẩm
     * Đầu vào: Hai sản phẩm với code LAP003, LAP004
     * Đầu ra mong đợi: Danh sách >=2 và chứa các code trên
     * Ghi chú: Kiểm tra chức năng lấy danh sách
     */
    @Test
    public void testGetAllProducts() {
        // Arrange
        Product p1 = new Product(); p1.setName("Laptop Dell XPS 13"); p1.setCode("LAP003"); p1.setSlug("laptop-dell-xps-13-list"); p1.setStatus(1);
        Product p2 = new Product(); p2.setName("Laptop HP Spectre"); p2.setCode("LAP004"); p2.setSlug("laptop-hp-spectre"); p2.setStatus(1);
        productRepository.save(p1);
        productRepository.save(p2);
        System.out.println("Input: preSavedCodes=[LAP003, LAP004]");

        // Act
        List<Product> list = productRepository.findAll();
        System.out.println("Expected Output: size=" + list.size() + ", contains LAP003=" + list.stream().anyMatch(p -> "LAP003".equals(p.getCode())));

        // Assert
        assertTrue(list.size() >= 2);
        assertTrue(list.stream().anyMatch(p -> "LAP003".equals(p.getCode())));
        assertTrue(list.stream().anyMatch(p -> "LAP004".equals(p.getCode())));
    }

    /**
     * Test Case ID: PIT005
     * Tên test: testGetProductById
     * Mục tiêu: Kiểm tra tìm kiếm sản phẩm theo ID
     * Đầu vào: ID sản phẩm
     * Đầu ra mong đợi: tìm thấy và thông tin khớp
     * Ghi chú: Kiểm tra chức năng tìm kiếm
     */
    @Test
    public void testGetProductById() {
        // Arrange
        Product product = new Product(); product.setName("Laptop Test"); product.setCode("LAP005"); product.setSlug("laptop-test"); product.setStatus(1);
        Product saved = productRepository.save(product);
        System.out.println("Input: searchId=" + saved.getId());

        // Act
        Optional<Product> found = productRepository.findById(saved.getId());
        System.out.println("Expected Output: found.isPresent=" + found.isPresent() + ", name=" + (found.isPresent() ? found.get().getName() : "not found") + ", code=" + (found.isPresent() ? found.get().getCode() : "not found"));

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Laptop Test", found.get().getName());
        assertEquals("LAP005", found.get().getCode());
    }

    /**
     * Test Case ID: PIT006
     * Tên test: testProductWithCategory
     * Mục tiêu: Kiểm tra mối quan hệ Product-Category
     * Đầu vào: một Product và một Category
     * Đầu ra mong đợi: liên kết đúng hai bên
     * Ghi chú: Quan hệ Many-to-One
     */
     @Test
    public void testProductWithCategory() {
        // Arrange
        Category cat = new Category(); cat.setName("Mobile"); cat.setSlug("mobile"); cat.setStatus(1);
        Category savedCat = categoryRepository.save(cat);
        Product product = new Product(); product.setName("Laptop Dell XPS 13"); product.setCode("LAP006"); product.setSlug("laptop-dell-xps-13-category"); product.setStatus(1);
        product.setCategory(savedCat);
        Product saved = productRepository.save(product);
        System.out.println("Input: productId = " + saved.getId() + ", categoryId = " + savedCat.getId() + ", categoryName = " + savedCat.getName() + ", productName = " + saved.getName());


        // Act
        entityManager.flush(); entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        Category refCat = categoryRepository.findById(savedCat.getId()).orElseThrow();
        System.out.println("Expected Output: refProd.category.id=" + refProd.getCategory().getId() + ", categoryName=" + refCat.getName());

        // Assert
        assertEquals(savedCat.getId(), refProd.getCategory().getId());
        assertTrue(refCat.getProducts().stream().anyMatch(p -> p.getId().equals(saved.getId())));
    }

    /**
     * Test Case ID: PIT007
     * Tên test: testProductWithTagsRelationship
     * Mục tiêu: Kiểm tra mối quan hệ Product-Tag
     * Đầu vào: một Product và hai Tag
     * Đầu ra mong đợi: liên kết đúng hai bên
     * Ghi chú:
     */
     @Test
    public void testProductWithTagsRelationship() {
        // Arrange
        Tag t1 = new Tag(); t1.setName("Gaming"); t1.setSlug("gaming"); t1.setStatus(1);
        Tag t2 = new Tag(); t2.setName("Ultrabook"); t2.setSlug("ultrabook"); t2.setStatus(1);
        Tag saved1 = tagRepository.save(t1);
        Tag saved2 = tagRepository.save(t2);
        Product product = new Product(); product.setName("Laptop Gaming"); product.setCode("LAP007"); product.setSlug("laptop-gaming"); product.setStatus(1);
        product.setTags(new HashSet<>(Set.of(saved1, saved2)));
        Product saved = productRepository.save(product);
        System.out.println("Input: productId=" + saved.getId() + ", tagIds=[" + saved1.getId() + "," + saved2.getId() + "] " + "ProductName=" + saved.getName() + ", Tags=[" + saved1.getName() + "," + saved2.getName() + "]");

        // Act
        entityManager.flush(); entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        Tag ref1 = tagRepository.findById(saved1.getId()).orElseThrow();
        Tag ref2 = tagRepository.findById(saved2.getId()).orElseThrow();
        System.out.println("Expected Output: ProductName: " + refProd.getName() + " Tags.size=" + refProd.getTags().size());

        // Assert
        assertEquals(2, refProd.getTags().size());
        assertTrue(ref1.getProducts().stream().anyMatch(p -> p.getId().equals(saved.getId())));
        assertTrue(ref2.getProducts().stream().anyMatch(p -> p.getId().equals(saved.getId())));
    }

    /**
     * Test Case ID: PIT008
     * Tên test: testProductWithUnit
     * Mục tiêu: Kiểm tra mối quan hệ Product-Unit
     * Đầu vào: một Product và một Unit
     * Đầu ra mong đợi: liên kết đúng hai bên
     * Ghi chú: Quan hệ Many-to-One
     */
     @Test
    public void testProductWithUnit() {
        // Arrange
        Unit unit = new Unit(); unit.setName("Piece"); unit.setStatus(1);
        Unit savedUnit = unitRepository.save(unit);
        Product product = new Product(); product.setName("Laptop Dell XPS 13"); product.setCode("LAP008"); product.setSlug("laptop-dell-xps-13-unit"); product.setStatus(1);
        product.setUnit(savedUnit);
        Product saved = productRepository.save(product);
        System.out.println("Input: productId=" + saved.getId() + ", unitId=" + savedUnit.getId() + " ProductName=" + saved.getName() + ", UnitName=" + savedUnit.getName());

        // Act
        entityManager.flush(); entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        System.out.println("Expected Output: ProductName: " + refProd.getName() + "UnitName" + refProd.getUnit().getName());

        // Assert
        assertEquals(savedUnit.getId(), refProd.getUnit().getId());
    }

    /**
     * Test Case ID: PIT009
     * Tên test: testProductWithBrand
     * Mục tiêu: Kiểm tra mối quan hệ Product-Brand
     * Đầu vào: một Product và một Brand
     * Đầu ra mong đợi: liên kết đúng hai bên
     * Ghi chú: Quan hệ Many-to-One
     */
     @Test
    public void testProductWithBrand() {
        // Arrange
        Brand brand = new Brand(); brand.setName("Dell"); brand.setCode("DELL"); brand.setStatus(1);
        Brand savedBrand = brandRepository.save(brand);
        Product product = new Product(); product.setName("Laptop Dell XPS 13"); product.setCode("LAP009"); product.setSlug("laptop-dell-xps-13-brand"); product.setStatus(1);
        product.setBrand(savedBrand);
        Product saved = productRepository.save(product);
        System.out.println("Input: productId=" + saved.getId() + ", brandId=" + savedBrand.getId() + ", ProductName=" + saved.getName() + ", BrandName=" + savedBrand.getName());

        // Act
        entityManager.flush();	entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        System.out.println("Expected Output: ProductName: " + refProd.getName() + ", BrandName: " + refProd.getBrand().getName() + ", BrandId: " + refProd.getBrand().getId());

        // Assert
        assertEquals(savedBrand.getId(), refProd.getBrand().getId());
    }

    /**
     * Test Case ID: PIT010
     * Tên test: testProductWithSupplier
     * Mục tiêu: Kiểm tra mối quan hệ Product-Supplier
     * Đầu vào: một Product và một Supplier
     * Đầu ra mong đợi: liên kết đúng hai bên
     * Ghi chú: Quan hệ Many-to-One
     */
     @Test
    public void testProductWithSupplier() {
        // Arrange
        Supplier supplier = new Supplier(); supplier.setDisplayName("Công ty ABC"); supplier.setCode("SUP001"); supplier.setStatus(1);
        Supplier savedSupplier = supplierRepository.save(supplier);
        Product product = new Product(); product.setName("Laptop Dell XPS 13"); product.setCode("LAP010"); product.setSlug("laptop-dell-xps-13-supplier"); product.setStatus(1);
        product.setSupplier(savedSupplier);
        Product saved = productRepository.save(product);
        System.out.println("Input: ProductName = " + saved.getName() + ", SupplierName = " + savedSupplier.getDisplayName() + ", productId = " + saved.getId() + ", supplierId = " + savedSupplier.getId());

        // Act
        entityManager.flush(); entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        System.out.println("Expected Output: ProductName: " + refProd.getName() + ", SupplierName: " + refProd.getSupplier().getDisplayName() + ", SupplierId: " + refProd.getSupplier().getId());

        // Assert
        assertEquals(savedSupplier.getId(), refProd.getSupplier().getId());
    }

    /**
     * Test Case ID: PIT011
     * Tên test: testProductWithSpecifications
     * Mục tiêu: Kiểm tra lưu trữ specifications dạng JSON
     * Đầu vào: một Product với specifications JSON
     * Đầu ra mong đợi: JSON lưu chính xác
     * Ghi chú: Kiểm tra lưu trữ JSON
     */
     @Test
    public void testProductWithSpecifications() {
        // Arrange
        Product product = new Product(); product.setName("Laptop Dell XPS 13"); product.setCode("LAP011"); product.setSlug("laptop-dell-xps-13-specs"); product.setStatus(1);
        ObjectNode specs = objectMapper.createObjectNode(); specs.put("cpu","Intel Core i7"); specs.put("ram","16GB"); specs.put("storage","512GB SSD"); specs.put("display","13.3-inch 4K");
        product.setSpecifications(specs);
        Product saved = productRepository.save(product);
        System.out.println("Input: productId=" + saved.getId() + ", specs=" + specs.toString());

        // Act
        entityManager.flush(); entityManager.clear();
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        JsonNode actual = refProd.getSpecifications();
        System.out.println("Expected Output: ProductName: " + refProd.getName() + " cpu=" + actual.get("cpu").asText() +
                           ", ram=" + actual.get("ram").asText() + ", storage=" + actual.get("storage").asText() +
                           ", display=" + actual.get("display").asText());

        // Assert
        assertEquals("Intel Core i7", actual.get("cpu").asText());
        assertEquals("16GB", actual.get("ram").asText());
        assertEquals("512GB SSD", actual.get("storage").asText());
        assertEquals("13.3-inch 4K", actual.get("display").asText());
    }

    /**
     * Test Case ID: PIT012
     * Tên test: testCreateCompleteProduct
     * Mục tiêu: Kiểm tra tạo Product đầy đủ
     * Đầu vào: Product với tất cả quan hệ và JSON
     * Đầu ra mong đợi: lưu thành công, quan hệ đúng, JSON đúng
     * Ghi chú: Kiểm tra đầy đủ
     */
     @Test
    public void testCreateCompleteProduct() {
        // Arrange
        Category cat = new Category(); cat.setName("Laptop Gaming"); cat.setSlug("laptop-gaming"); cat.setStatus(1); Category savedCat = categoryRepository.save(cat);
        Brand brand = new Brand(); brand.setName("Dell"); brand.setCode("DELLComplete"); brand.setStatus(1); Brand savedBrand = brandRepository.save(brand);
        Supplier sup = new Supplier(); sup.setDisplayName("Công ty XYZ"); sup.setCode("SUP002"); sup.setStatus(1); Supplier savedSup = supplierRepository.save(sup);
        Unit u = new Unit(); u.setName("Piece"); u.setStatus(1); Unit savedUnit = unitRepository.save(u);
        Tag tag1 = new Tag(); tag1.setName("Gaming"); tag1.setSlug("gamingComplete"); tag1.setStatus(1); Tag sTag1 = tagRepository.save(tag1);
        Tag tag2 = new Tag(); tag2.setName("High-performance"); tag2.setSlug("high-performance"); tag2.setStatus(1); Tag sTag2 = tagRepository.save(tag2);
        Product product = new Product(); product.setName("Dell Alienware m15"); product.setCode("LAP012"); product.setSlug("dell-alienware-m15-Complete"); product.setShortDescription("Laptop gaming cao cấp"); product.setDescription("Laptop gaming Dell Alienware m15 với hiệu năng mạnh mẽ"); product.setStatus(1); product.setWeight(2.5);
        product.setCategory(savedCat); product.setBrand(savedBrand); product.setSupplier(savedSup); product.setUnit(savedUnit);
        product.setTags(new HashSet<>(Set.of(sTag1, sTag2)));
        ObjectNode specs2 = objectMapper.createObjectNode(); specs2.put("cpu","Intel Core i9"); specs2.put("ram","32GB"); specs2.put("gpu","NVIDIA RTX 3080"); product.setSpecifications(specs2);
        System.out.println("Input: ProductName" + product.getName() + ", Category=" + savedCat.getName() + ", Brand=" + savedBrand.getName() + ", Supplier=" + savedSup.getDisplayName() + ", Unit=" + savedUnit.getName() + ", Tags=[" + sTag1.getName() + "," + sTag2.getName() + "], Specifications=" + specs2.toString());

        // Act
        Product saved = productRepository.save(product);
        Product refProd = productRepository.findById(saved.getId()).orElseThrow();
        System.out.println("Expected Output: id=" + refProd.getId() + ", relations and specs match");

        // Assert
        assertNotNull(refProd.getId());
        assertEquals(savedCat.getId(), refProd.getCategory().getId());
        assertEquals(savedBrand.getId(), refProd.getBrand().getId());
        assertEquals(savedSup.getId(), refProd.getSupplier().getId());
        assertEquals(savedUnit.getId(), refProd.getUnit().getId());
        assertEquals(2, refProd.getTags().size());
        JsonNode sp = refProd.getSpecifications();
        assertEquals("Intel Core i9", sp.get("cpu").asText());
        assertEquals("32GB", sp.get("ram").asText());
        assertEquals("NVIDIA RTX 3080", sp.get("gpu").asText());
    }

    /**
     * Test Case ID: PIT013
     * Tên test: testCreateProductWithDuplicateCodeOrSlug
     * Mục tiêu: Kiểm tra tạo sản phẩm trùng code hoặc slug
     * Đầu vào: hai Product với cùng code hoặc slug
     * Đầu ra mong đợi: ném DataIntegrityViolationException
     * Ghi chú: Đảm bảo ràng buộc UNIQUE
     */
    @Test
    public void testCreateProductWithDuplicateCodeOrSlug() {
        // Arrange
        Product p1 = new Product(); p1.setName("Laptop Dell XPS 13"); p1.setCode("LAP001"); p1.setSlug("laptop-dell-xps-13"); p1.setStatus(1); productRepository.save(p1);
        System.out.println("Input: existing code=LAP001, slug=laptop-dell-xps-13");

        // Duplicate code
        Product p2 = new Product(); p2.setName("Laptop Dell XPS 15"); p2.setCode("LAP001"); p2.setSlug("laptop-dell-xps-15"); p2.setStatus(1);
        System.out.println("Expected Output: DataIntegrityViolationException for duplicate code");
        assertThrows(DataIntegrityViolationException.class, () -> productRepository.save(p2));

        // Duplicate slug
        Product p3 = new Product(); p3.setName("Laptop Dell XPS 17"); p3.setCode("LAP002"); p3.setSlug("laptop-dell-xps-13"); p3.setStatus(1);
        System.out.println("Expected Output: DataIntegrityViolationException for duplicate slug");
        assertThrows(DataIntegrityViolationException.class, () -> productRepository.save(p3));
    }
}
