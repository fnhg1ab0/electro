package com.electro.Minh;

// Import các thư viện cần thiết
import com.electro.constant.AppConstants;
import com.electro.controller.GenericController;
import com.electro.dto.ListResponse;
import com.electro.dto.address.AddressRequest;
import com.electro.dto.inventory.DocketRequest;
import com.electro.dto.inventory.DocketVariantRequest;
import com.electro.dto.inventory.DocketReasonRequest;
import com.electro.dto.inventory.DocketReasonResponse;
import com.electro.entity.BaseEntity;
import com.electro.entity.inventory.DocketReason;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.inventory.DocketReasonMapper;
import com.electro.repository.inventory.DocketReasonRepository;
import com.electro.service.GenericService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * File test: docketReasonControllerTests.java
 * Class được test: GenericController (dùng cho docketReason)
 *
 * MỤC ĐÍCH: Test các chức năng CRUD của DocketReason (Lý do phiếu kho)
 *
 * Kiểm thử hộp trắng (White Box Testing):
 * - Cấp độ 1: Bao phủ câu lệnh - Mỗi câu lệnh được thực thi ít nhất một lần
 * - Cấp độ 2: Bao phủ nhánh - Mỗi nhánh quyết định được thực thi ít nhất một lần cho cả true và false
 * - Loop coverage - Kiểm tra các vòng lặp:
 *   * Loop 0 lần (kiểm tra 2 biên của for loop)
 *   * Loop 1 lần (loop chạy 1 lần duy nhất)
 *   * Loop k lần (loop chạy k lần; k nằm trong khoảng từ 1 đến n)
 *   * Loop n lần (loop chạy n lần; n là số lượng phần tử trong danh sách)
 */
@SpringBootTest              // Annotation để khởi tạo Spring Boot context cho test
@ActiveProfiles("test")      // Sử dụng profile "test" (thường dùng DB test riêng)
@Transactional              // Mỗi test sẽ được rollback sau khi chạy xong
public class DocketReasonTest {

    // ======================= KHAI BÁO BIẾN VÀ DEPENDENCIES =======================

    // Các tham số mặc định cho phân trang và tìm kiếm
    private final int page = Integer.parseInt(AppConstants.DEFAULT_PAGE_NUMBER);  // Trang mặc định (thường là 1)
    private final String search = null;     // Từ khóa tìm kiếm (null = không tìm kiếm)
    private final boolean all = false;      // Lấy tất cả hay phân trang (false = phân trang)

    // Inject các dependency từ Spring Container
    @Autowired
    private GenericController<DocketReasonRequest, DocketReasonResponse> docketReasonController;

    @Autowired
    private GenericService<DocketReason, DocketReasonRequest, DocketReasonResponse> docketReasonService;

    @Autowired
    private DocketReasonRepository docketReasonRepository;  // Repository để truy cập DB

    @Autowired
    private DocketReasonMapper docketReasonMapper;  // Mapper chuyển đổi Entity <-> DTO

