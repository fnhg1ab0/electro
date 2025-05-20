package com.electro.Tuan;

import com.electro.constant.AppConstants;
import com.electro.controller.GenericController;
import com.electro.dto.ListResponse;
import com.electro.dto.product.ProductResponse;
import com.electro.dto.promotion.PromotionRequest;
import com.electro.dto.promotion.PromotionResponse;
import com.electro.entity.BaseEntity;
import com.electro.exception.ResourceNotFoundException;
import com.electro.repository.promotion.PromotionRepository;
import com.electro.service.promotion.PromotionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * File test: PromotionControllerTests.java
 * Class được test: GenericController (dùng cho Promotion)
 * <p>
 * Kiểm thử hộp trắng:
 * - Cấp độ 1: Bao phủ câu lệnh - Mỗi câu lệnh được thực thi ít nhất một lần
 * - Cấp độ 2: Bao phủ nhánh - Mỗi nhánh quyết định được thực thi ít nhất một lần cho cả true và false
 * - Loop coverage - Kiểm tra các vòng lặp
 * <p>
 * * Loop 0 lần (kiểm tra 2 biên của for loop)
 * * Loop 1 lần (loop chạy 1 lần duy nhất)
 * * Loop k lần (loop chạy k lần; k nằm trong khoảng từ 1 đến n)
 * * Loop n lần (loop chạy n lần; n là số lượng phần tử trong danh sách)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PromotionControllerTests {

    private final int page = Integer.parseInt(AppConstants.DEFAULT_PAGE_NUMBER);
    private final String search = null;
    private final boolean all = false;
    @Autowired
    private GenericController<PromotionRequest, PromotionResponse> promotionController;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private PromotionRepository promotionRepository;
    private int size = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);
    private String sort = AppConstants.DEFAULT_SORT;
    private String filter = null;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonNode requestNode;

    @BeforeEach
    public void setup() {
        // Set the crudService in the generic controller before each test
        promotionController.setCrudService(promotionService);
        promotionController.setRequestType(PromotionRequest.class);
    }

    // ---------------------------------------------------------------------------------
    // getAllResources()
    // ---------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT001
     * Tên test: testGetAllResources_ThamSoMacDinh
     * Mục tiêu: Kiểm tra phương thức getAllResources với tham số mặc định
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với danh sách khuyến mãi và thông tin phân trang
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint lấy danh sách khuyến mãi
     */
    @Test
    public void testGetAllResources_ThamSoMacDinh_PCT001() throws JsonProcessingException {
        // Thực hiện với tham số mặc định
        ResponseEntity<ListResponse<PromotionResponse>> response =
                promotionController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getContent());
        // Kiểm tra có dữ liệu trả về
        assertTrue(response.getBody().getTotalElements() > 0);
    }

    /**
     * Test Case ID: PCT002
     * Tên test: testGetAllResources_PhanTrang
     * Mục tiêu: Kiểm tra phương thức getAllResources với phân trang
     * Đầu vào: Tham số page=2, size=2, sort=null, filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với trang thứ 2 của dữ liệu
     * Ghi chú: Kiểm tra chức năng phân trang khi lấy danh sách khuyến mãi
     */
    @Test
    public void testGetAllResources_PhanTrang_PCT002() throws JsonProcessingException {
        // Đếm tổng số bản ghi để tính số trang
        long totalElements = promotionRepository.count();
        size = 2; // Kích thước trang là 2

        // Nếu có ít nhất 3 bản ghi, kiểm tra trang thứ 2
        if (totalElements >= 3) {
            // Thực hiện - lấy trang thứ 2 với kích thước trang là 2
            ResponseEntity<ListResponse<PromotionResponse>> response =
                    promotionController.getAllResources(2, size, sort, filter, search, all);
            System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

            // Kiểm tra
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertEquals(size, response.getBody().getSize());
            assertEquals(2, response.getBody().getPage());
        } else {
            // Bỏ qua test nếu không đủ dữ liệu
            System.out.println("Bỏ qua test phân trang vì không đủ dữ liệu");
        }
    }

    /**
     * Test Case ID: PCT003
     * Tên test: testGetAllResources_SapXep
     * Mục tiêu: Kiểm tra phương thức getAllResources với sắp xếp
     * Đầu vào: Tham số page=1, size=10, sort="name,asc", filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với danh sách khuyến mãi được sắp xếp theo tên tăng dần
     * Ghi chú: Kiểm tra chức năng sắp xếp khi lấy danh sách khuyến mãi
     */
    @Test
    public void testGetAllResources_SapXep_PCT003() throws JsonProcessingException {
        // Thực hiện - sắp xếp theo tên tăng dần
        sort = "name,asc";
        ResponseEntity<ListResponse<PromotionResponse>> response =
                promotionController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        List<PromotionResponse> results = response.getBody().getContent();

        // Kiểm tra thứ tự sắp xếp nếu có ít nhất 2 kết quả
        if (results.size() >= 2) {
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue(results.get(i).getName().compareToIgnoreCase(results.get(i + 1).getName()) <= 0);
            }
        }
    }

    /**
     * Test Case ID: PCT004
     * Tên test: testGetAllResources_BoLoc
     * Mục tiêu: Kiểm tra phương thức getAllResources với bộ lọc
     * Đầu vào: Tham số page=1, size=10, sort=null, filter="{\"percent\":{\"$gt\":15}}", search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với danh sách khuyến mãi có percent > 15
     * Ghi chú: Kiểm tra chức năng lọc khi lấy danh sách khuyến mãi
     */
    @Test
    public void testGetAllResources_BoLoc_PCT004() throws JsonProcessingException {
        // Thực hiện - lọc khuyến mãi có phần trăm > 15
        filter = "percent>15";
        ResponseEntity<ListResponse<PromotionResponse>> response =
                promotionController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Kiểm tra tất cả kết quả đều có percent > 15
        for (PromotionResponse promo : response.getBody().getContent()) {
            assertTrue(promo.getPercent() > 15);
        }
    }

    /**
     * Test Case ID: PCT005
     * Tên test: testGetAllResources_TimKiem
     * Mục tiêu: Kiểm tra phương thức getAllResources với tìm kiếm
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search="khuyến mãi", all=false
     * Đầu ra mong đợi: HTTP 200 OK với danh sách khuyến mãi có tên chứa từ khóa tìm kiếm
     * Ghi chú: Kiểm tra chức năng tìm kiếm khi lấy danh sách khuyến mãi
     */
    @Test
    public void testGetAllResources_TimKiem_PCT005() throws JsonProcessingException {
        // Lấy từ khóa từ một promotion có trong DB
        String keyword = "khuyến mãi"; // Từ khóa phổ biến tiếng Việt cho promotions

        // Thực hiện tìm kiếm
        ResponseEntity<ListResponse<PromotionResponse>> response =
                promotionController.getAllResources(page, size, sort, filter, keyword, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Kiểm tra kết quả tìm kiếm
        List<PromotionResponse> results = response.getBody().getContent();
        if (!results.isEmpty()) {
            for (PromotionResponse promo : results) {
                assertTrue(promo.getName().toLowerCase().contains(keyword.toLowerCase()));
            }
        }
    }

    /**
     * Test Case ID: PCT006
     * Tên test: testGetAllResources_LayTatCa
     * Mục tiêu: Kiểm tra phương thức getAllResources với lấy tất cả dữ liệu
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search=null, all=true
     * Đầu ra mong đợi: HTTP 200 OK với tất cả khuyến mãi không phân trang
     * Ghi chú: Kiểm tra chức năng lấy toàn bộ dữ liệu không phân trang
     */
    @Test
    public void testGetAllResources_LayTatCa_PCT006() throws JsonProcessingException {
        // Đếm tổng số bản ghi trước
        long totalRecords = promotionRepository.count();

        // Thực hiện - lấy tất cả không phân trang
        ResponseEntity<ListResponse<PromotionResponse>> response =
                promotionController.getAllResources(page, size, sort, filter, search, true);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Khi all=true, số lượng phần tử trả về phải bằng tổng số phần tử trong DB
        assertEquals(totalRecords, response.getBody().getContent().size());
        assertEquals(totalRecords, response.getBody().getTotalElements());
    }

    // ---------------------------------------------------------------------------------
    // getResource()
    // ---------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT007
     * Tên test: testGetResource_TonTai
     * Mục tiêu: Kiểm tra phương thức getResource khi ID tồn tại
     * Đầu vào: ID của một promotion tồn tại
     * Đầu ra mong đợi: HTTP 200 OK với thông tin đầy đủ của promotion
     * Ghi chú: Kiểm tra chức năng lấy thông tin chi tiết của promotion
     */
    @Test
    public void testGetResource_TonTai_PCT007() throws JsonProcessingException {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = promotionRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một promotion trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có promotion trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<PromotionResponse> response = promotionController.getResource(existingId);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());
    }

    /**
     * Test Case ID: PCT008
     * Tên test: testGetResource_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức getResource khi ID không tồn tại
     * Đầu vào: ID không tồn tại
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi truy vấn tài nguyên không tồn tại
     */
    @Test
    public void testGetResource_KhongTonTai_PCT008() {
        // Chuẩn bị - Sử dụng một ID không tồn tại
        Long nonExistingId = 999999L;

        // Đảm bảo ID không tồn tại trong cơ sở dữ liệu
        assumeFalse(promotionRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");

        // Thực hiện & Kiểm tra
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> promotionController.getResource(nonExistingId)
        );

        // Kiểm tra thông báo lỗi
        assertTrue(exception.getMessage().contains(nonExistingId.toString()));
    }

    // ---------------------------------------------------------------------------------
    // createResource()
    // ---------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT009
     * Tên test: testCreateResource_ThanhCong
     * Mục tiêu: Kiểm tra phương thức createResource tạo mới khuyến mãi thành công
     * Đầu vào: Yêu cầu tạo khuyến mãi với thông tin hợp lệ chứa product
     * Đầu ra mong đợi: HTTP 201 Created với thông tin khuyến mãi đã tạo trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint tạo khuyến mãi
     * và loop 1 lần trong PromotionServiceImpl
     */
    @Test
    public void testCreateResource_ThanhCongProduct_PCT009() throws JsonProcessingException {
        // Chuẩn bị
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Test");
        request.setStartDate(Instant.now());
        request.setEndDate(Instant.now().plusSeconds(86400));
        request.setPercent(20);
        request.setStatus(1);

        Set<Long> productIds = new HashSet<>();
        productIds.add(2L);
        request.setProductIds(productIds);

        Set<Long> categoryIds = new HashSet<>();
        request.setCategoryIds(categoryIds);

        // Chuyển đổi PromotionRequest thành JsonNode để truyền vào controller
        requestNode = objectMapper.valueToTree(request);

        // Thực hiện
        ResponseEntity<PromotionResponse> response = promotionController.createResource(requestNode);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Khuyến mãi Test", response.getBody().getName());
        assertEquals(20, response.getBody().getPercent());
    }

    /**
     * Test Case ID: PCT010
     * Tên test: testCreateResource_ThanhCongCategory
     * Mục tiêu: Kiểm tra phương thức createResource tạo mới khuyến mãi thành công
     * Đầu vào: Yêu cầu tạo khuyến mãi với thông tin hợp lệ chứa category
     * Đầu ra mong đợi: HTTP 201 Created với thông tin khuyến mãi đã tạo trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint tạo khuyến mãi
     * với (if categoryIds.size() != 0) trong mapper
     * và loop n lần trong PromotionServiceImpl
     */
    @Test
    public void testCreateResource_ThanhCongCategory_PCT010() throws JsonProcessingException {
        // Chuẩn bị
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Test Category");
        request.setStartDate(Instant.now());
        request.setEndDate(Instant.now().plusSeconds(86400));
        request.setPercent(20);
        request.setStatus(1);

        Set<Long> categoryIds = new HashSet<>();
        categoryIds.add(2L);
        request.setCategoryIds(categoryIds);
        request.setProductIds(new HashSet<>()); // Không cần sản phẩm
        // Chuyển đổi PromotionRequest thành JsonNode để truyền vào controller
        requestNode = objectMapper.valueToTree(request);
        // Thực hiện
        ResponseEntity<PromotionResponse> response = promotionController.createResource(requestNode);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));
        // Kiểm tra
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Khuyến mãi Test Category", response.getBody().getName());
        assertEquals(20, response.getBody().getPercent());
        assertEquals(2, response.getBody().getProducts().size());
    }


    /**
     * Test Case ID: PCT011
     * Tên test: testCreateResource_KhongCoSanPham
     * Mục tiêu: Kiểm tra phương thức createResource xử lý lỗi khi không có sản phẩm nào trong danh sách
     * Đầu vào: Yêu cầu tạo khuyến mãi với danh sách sản phẩm rỗng
     * Đầu ra mong đợi: RuntimeException liên quan đến việc không có sản phẩm nào trong danh sách
     * Ghi chú: Kiểm tra xử lý lỗi đối với trường hợp không có sản phẩm nào trong danh sách
     * và (promotion.getProducts().size() == 0); loop 0 lần trong PromotionServiceImpl
     */
    @Test
    public void testCreateResource_KhongCoSanPham_PCT011() {
        // Chuẩn bị
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi không sản phẩm");
        request.setStartDate(Instant.now());
        request.setEndDate(Instant.now().plusSeconds(86400));
        request.setPercent(25);
        request.setStatus(1);

        // Tạo danh sách sản phẩm rỗng
        request.setProductIds(new HashSet<>());
        request.setCategoryIds(new HashSet<>());

        // Chuyển đổi PromotionRequest thành JsonNode
        requestNode = objectMapper.valueToTree(request);

        // Thực hiện & Kiểm tra
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> promotionController.createResource(requestNode)
        );

        assertEquals("Product list of promotion is empty", exception.getMessage());
    }

    /**
     * Test Case ID: PCT012
     * Tên test: testCreateResource_TrungLapKhuyenMai
     * Mục tiêu: Kiểm tra phương thức createResource xử lý lỗi khi tạo khuyến mãi trùng lặp cho sản phẩm
     * Đầu vào: Yêu cầu tạo khuyến mãi cho sản phẩm đã có khuyến mãi trong cùng khoảng thời gian
     * Đầu ra mong đợi: RuntimeException liên quan đến việc trùng lặp khuyến mãi
     * Ghi chú: Kiểm tra xử lý lỗi đối với trường hợp trùng lặp khuyến mãi
     * và loop k lần trong PromotionServiceImpl (trùng lặp sản phẩm với promotion khác)
     */
    @Test
    public void testCreateResource_TrungLapKhuyenMai_PCT012() {
        // Chuẩn bị - Tạo khuyến mãi thứ nhất
        PromotionRequest firstRequest = new PromotionRequest();
        firstRequest.setName("Khuyến mãi Đầu tiên");
        firstRequest.setStartDate(Instant.now());
        firstRequest.setEndDate(Instant.now().plusSeconds(86400));
        firstRequest.setPercent(10);
        firstRequest.setStatus(1);

        Set<Long> productIds = new HashSet<>();
        productIds.add(3L);
        firstRequest.setProductIds(productIds);
        firstRequest.setCategoryIds(new HashSet<>());

        JsonNode firstRequestNode = objectMapper.valueToTree(firstRequest);

        ResponseEntity<PromotionResponse> firstResponse = promotionController.createResource(firstRequestNode);

        assertEquals(201, firstResponse.getStatusCodeValue());

        // Chuẩn bị khuyến mãi thứ hai trùng với khuyến mãi đầu tiên
        PromotionRequest secondRequest = new PromotionRequest();
        secondRequest.setName("Khuyến mãi Thứ hai");
        secondRequest.setStartDate(firstRequest.getStartDate());
        secondRequest.setEndDate(firstRequest.getEndDate());
        secondRequest.setPercent(15);
        secondRequest.setStatus(1);

        productIds = new HashSet<>();
        productIds.add(2L);
        productIds.add(3L);
        secondRequest.setProductIds(productIds); // Dùng chung productIds với khuyến mãi đầu tiên
        secondRequest.setCategoryIds(new HashSet<>());

        JsonNode secondRequestNode = objectMapper.valueToTree(secondRequest);

        // Thực hiện & Kiểm tra - Tạo khuyến mãi thứ hai phải ném ngoại lệ
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> promotionController.createResource(secondRequestNode)
        );

        assertTrue(exception.getMessage().contains("Overlap promotion with product id:"));
    }

    /**
     * Test Case ID: PCT013
     * Tên test: testCreateResource_DuLieuKhongHopLe
     * Mục tiêu: Kiểm tra phương thức createResource xử lý lỗi với dữ liệu không hợp lệ
     * Đầu vào: Yêu cầu tạo khuyến mãi với ngày kết thúc trước ngày bắt đầu
     * Đầu ra mong đợi: Exception liên quan đến dữ liệu không hợp lệ
     * Ghi chú: Kiểm tra xử lý lỗi đối với dữ liệu đầu vào không hợp lệ
     */
    @Test
    public void testCreateResource_DuLieuKhongHopLe_PCT013() {
        // Chuẩn bị
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Không hợp lệ");

        // Ngày kết thúc trước ngày bắt đầu
        Instant endDate = Instant.now();
        Instant startDate = endDate.plusSeconds(86400); // startDate sau endDate 1 ngày

        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setPercent(30);
        request.setStatus(1);

        Set<Long> productIds = new HashSet<>();
        productIds.add(1L);
        request.setProductIds(productIds);

        // Chuyển đổi PromotionRequest thành JsonNode
        requestNode = objectMapper.valueToTree(request);

        // Thực hiện & Kiểm tra
        Exception exception = assertThrows(
                Exception.class,
                () -> promotionController.createResource(requestNode)
        );

        // Không cần kiểm tra message cụ thể vì có thể khác nhau tùy vào cài đặt validation
        assertNotNull(exception);
    }

    // ----------------------------------------------------------------------------------
    // updateResource()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT014
     * Tên test: testUpdateResource_ThanhCong
     * Mục tiêu: Kiểm tra phương thức updateResource cập nhật khuyến mãi thành công
     * Đầu vào: ID của khuyến mãi đã tồn tại và yêu cầu cập nhật với thông tin hợp lệ
     * Đầu ra mong đợi: HTTP 200 OK với thông tin khuyến mãi đã cập nhật trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint cập nhật khuyến mãi
     */
    @Test
    public void testUpdateResource_ThanhCong_PCT014() throws JsonProcessingException {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = promotionRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một promotion trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có promotion trong cơ sở dữ liệu để test");

        // Tạo yêu cầu cập nhật
        ResponseEntity<PromotionResponse> initialResponse = promotionController.getResource(existingId);
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Cập nhật");
        request.setPercent(30);
        request.setStatus(1);

        Set<ProductResponse> productIds = Objects.requireNonNull(initialResponse.getBody()).getProducts();
        Set<Long> productIdsLong = new HashSet<>();
        for (ProductResponse product : productIds) {
            productIdsLong.add(product.getId());
        }
        productIdsLong.add(2L);
        request.setProductIds(productIdsLong);

        Set<Long> categoryIds = new HashSet<>();
        request.setCategoryIds(categoryIds);
        // Chuyển đổi PromotionRequest thành JsonNode
        requestNode = objectMapper.valueToTree(request);
        // Thực hiện
        ResponseEntity<PromotionResponse> response = promotionController.updateResource(existingId, requestNode);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));
        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());
        assertEquals("Khuyến mãi Cập nhật", response.getBody().getName());
        assertEquals(30, response.getBody().getPercent());
        assertEquals(2, response.getBody().getProducts().size());
    }

    /**
     * Test Case ID: PCT015
     * Tên test: testUpdateResource_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức updateResource xử lý lỗi khi ID không tồn tại
     * Đầu vào: ID không tồn tại và yêu cầu cập nhật với thông tin hợp lệ
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi cố gắng cập nhật tài nguyên không tồn tại
     */
    @Test
    public void testUpdateResource_KhongTonTai_PCT015() {
        // Chuẩn bị - Sử dụng một ID không tồn tại
        Long nonExistingId = 999999L;

        // Đảm bảo ID không tồn tại trong cơ sở dữ liệu
        assumeFalse(promotionRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");
        // Tạo yêu cầu cập nhật
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Cập nhật Không tồn tại");
        request.setStartDate(Instant.now());
        request.setEndDate(Instant.now().plusSeconds(86400));
        request.setPercent(30);
        request.setStatus(1);
        request.setProductIds(new HashSet<>());
        request.setCategoryIds(new HashSet<>());
        // Chuyển đổi PromotionRequest thành JsonNode
        requestNode = objectMapper.valueToTree(request);
        // Thực hiện & Kiểm tra
        assertThrows(
                ResourceNotFoundException.class,
                () -> promotionController.updateResource(nonExistingId, requestNode)
        );
    }

    /**
     * Test Case ID: PCT016
     * Tên test: testUpdateResource_KhongCoSanPham
     * Mục tiêu: Kiểm tra phương thức updateResource xử lý lỗi khi không có sản phẩm nào trong danh sách
     * Đầu vào: ID của khuyến mãi đã tồn tại và yêu cầu cập nhật với danh sách sản phẩm rỗng
     * Đầu ra mong đợi: RuntimeException liên quan đến việc không có sản phẩm nào trong danh sách
     * Ghi chú: Kiểm tra xử lý lỗi đối với trường hợp không có sản phẩm nào trong danh sách
     */
    @Test
    public void testUpdateResource_KhongCoSanPham_PCT016() {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = promotionRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một promotion trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có promotion trong cơ sở dữ liệu để test");

        // Tạo yêu cầu cập nhật với danh sách sản phẩm rỗng
        PromotionRequest request = new PromotionRequest();
        request.setName("Khuyến mãi Cập nhật Không sản phẩm");
        request.setStartDate(Instant.now());
        request.setEndDate(Instant.now().plusSeconds(86400));
        request.setPercent(30);
        request.setStatus(1);
        request.setProductIds(new HashSet<>()); // Danh sách sản phẩm rỗng
        request.setCategoryIds(new HashSet<>());

        // Chuyển đổi PromotionRequest thành JsonNode
        requestNode = objectMapper.valueToTree(request);

        // Thực hiện & Kiểm tra
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> promotionController.updateResource(existingId, requestNode)
        );

        assertEquals("Product list of promotion is empty", exception.getMessage());
    }

    // ----------------------------------------------------------------------------------
    // deleteResource()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT017
     * Tên test: testDeleteResource_ThanhCong
     * Mục tiêu: Kiểm tra phương thức deleteResource xóa khuyến mãi thành công
     * Đầu vào: ID của khuyến mãi đã tồn tại
     * Đầu ra mong đợi: HTTP 204 No Content
     * Ghi chú: Kiểm tra chức năng xóa khuyến mãi
     */
    @Test
    public void testDeleteResource_ThanhCong_PCT017() {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = promotionRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một promotion trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có promotion trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = promotionController.deleteResource(existingId);

        // Kiểm tra
        assertEquals(204, response.getStatusCodeValue());
    }

    /**
     * Test Case ID: PCT018
     * Tên test: testDeleteResource_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức deleteResource xử lý lỗi khi ID không tồn tại
     * Đầu vào: ID không tồn tại
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi cố gắng xóa tài nguyên không tồn tại
     */
    @Test
    public void testDeleteResource_KhongTonTai_PCT018() {
        // Chuẩn bị - Sử dụng một ID không tồn tại
        Long nonExistingId = 999999L;

        // Đảm bảo ID không tồn tại trong cơ sở dữ liệu
        assumeFalse(promotionRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");
        // Thực hiện & Kiểm tra
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> promotionController.deleteResource(nonExistingId)
        );
        // Kiểm tra thông báo lỗi
        assertTrue(exception.getMessage().contains(nonExistingId.toString()));
    }

    // ----------------------------------------------------------------------------------
    // deleteResources()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: PCT019
     * Tên test: testDeleteResources_ThanhCong
     * Mục tiêu: Kiểm tra phương thức deleteResources xóa nhiều khuyến mãi thành công
     * Đầu vào: Danh sách ID của các khuyến mãi đã tồn tại
     * Đầu ra mong đợi: HTTP 204 No Content
     * Ghi chú: Kiểm tra chức năng xóa nhiều khuyến mãi
     */
    @Test
    public void testDeleteResources_ThanhCong_PCT019() {
        // Chuẩn bị - Lấy danh sách ID tồn tại từ cơ sở dữ liệu
        List<Long> existingIds = promotionRepository.findAll().stream()
                .map(BaseEntity::getId)
                .limit(2) // Giới hạn lấy 2 ID
                .collect(Collectors.toList());

        // đảm bảo có ít nhất 2 promotion trong cơ sở dữ liệu
        assumeTrue(existingIds.size() >= 2, "Không đủ khuyến mãi trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = promotionController.deleteResources(existingIds);

        // Kiểm tra
        assertEquals(204, response.getStatusCodeValue());
    }

    /**
     * Test Case ID: PCT020
     * Tên test: testDeleteResources_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức deleteResources xử lý lỗi khi ID không tồn tại
     * Đầu vào: Danh sách ID không tồn tại
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi cố gắng xóa nhiều tài nguyên không tồn tại
     */
    @Test
    public void testDeleteResources_KhongTonTai_PCT020() {
        // Chuẩn bị - Sử dụng danh sách ID không tồn tại
        List<Long> nonExistingIds = List.of(999999L, 888888L);

        // Đảm bảo các ID không tồn tại trong cơ sở dữ liệu
        assumeTrue(nonExistingIds.stream().noneMatch(promotionRepository::existsById),
                "Một trong các ID đã tồn tại trong cơ sở dữ liệu");

        // Thực hiện & Kiểm tra
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> promotionController.deleteResources(nonExistingIds)
        );

        // Kiểm tra thông báo lỗi
        assertTrue(exception.getMessage().contains("Không tìm thấy tài nguyên với ID: " + nonExistingIds));
    }
}