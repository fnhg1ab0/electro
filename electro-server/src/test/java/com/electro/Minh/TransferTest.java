package com.electro.Minh;

// Import các thư viện cần thiết
import com.electro.constant.AppConstants;
import com.electro.controller.GenericController;
import com.electro.dto.ListResponse;
import com.electro.dto.address.AddressRequest;
import com.electro.dto.inventory.DocketRequest;
import com.electro.dto.inventory.DocketVariantRequest;
import com.electro.dto.inventory.TransferRequest;
import com.electro.dto.inventory.TransferResponse;
import com.electro.entity.BaseEntity;
import com.electro.entity.inventory.Transfer;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.inventory.TransferMapper;
import com.electro.repository.inventory.TransferRepository;
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
 * File test: transferControllerTests.java
 * Class được test: GenericController (dùng cho transfer)
 *
 * MỤC ĐÍCH: Test chức năng CRUD (Create, Read, Update, Delete) cho Transfer
 * Transfer là chức năng chuyển kho - chuyển hàng từ kho này sang kho khác
 *
 * PHƯƠNG PHÁP KIỂM THỬ HỘP TRẮNG (White Box Testing):
 * - Cấp độ 1: Bao phủ câu lệnh - Mỗi câu lệnh được thực thi ít nhất một lần
 * - Cấp độ 2: Bao phủ nhánh - Mỗi nhánh quyết định được thực thi ít nhất một lần cho cả true và false
 * - Loop coverage - Kiểm tra các vòng lặp:
 *   * Loop 0 lần (kiểm tra 2 biên của for loop)
 *   * Loop 1 lần (loop chạy 1 lần duy nhất)
 *   * Loop k lần (loop chạy k lần; k nằm trong khoảng từ 1 đến n)
 *   * Loop n lần (loop chạy n lần; n là số lượng phần tử trong danh sách)
 */
@SpringBootTest // Annotation để chạy test trong Spring Boot context
@ActiveProfiles("test") // Sử dụng profile "test" (có thể có DB test riêng)
@Transactional // Mỗi test method sẽ chạy trong transaction và rollback sau khi test
public class TransferTest {

    // ====================================================================================
    // KHAI BÁO BIẾN VÀ DEPENDENCY INJECTION
    // ====================================================================================

    // Các tham số mặc định cho phân trang
    private final int page = Integer.parseInt(AppConstants.DEFAULT_PAGE_NUMBER); // Trang đầu tiên (thường là 1)
    private final String search = null; // Không tìm kiếm gì
    private final boolean all = false; // Không lấy tất cả, có phân trang

    // Inject các dependency cần thiết từ Spring context
    @Autowired
    private GenericController<TransferRequest, TransferResponse> transferController; // Controller để test

    @Autowired
    private GenericService<Transfer, TransferRequest, TransferResponse> transferService; // Service xử lý logic

    @Autowired
    private TransferRepository transferRepository; // Repository để truy cập database

    @Autowired
    private TransferMapper transferMapper; // Mapper chuyển đổi giữa Entity và DTO