    // Các biến cấu hình
    private int size = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);  // Kích thước trang (thường là 10)
    private String sort = AppConstants.DEFAULT_SORT;    // Cách sắp xếp mặc định
    private String filter = null;                       // Bộ lọc (null = không lọc)

    // ObjectMapper để chuyển đổi Object <-> JSON
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonNode requestNode;  // Node JSON để gửi request

    // ======================= SETUP TRƯỚC KHI CHẠY TEST =======================

    @BeforeEach  // Chạy trước mỗi test method
    public void setup() {
        // Cấu hình controller để sử dụng service và kiểu request
        docketReasonController.setCrudService(docketReasonService);
        docketReasonController.setRequestType(DocketReasonRequest.class);

        // Khởi tạo service với các thông tin cần thiết
        docketReasonService.init(
                docketReasonRepository,           // Repository để truy cập DB
                docketReasonMapper,              // Mapper để chuyển đổi dữ liệu
                List.of("name"),                // Các trường có thể search (chỉ search theo name)
                "DocketReason"                  // Tên entity để hiển thị trong error message
        );
    }

    // ======================= TEST CHỨC NĂNG LẤY DANH SÁCH (getAllResources) =======================

    /**
     * Test Case ID: WHT001
     * MỤC ĐÍCH: Kiểm tra lấy danh sách DocketReason với tham số mặc định
     * KỊCH BẢN: Gọi API với các tham số mặc định (page=1, size=10, không sort, không filter)
     * KẾT QUẢ MONG ĐỢI: HTTP 200 OK và trả về danh sách DocketReason có phân trang
     */
    @Test
    public void testGetAllResources_ThamSoMacDinh_WHT001() throws JsonProcessingException {
        // THỰC HIỆN: Gọi API lấy danh sách với tham số mặc định
        ResponseEntity<ListResponse<DocketReasonResponse>> response =
                docketReasonController.getAllResources(page, size, sort, filter, search, all);

        // In ra kết quả để debug (nếu cần)
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA KẾT QUẢ:
        assertEquals(200, response.getStatusCodeValue());  // Status code phải là 200
        assertNotNull(response.getBody());                 // Body không được null
        assertNotNull(response.getBody().getContent());    // Content (danh sách) không được null
        assertTrue(response.getBody().getTotalElements() > 0);  // Phải có ít nhất 1 bản ghi
    }

    /**
     * Test Case ID: WHT002
     * MỤC ĐÍCH: Kiểm tra chức năng phân trang
     * KỊCH BẢN: Lấy trang thứ 2 với kích thước trang là 2
     * KẾT QUẢ MONG ĐỢI: Trả về đúng trang và kích thước theo yêu cầu
     */
    @Test
    public void testGetAllResources_PhanTrang_WHT002() throws JsonProcessingException {
        // Đếm tổng số bản ghi trong DB để kiểm tra có đủ dữ liệu test không
        long totalElements = docketReasonRepository.count();
        size = 2; // Đặt kích thước trang là 2

        // Chỉ test nếu có ít nhất 3 bản ghi (để trang 2 có nghĩa)
        if (totalElements >= 3) {
            // THỰC HIỆN: Lấy trang thứ 2
            ResponseEntity<ListResponse<DocketReasonResponse>> response =
                    docketReasonController.getAllResources(2, size, sort, filter, search, all);

            System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

            // KIỂM TRA:
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertEquals(size, response.getBody().getSize());  // Kích thước trang đúng
            assertEquals(2, response.getBody().getPage());     // Số trang đúng
        } else {
            // Bỏ qua test nếu không đủ dữ liệu
            System.out.println("Bỏ qua test phân trang vì không đủ dữ liệu");
        }
    }

    /**
     * Test Case ID: WHT003
     * MỤC ĐÍCH: Kiểm tra chức năng sắp xếp
     * KỊCH BẢN: Sắp xếp danh sách theo tên tăng dần
     * KẾT QUẢ MONG ĐỢI: Danh sách được sắp xếp đúng thứ tự alphabet
     */
    @Test
    public void testGetAllResources_SapXep_WHT003() throws JsonProcessingException {
        sort = "name,asc";  // Sắp xếp theo tên tăng dần

        // THỰC HIỆN: Lấy danh sách đã sắp xếp
        ResponseEntity<ListResponse<DocketReasonResponse>> response =
                docketReasonController.getAllResources(page, size, sort, filter, search, all);

        // KIỂM TRA STATUS:
        assertEquals(200, response.getStatusCodeValue());
        List<DocketReasonResponse> results = response.getBody().getContent();
        assertNotNull(results);

        // KIỂM TRA THỨ TỰ SẮP XẾP (nếu có ít nhất 2 phần tử):
        if (results.size() >= 2) {
            Collator collator = Collator.getInstance(new Locale("vi", "VN"));  // Collator tiếng Việt

            // Duyệt qua từng cặp phần tử liền kề để kiểm tra thứ tự
            for (int i = 0; i < results.size() - 1; i++) {
                String current = results.get(i).getName();      // Tên hiện tại
                String next = results.get(i + 1).getName();     // Tên tiếp theo

                // Kiểm tra current <= next (thứ tự tăng dần)
                assertTrue(collator.compare(current, next) <= 0,
                        String.format("Sai thứ tự tại %d: '%s' > '%s'", i, current, next));
            }
        }
    }

    /**
     * Test Case ID: WHT004
     * MỤC ĐÍCH: Kiểm tra chức năng tìm kiếm
     * KỊCH BẢN: Tìm kiếm DocketReason theo từ khóa trong tên
     * KẾT QUẢ MONG ĐỢI: Chỉ trả về các bản ghi có tên chứa từ khóa
     */
    @Test
    public void testGetAllResources_TimKiem_WHT004() throws JsonProcessingException {
        // Từ khóa tìm kiếm (thường lấy từ dữ liệu có sẵn trong DB)
        String keyword = "10812-201";

        // THỰC HIỆN: Tìm kiếm với từ khóa
        ResponseEntity<ListResponse<DocketReasonResponse>> response =
                docketReasonController.getAllResources(page, size, sort, filter, keyword, all);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA STATUS:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // KIỂM TRA KẾT QUẢ TÌM KIẾM:
        List<DocketReasonResponse> results = response.getBody().getContent();
        if (!results.isEmpty()) {
            // Tất cả kết quả phải chứa từ khóa tìm kiếm
            for (DocketReasonResponse item : results) {
                assertTrue(item.getName().toLowerCase().contains(keyword.toLowerCase()));
            }
        }
    }

    /**
     * Test Case ID: WHT005
     * MỤC ĐÍCH: Kiểm tra chức năng lấy tất cả không phân trang
     * KỊCH BẢN: Đặt all=true để lấy toàn bộ dữ liệu
     * KẾT QUẢ MONG ĐỢI: Trả về tất cả bản ghi, không phân trang
     */
    @Test
    public void testGetAllResources_LayTatCa_WHT005() throws JsonProcessingException {
        // Đếm tổng số bản ghi trong DB
        long totalRecords = docketReasonRepository.count();

        // THỰC HIỆN: Lấy tất cả không phân trang (all=true)
        ResponseEntity<ListResponse<DocketReasonResponse>> response =
                docketReasonController.getAllResources(page, size, sort, filter, search, true);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Khi all=true, số lượng trả về phải bằng tổng số bản ghi trong DB
        assertEquals(totalRecords, response.getBody().getContent().size());
        assertEquals(totalRecords, response.getBody().getTotalElements());
    }

    // ======================= TEST CHỨC NĂNG LẤY CHI TIẾT (getResource) =======================

    /**
     * Test Case ID: WHT006
     * MỤC ĐÍCH: Kiểm tra lấy chi tiết DocketReason khi ID tồn tại
     * KỊCH BẢN: Gọi API với ID có trong DB
     * KẾT QUẢ MONG ĐỢI: HTTP 200 OK và trả về thông tin chi tiết
     */
    @Test
    public void testGetResource_TonTai_WHT006() throws JsonProcessingException {
        // CHUẨN BỊ: Lấy một ID tồn tại từ DB
        Long existingId = docketReasonRepository.findAll().stream()
                .findFirst()                    // Lấy phần tử đầu tiên
                .map(BaseEntity::getId)         // Lấy ID
                .orElse(null);                  // Nếu không có thì null

        // Đảm bảo có dữ liệu để test (nếu không có thì skip test)
        assumeTrue(existingId != null, "Không có docketReason trong cơ sở dữ liệu để test");

        // THỰC HIỆN: Lấy chi tiết theo ID
        ResponseEntity<DocketReasonResponse> response = docketReasonController.getResource(existingId);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());  // ID trả về phải đúng
    }

    /**
     * Test Case ID: WHT007
     * MỤC ĐÍCH: Kiểm tra xử lý lỗi khi ID không tồn tại
     * KỊCH BẢN: Gọi API với ID không có trong DB
     * KẾT QUẢ MONG ĐỢI: Ném ResourceNotFoundException
     */
    @Test
    public void testGetResource_KhongTonTai_WHT007() {
        // CHUẨN BỊ: Sử dụng ID không tồn tại
        Long nonExistingId = 999999L;

        // Đảm bảo ID thực sự không tồn tại
        assumeFalse(docketReasonRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> docketReasonController.getResource(nonExistingId)
        );

        // Kiểm tra thông báo lỗi có chứa ID
        assertTrue(exception.getMessage().contains(nonExistingId.toString()));
    }

    // ======================= TEST CHỨC NĂNG TẠO MỚI (createResource) =======================

    /**
     * Test Case ID: WCT008
     * MỤC ĐÍCH: Kiểm tra tạo mới DocketReason thành công
     * KỊCH BẢN: Tạo DocketReason với dữ liệu hợp lệ
     * KẾT QUẢ MONG ĐỢI: HTTP 201 Created và trả về thông tin đã tạo
     */
    @Test
    public void testCreateDocketReason_ThanhCong_WCT008() throws JsonProcessingException {
        // CHUẨN BỊ: Tạo request với dữ liệu hợp lệ
        DocketReasonRequest request = new DocketReasonRequest();
        request.setName("Lý do nhập kho test");  // Tên lý do
        request.setStatus(1);                    // Trạng thái active

        // Chuyển request thành JsonNode
        requestNode = objectMapper.valueToTree(request);

        // THỰC HIỆN: Tạo mới
        ResponseEntity<DocketReasonResponse> response = docketReasonController.createResource(requestNode);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(201, response.getStatusCodeValue());  // Status 201 Created
        assertNotNull(response.getBody());
        assertEquals("Lý do nhập kho test", response.getBody().getName());  // Tên đúng
    }

    /**
     * Test Case ID: WCT009
     * MỤC ĐÍCH: Kiểm tra xử lý lỗi khi tên bị trùng
     * KỊCH BẢN: Tạo 2 DocketReason có cùng tên
     * KẾT QUẢ MONG ĐỢI: Ném exception do vi phạm constraint unique
     */
    @Test
    public void testCreateDocketReason_TrungMaCode_WCT009() throws JsonProcessingException {
        // CHUẨN BỊ: Tạo bản ghi đầu tiên
        DocketReasonRequest request1 = new DocketReasonRequest();
        request1.setName("Lý do trùng");
        request1.setStatus(1);
        docketReasonController.createResource(objectMapper.valueToTree(request1));

        // Tạo bản ghi thứ hai với tên trùng
        DocketReasonRequest request2 = new DocketReasonRequest();
        request2.setName("Lý do trùng");  // Trùng tên với bản ghi đầu
        request2.setStatus(1);

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        assertThrows(Exception.class, () ->
                docketReasonController.createResource(objectMapper.valueToTree(request2)));
    }

    /**
     * Test Case ID: WCT010
     * MỤC ĐÍCH: Kiểm tra xử lý lỗi khi thiếu dữ liệu bắt buộc
     * KỊCH BẢN: Tạo DocketReason không có tên (trường bắt buộc)
     * KẾT QUẢ MONG ĐỢI: Ném exception do validation fail
     */
    @Test
    public void testCreateDocketReason_DuLieuThieu_WCT010() {
        // CHUẨN BỊ: Request thiếu tên (trường bắt buộc)
        DocketReasonRequest request = new DocketReasonRequest();
        // Không set name - vi phạm validation
        request.setStatus(1);

        requestNode = objectMapper.valueToTree(request);

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        assertThrows(Exception.class, () -> docketReasonController.createResource(requestNode));
    }

    // ======================= TEST CHỨC NĂNG CẬP NHẬT (updateResource) =======================

    /**
     * Test Case ID: WHT011
     * MỤC ĐÍCH: Kiểm tra cập nhật DocketReason thành công
     * KỊCH BẢN: Cập nhật DocketReason có ID tồn tại với dữ liệu hợp lệ
     * KẾT QUẢ MONG ĐỢI: HTTP 200 OK và trả về thông tin đã cập nhật
     */
    @Test
    public void testUpdateResource_ThanhCong_WHT011() throws JsonProcessingException {
        // CHUẨN BỊ: Lấy ID tồn tại
        Long existingId = docketReasonRepository.findAll().stream()
                .findFirst().map(BaseEntity::getId).orElse(null);
        assumeTrue(existingId != null, "Không có docketReason trong DB");

        // Tạo dữ liệu cập nhật
        DocketReasonRequest request = new DocketReasonRequest();
        request.setName("Lý do đã cập nhật");  // Tên mới
        request.setStatus(0);                  // Trạng thái mới (inactive)

        requestNode = objectMapper.valueToTree(request);

        // THỰC HIỆN: Cập nhật
        ResponseEntity<DocketReasonResponse> response =
                docketReasonController.updateResource(existingId, requestNode);

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());           // ID không đổi
        assertEquals("Lý do đã cập nhật", response.getBody().getName()); // Tên đã cập nhật
    }

    /**
     * Test Case ID: WHT012
     * MỤC ĐÍCH: Kiểm tra xử lý lỗi khi cập nhật ID không tồn tại
     * KỊCH BẢN: Cập nhật DocketReason với ID không có trong DB
     * KẾT QUẢ MONG ĐỢI: Ném ResourceNotFoundException
     */
    @Test
    public void testUpdateResource_KhongTonTai_WHT012() {
        // CHUẨN BỊ: ID không tồn tại
        Long nonExistingId = 999999L;
        assumeFalse(docketReasonRepository.existsById(nonExistingId));

        DocketReasonRequest request = new DocketReasonRequest();
        request.setName("Lý do không tồn tại");
        request.setStatus(1);

        requestNode = objectMapper.valueToTree(request);

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        assertThrows(ResourceNotFoundException.class, () ->
                docketReasonController.updateResource(nonExistingId, requestNode));
    }

    // ======================= TEST CHỨC NĂNG XÓA (deleteResource) =======================

    /**
     * Test Case ID: WHT013
     * MỤC ĐÍCH: Kiểm tra xóa DocketReason thành công
     * KỊCH BẢN: Xóa DocketReason có ID tồn tại
     * KẾT QUẢ MONG ĐỢI: HTTP 204 No Content
     */
    @Test
    public void testDeleteResource_ThanhCong_WHT013() {
        // CHUẨN BỊ: Lấy ID tồn tại
        Long existingId = docketReasonRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        assumeTrue(existingId != null, "Không có docketReason trong cơ sở dữ liệu để test");

        // THỰC HIỆN: Xóa
        ResponseEntity<Void> response = docketReasonController.deleteResource(existingId);

        // KIỂM TRA: Status 204 No Content
        assertEquals(204, response.getStatusCodeValue());
    }

    // ======================= TEST CHỨC NĂNG XÓA NHIỀU (deleteResources) =======================

    /**
     * Test Case ID: WHT014
     * MỤC ĐÍCH: Kiểm tra xóa nhiều DocketReason thành công
     * KỊCH BẢN: Xóa danh sách DocketReason có ID tồn tại
     * KẾT QUẢ MONG ĐỢI: HTTP 204 No Content
     */
    @Test
    public void testDeleteResources_ThanhCong_WHT014() {
        // CHUẨN BỊ: Lấy danh sách ID tồn tại (tối đa 2 ID)
        List<Long> existingIds = docketReasonRepository.findAll().stream()
                .map(BaseEntity::getId)
                .limit(2)  // Giới hạn 2 ID
                .collect(Collectors.toList());

        assumeTrue(existingIds.size() >= 2, "Không đủ DocketReason trong cơ sở dữ liệu để test");

        // THỰC HIỆN: Xóa nhiều
        ResponseEntity<Void> response = docketReasonController.deleteResources(existingIds);

        // KIỂM TRA: Status 204 No Content
        assertEquals(204, response.getStatusCodeValue());
    }

    /**
     * Test Case ID: WHT015
     * MỤC ĐÍCH: Kiểm tra xử lý lỗi khi xóa nhiều ID không tồn tại
     * KỊCH BẢN: Xóa danh sách DocketReason với ID không có trong DB
     * KẾT QUẢ MONG ĐỢI: Ném EmptyResultDataAccessException
     */
    @Test
    public void testDeleteResources_KhongTonTai_WHT015() {
        // CHUẨN BỊ: Danh sách ID không tồn tại
        List<Long> nonExistingIds = List.of(999999L, 888888L);
        assumeTrue(nonExistingIds.stream().noneMatch(docketReasonRepository::existsById));

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        Exception exception = assertThrows(
                EmptyResultDataAccessException.class,  // Exception cụ thể từ Spring Data
                () -> docketReasonController.deleteResources(nonExistingIds)
        );

        // Kiểm tra thông báo lỗi (tùy chọn)
        assertTrue(exception.getMessage().toLowerCase().contains("no class"));
    }
}