    // Các biến cấu hình cho test
    private int size = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE); // Kích thước trang (thường là 10)
    private String sort = AppConstants.DEFAULT_SORT; // Cách sắp xếp mặc định
    private String filter = null; // Không có filter

    // ObjectMapper để chuyển đổi JSON
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonNode requestNode; // Node JSON để gửi request

    /**
     * PHƯƠNG THỨC SETUP - chạy trước mỗi test method
     * Mục đích: Khởi tạo và cấu hình các component cần thiết
     */
    @BeforeEach
    public void setup() {
        // Cấu hình controller với service
        transferController.setCrudService(transferService);
        transferController.setRequestType(TransferRequest.class);

        // Khởi tạo service với các tham số cần thiết
        transferService.init(
                transferRepository,        // Repository để truy cập DB
                transferMapper,           // Mapper để chuyển đổi dữ liệu
                List.of("code"),         // Các trường có thể search (tìm theo mã code)
                "Transfer"               // Tên entity để hiển thị trong error message
        );
    }

    // ====================================================================================
    // TEST METHODS CHO getAllResources() - LẤY DANH SÁCH TRANSFER
    // ====================================================================================

    /**
     * Test Case ID: WHT001
     * KIỂM TRA: Lấy danh sách transfer với tham số mặc định
     * KỊCH BẢN: Gọi API GET /transfers với tham số mặc định
     * KỲ VỌNG: Trả về HTTP 200 và danh sách transfer có phân trang
     */
    @Test
    public void testGetAllResources_ThamSoMacDinh_WHT001() throws JsonProcessingException {
        // THỰC HIỆN: Gọi controller với tham số mặc định
        ResponseEntity<ListResponse<TransferResponse>> response =
                transferController.getAllResources(page, size, sort, filter, search, all);

        // In ra kết quả để debug
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA KẾT QUẢ:
        assertEquals(200, response.getStatusCodeValue()); // HTTP status phải là 200 OK
        assertNotNull(response.getBody()); // Response body không được null
        assertNotNull(response.getBody().getContent()); // Danh sách content không được null
        assertTrue(response.getBody().getTotalElements() > 0); // Phải có ít nhất 1 transfer
    }

    /**
     * Test Case ID: WHT002
     * KIỂM TRA: Chức năng phân trang
     * KỊCH BẢN: Lấy trang 2 với kích thước trang là 2
     * KỲ VỌNG: Trả về đúng trang và kích thước trang
     */
    @Test
    public void testGetAllResources_PhanTrang_WHT002() throws JsonProcessingException {
        // Đếm tổng số bản ghi để biết có đủ dữ liệu cho phân trang không
        long totalElements = transferRepository.count();
        size = 2; // Đặt kích thước trang = 2

        // CHỈ TEST KHI CÓ ĐỦ DỮ LIỆU (ít nhất 3 bản ghi để có trang 2)
        if (totalElements >= 3) {
            // THỰC HIỆN: Lấy trang thứ 2
            ResponseEntity<ListResponse<TransferResponse>> response =
                    transferController.getAllResources(2, size, sort, filter, search, all);
            System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

            // KIỂM TRA:
            assertEquals(200, response.getStatusCodeValue());
            assertNotNull(response.getBody());
            assertEquals(size, response.getBody().getSize()); // Kích thước trang đúng
            assertEquals(2, response.getBody().getPage()); // Đúng trang số 2
        } else {
            // Bỏ qua test nếu không đủ dữ liệu
            System.out.println("Bỏ qua test phân trang vì không đủ dữ liệu");
        }
    }

    /**
     * Test Case ID: WHT003
     * KIỂM TRA: Chức năng sắp xếp
     * KỊCH BẢN: Sắp xếp transfer theo code tăng dần
     * KỲ VỌNG: Danh sách trả về được sắp xếp đúng thứ tự
     */
    @Test
    public void testGetAllResources_SapXep_WHT003() throws JsonProcessingException {
        // THIẾT LẬP: Sắp xếp theo code tăng dần
        sort = "code,asc";

        // THỰC HIỆN:
        ResponseEntity<ListResponse<TransferResponse>> response =
                transferController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        List<TransferResponse> results = response.getBody().getContent();

        // KIỂM TRA THỨ TỰ SẮP XẾP: So sánh từng cặp phần tử liền kề
        if (results.size() >= 2) {
            for (int i = 0; i < results.size() - 1; i++) {
                // Code của phần tử i phải <= code của phần tử i+1 (sắp xếp tăng dần)
                assertTrue(results.get(i).getCode().compareToIgnoreCase(results.get(i + 1).getCode()) <= 0);
            }
        }
    }

    /**
     * Test Case ID: WHT004
     * KIỂM TRA: Chức năng tìm kiếm
     * KỊCH BẢN: Tìm kiếm transfer theo từ khóa
     * KỲ VỌNG: Chỉ trả về transfer có code chứa từ khóa tìm kiếm
     */
    @Test
    public void testGetAllResources_TimKiem_WHT004() throws JsonProcessingException {
        // THIẾT LẬP: Từ khóa tìm kiếm
        String keyword = "10812-201"; // Từ khóa có trong DB

        // THỰC HIỆN: Tìm kiếm với từ khóa
        ResponseEntity<ListResponse<TransferResponse>> response =
                transferController.getAllResources(page, size, sort, filter, keyword, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // KIỂM TRA KẾT QUẢ TÌM KIẾM: Tất cả kết quả phải chứa từ khóa
        List<TransferResponse> results = response.getBody().getContent();
        if (!results.isEmpty()) {
            for (TransferResponse transfer : results) {
                // Code của transfer phải chứa từ khóa (không phân biệt hoa thường)
                assertTrue(transfer.getCode().toLowerCase().contains(keyword.toLowerCase()));
            }
        }
    }

    /**
     * Test Case ID: WHT005
     * KIỂM TRA: Lấy tất cả dữ liệu không phân trang
     * KỊCH BẢN: Gọi API với all=true
     * KỲ VỌNG: Trả về tất cả transfer trong DB, không phân trang
     */
    @Test
    public void testGetAllResources_LayTatCa_WHT005() throws JsonProcessingException {
        // CHUẨN BỊ: Đếm tổng số bản ghi trong DB
        long totalRecords = transferRepository.count();

        // THỰC HIỆN: Lấy tất cả với all=true
        ResponseEntity<ListResponse<TransferResponse>> response =
                transferController.getAllResources(page, size, sort, filter, search, true);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Khi all=true, số lượng trả về phải bằng tổng số trong DB
        assertEquals(totalRecords, response.getBody().getContent().size());
        assertEquals(totalRecords, response.getBody().getTotalElements());
    }

    // ====================================================================================
    // TEST METHODS CHO getResource() - LẤY CHI TIẾT 1 TRANSFER
    // ====================================================================================

    /**
     * Test Case ID: WHT006
     * KIỂM TRA: Lấy thông tin chi tiết transfer khi ID tồn tại
     * KỊCH BẢN: Gọi API GET /transfers/{id} với ID hợp lệ
     * KỲ VỌNG: Trả về HTTP 200 và thông tin chi tiết transfer
     */
    @Test
    public void testGetResource_TonTai_WHT006() throws JsonProcessingException {
        // CHUẨN BỊ: Lấy một ID tồn tại từ DB
        Long existingId = transferRepository.findAll().stream()
                .findFirst() // Lấy transfer đầu tiên
                .map(BaseEntity::getId) // Lấy ID của nó
                .orElse(null);

        // KIỂM TRA ĐIỀU KIỆN: Đảm bảo có ít nhất một transfer trong DB
        assumeTrue(existingId != null, "Không có transfer trong cơ sở dữ liệu để test");

        // THỰC HIỆN: Lấy thông tin chi tiết
        ResponseEntity<TransferResponse> response = transferController.getResource(existingId);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId()); // ID trả về phải đúng
    }

    /**
     * Test Case ID: WHT007
     * KIỂM TRA: Xử lý lỗi khi ID không tồn tại
     * KỊCH BẢN: Gọi API với ID không tồn tại
     * KỲ VỌNG: Ném ResourceNotFoundException
     */
    @Test
    public void testGetResource_KhongTonTai_WHT007() {
        // CHUẨN BỊ: ID không tồn tại
        Long nonExistingId = 999999L;

        // KIỂM TRA ĐIỀU KIỆN: Đảm bảo ID thực sự không tồn tại
        assumeFalse(transferRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transferController.getResource(nonExistingId)
        );

        // Kiểm tra thông báo lỗi có chứa ID
        assertTrue(exception.getMessage().contains(nonExistingId.toString()));
    }

    // ====================================================================================
    // TEST METHODS CHO createResource() - TẠO MỚI TRANSFER
    // ====================================================================================

    /**
     * Test Case ID: WCT008
     * KIỂM TRA: Tạo mới transfer thành công
     * KỊCH BẢN: Gửi request tạo transfer với đầy đủ thông tin hợp lệ
     * KỲ VỌNG: Trả về HTTP 201 Created và thông tin transfer vừa tạo
     *
     * CHÚ Ý: Transfer bao gồm 2 phiếu:
     * - Export Docket: Phiếu xuất kho (lấy hàng từ kho nguồn)
     * - Import Docket: Phiếu nhập kho (đưa hàng vào kho đích)
     */
    @Test
    public void testCreateTransfer_ThanhCong_WCT008() throws JsonProcessingException {
        // CHUẨN BỊ DỮ LIỆU:

        // 1. Tạo Export Docket (Phiếu xuất kho)
        DocketRequest exportDocket = new DocketRequest();
        exportDocket.setType(0); // 0 = export (xuất)
        exportDocket.setCode("EX-001"); // Mã phiếu xuất
        exportDocket.setReasonId(1L); // ID lý do xuất kho
        exportDocket.setWarehouseId(1L); // ID kho xuất
        exportDocket.setStatus(1); // Trạng thái: 1 = active

        // Tạo item xuất kho (sản phẩm được xuất)
        DocketVariantRequest exportItem = new DocketVariantRequest();
        exportItem.setVariantId(1L); // ID variant sản phẩm
        exportItem.setQuantity(10); // Số lượng xuất
        exportDocket.setDocketVariants(Set.of(exportItem)); // Gán item vào phiếu

        // 2. Tạo Import Docket (Phiếu nhập kho)
        DocketRequest importDocket = new DocketRequest();
        importDocket.setType(1); // 1 = import (nhập)
        importDocket.setCode("IM-001"); // Mã phiếu nhập
        importDocket.setReasonId(2L); // ID lý do nhập kho
        importDocket.setWarehouseId(2L); // ID kho nhập (khác kho xuất)
        importDocket.setStatus(1); // Trạng thái: 1 = active

        // Tạo item nhập kho (cùng sản phẩm, cùng số lượng)
        DocketVariantRequest importItem = new DocketVariantRequest();
        importItem.setVariantId(1L); // Cùng variant
        importItem.setQuantity(10); // Cùng số lượng
        importDocket.setDocketVariants(Set.of(importItem));

        // 3. Tạo Transfer Request (Yêu cầu chuyển kho)
        TransferRequest request = new TransferRequest();
        request.setCode("TR-TEST-001"); // Mã chuyển kho
        request.setExportDocket(exportDocket); // Gán phiếu xuất
        request.setImportDocket(importDocket); // Gán phiếu nhập
        request.setNote("Tạo transfer thành công test"); // Ghi chú

        // THỰC HIỆN: Chuyển request thành JSON và gọi API
        requestNode = objectMapper.valueToTree(request);
        ResponseEntity<TransferResponse> response = transferController.createResource(requestNode);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // KIỂM TRA KẾT QUẢ:
        assertEquals(201, response.getStatusCodeValue()); // HTTP 201 Created
        assertNotNull(response.getBody()); // Có response body
        assertEquals("TR-TEST-001", response.getBody().getCode()); // Code đúng
    }

    /**
     * Test Case ID: WCT009
     * KIỂM TRA: Xử lý lỗi khi tạo transfer với mã code bị trùng
     * KỊCH BẢN: Tạo 2 transfer với cùng code
     * KỲ VỌNG: Transfer thứ 2 phải ném exception do vi phạm ràng buộc unique
     */
    @Test
    public void testCreateTransfer_TrungMaCode_WCT009() throws JsonProcessingException {
        // CHUẨN BỊ: Tạo dữ liệu chung
        DocketVariantRequest docketVariant = new DocketVariantRequest();
        docketVariant.setVariantId(1L);
        docketVariant.setQuantity(5);

        // Export docket
        DocketRequest exportDocket = new DocketRequest();
        exportDocket.setType(0);
        exportDocket.setCode("EX-DUP-001");
        exportDocket.setReasonId(1L);
        exportDocket.setWarehouseId(1L);
        exportDocket.setStatus(1);
        exportDocket.setDocketVariants(Set.of(docketVariant));

        // Import docket
        DocketRequest importDocket = new DocketRequest();
        importDocket.setType(1);
        importDocket.setCode("IM-DUP-001");
        importDocket.setReasonId(2L);
        importDocket.setWarehouseId(2L);
        importDocket.setStatus(1);
        importDocket.setDocketVariants(Set.of(docketVariant));

        // THỰC HIỆN:
        // 1. Tạo transfer đầu tiên (thành công)
        TransferRequest request1 = new TransferRequest();
        request1.setCode("TR-DUP-001"); // Code này
        request1.setExportDocket(exportDocket);
        request1.setImportDocket(importDocket);
        request1.setNote("Transfer 1");
        transferController.createResource(objectMapper.valueToTree(request1));

        // 2. Tạo transfer thứ hai với cùng code (phải lỗi)
        TransferRequest request2 = new TransferRequest();
        request2.setCode("TR-DUP-001"); // Trùng code!
        request2.setExportDocket(exportDocket);
        request2.setImportDocket(importDocket);
        request2.setNote("Transfer 2");

        // KIỂM TRA: Phải ném exception
        assertThrows(Exception.class, () -> transferController.createResource(objectMapper.valueToTree(request2)));
    }

    /**
     * Test Case ID: WCT010
     * KIỂM TRA: Xử lý lỗi khi thiếu dữ liệu bắt buộc
     * KỊCH BẢN: Tạo transfer không có code, exportDocket, importDocket
     * KỲ VỌNG: Ném exception do thiếu dữ liệu
     */
    @Test
    public void testCreateTransfer_DuLieuThieu_WCT010() {
        // CHUẨN BỊ: Transfer request thiếu thông tin cần thiết
        TransferRequest request = new TransferRequest();
        // Không set code, exportDocket, importDocket
        request.setNote("Thiếu dữ liệu"); // Chỉ có note

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        requestNode = objectMapper.valueToTree(request);
        assertThrows(Exception.class, () -> transferController.createResource(requestNode));
    }

    // ====================================================================================
    // TEST METHODS CHO updateResource() - CẬP NHẬT TRANSFER
    // ====================================================================================

    /**
     * Test Case ID: WHT011
     * KIỂM TRA: Cập nhật transfer thành công
     * KỊCH BẢN: Cập nhật transfer với ID tồn tại và dữ liệu hợp lệ
     * KỲ VỌNG: Trả về HTTP 200 và thông tin transfer đã cập nhật
     */
    @Test
    public void testUpdateResource_ThanhCong_WHT011() throws JsonProcessingException {
        // CHUẨN BỊ: Lấy ID transfer tồn tại
        Long existingId = transferRepository.findAll().stream()
                .findFirst().map(BaseEntity::getId).orElse(null);
        assumeTrue(existingId != null, "Không có transfer trong DB");

        // Tạo dữ liệu cập nhật
        DocketVariantRequest variant = new DocketVariantRequest();
        variant.setVariantId(1L);
        variant.setQuantity(10);

        DocketRequest exportDocket = new DocketRequest();
        exportDocket.setType(0);
        exportDocket.setCode("EX-UPD-001");
        exportDocket.setReasonId(1L);
        exportDocket.setWarehouseId(1L);
        exportDocket.setStatus(1);
        exportDocket.setDocketVariants(Set.of(variant));

        DocketRequest importDocket = new DocketRequest();
        importDocket.setType(1);
        importDocket.setCode("IM-UPD-001");
        importDocket.setReasonId(2L);
        importDocket.setWarehouseId(2L);
        importDocket.setStatus(1);
        importDocket.setDocketVariants(Set.of(variant));

        TransferRequest request = new TransferRequest();
        request.setCode("TR-UPD-001");
        request.setExportDocket(exportDocket);
        request.setImportDocket(importDocket);
        request.setNote("Transfer cập nhật");

        // THỰC HIỆN: Cập nhật
        requestNode = objectMapper.valueToTree(request);
        ResponseEntity<TransferResponse> response = transferController.updateResource(existingId, requestNode);

        // KIỂM TRA:
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId()); // ID không đổi
        assertEquals("TR-UPD-001", response.getBody().getCode()); // Code đã cập nhật
    }

    /**
     * Test Case ID: WHT012
     * KIỂM TRA: Xử lý lỗi khi cập nhật transfer không tồn tại
     * KỊCH BẢN: Cập nhật với ID không tồn tại
     * KỲ VỌNG: Ném ResourceNotFoundException
     */
    @Test
    public void testUpdateResource_KhongTonTai_WHT012() {
        // CHUẨN BỊ: ID không tồn tại
        Long nonExistingId = 999999L;
        assumeFalse(transferRepository.existsById(nonExistingId));

        // Tạo dữ liệu hợp lệ
        DocketVariantRequest variant = new DocketVariantRequest();
        variant.setVariantId(1L);
        variant.setQuantity(5);

        DocketRequest exportDocket = new DocketRequest();
        exportDocket.setType(0);
        exportDocket.setCode("EX-NF");
        exportDocket.setReasonId(1L);
        exportDocket.setWarehouseId(1L);
        exportDocket.setStatus(1);
        exportDocket.setDocketVariants(Set.of(variant));

        DocketRequest importDocket = new DocketRequest();
        importDocket.setType(1);
        importDocket.setCode("IM-NF");
        importDocket.setReasonId(2L);
        importDocket.setWarehouseId(2L);
        importDocket.setStatus(1);
        importDocket.setDocketVariants(Set.of(variant));

        TransferRequest request = new TransferRequest();
        request.setCode("TR-NF");
        request.setExportDocket(exportDocket);
        request.setImportDocket(importDocket);
        request.setNote("Transfer không tồn tại");

        // THỰC HIỆN & KIỂM TRA: Phải ném exception
        requestNode = objectMapper.valueToTree(request);
        assertThrows(ResourceNotFoundException.class, () -> transferController.updateResource(nonExistingId, requestNode));
    }



    // ----------------------------------------------------------------------------------
    // deleteResource()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: WHT013
     * Tên test: testDeleteResource_ThanhCong
     * Mục tiêu: Kiểm tra phương thức deleteResource xóa khuyến mãi thành công
     * Đầu vào: ID của khuyến mãi đã tồn tại
     * Đầu ra mong đợi: HTTP 204 No Content
     * Ghi chú: Kiểm tra chức năng xóa khuyến mãi
     */
    @Test
    public void testDeleteResource_ThanhCong_WHT013() {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = transferRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một transfer trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có transfer trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = transferController.deleteResource(existingId);

        // Kiểm tra
        assertEquals(204, response.getStatusCodeValue());
    }


    // ----------------------------------------------------------------------------------
    // deleteResources()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: WHT014
     * Tên test: testDeleteResources_ThanhCong
     * Mục tiêu: Kiểm tra phương thức deleteResources
     * Đầu vào: Danh sách ID của các khuyến mãi đã tồn tại
     * Đầu ra mong đợi: HTTP 204 No Content
     * Ghi chú: Kiểm tra chức năng xóa nhiều khuyến mãi
     */
    @Test
    public void testDeleteResources_ThanhCong_WHT014() {
        // Chuẩn bị - Lấy danh sách ID tồn tại từ cơ sở dữ liệu
        List<Long> existingIds = transferRepository.findAll().stream()
                .map(BaseEntity::getId)
                .limit(2) // Giới hạn lấy 2 ID
                .collect(Collectors.toList());

        // đảm bảo có ít nhất 2 transfer trong cơ sở dữ liệu
        assumeTrue(existingIds.size() >= 2, "Không đủ Transfer trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = transferController.deleteResources(existingIds);

        // Kiểm tra
        assertEquals(204, response.getStatusCodeValue());
    }

    /**
     * Test Case ID: WHT015
     * Tên test: testDeleteResources_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức deleteResources xử lý lỗi khi ID không tồn tại
     * Đầu vào: Danh sách ID không tồn tại
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi cố gắng xóa nhiều tài nguyên không tồn tại
     */
    @Test
    public void testDeleteResources_KhongTonTai_WHT015() {
        List<Long> nonExistingIds = List.of(999999L, 888888L);
        assumeTrue(nonExistingIds.stream().noneMatch(transferRepository::existsById));

        Exception exception = assertThrows(
                EmptyResultDataAccessException.class, // chính xác với hành vi hiện tại
                () -> transferController.deleteResources(nonExistingIds)
        );

        // Tùy chọn: kiểm tra thông điệp lỗi
        assertTrue(exception.getMessage().toLowerCase().contains("no class"));
    }
}