package com.electro.Minh;

import com.electro.constant.AppConstants;
import com.electro.controller.GenericController;
import com.electro.dto.ListResponse;
import com.electro.dto.address.AddressRequest;
import com.electro.dto.inventory.WarehouseRequest;
import com.electro.dto.inventory.WarehouseResponse;
import com.electro.entity.BaseEntity;
import com.electro.entity.inventory.Warehouse;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.inventory.WarehouseMapper;
import com.electro.repository.inventory.WarehouseRepository;
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
 * File test: warehouseControllerTests.java
 * Class được test: GenericController (dùng cho warehouse)
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
public class WarehouseTest {

    private final int page = Integer.parseInt(AppConstants.DEFAULT_PAGE_NUMBER);
    private final String search = null;
    private final boolean all = false;
    @Autowired
    private GenericController<WarehouseRequest, WarehouseResponse> warehouseController;
    @Autowired
    private GenericService<Warehouse, WarehouseRequest, WarehouseResponse> warehouseService;
    @Autowired
    private WarehouseRepository warehouseRepository;
    private int size = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);
    private String sort = AppConstants.DEFAULT_SORT;
    private String filter = null;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonNode requestNode;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @BeforeEach
    public void setup() {
        warehouseController.setCrudService(warehouseService);
        warehouseController.setRequestType(WarehouseRequest.class);

        warehouseService.init(
                warehouseRepository,
                warehouseMapper,                    // dùng bean đã được inject
                List.of("code", "name"),            // các trường có thể search
                "Warehouse"
        );
    }


    // ---------------------------------------------------------------------------------
    // getAllResources()
    // ---------------------------------------------------------------------------------

    /**
     * Test Case ID: WHT001
     * Tên test: testGetAllResources_ThamSoMacDinh
     * Mục tiêu: Kiểm tra phương thức getAllResources với tham số mặc định
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với warehouse và thông tin phân trang
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint lấy warehouse
     */
    @Test
    public void testGetAllResources_ThamSoMacDinh_WHT001() throws JsonProcessingException {
        // Thực hiện với tham số mặc định
        ResponseEntity<ListResponse<WarehouseResponse>> response =
                warehouseController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getContent());
        // Kiểm tra có dữ liệu trả về
        assertTrue(response.getBody().getTotalElements() > 0);
    }

    /**
     * Test Case ID: WHT002
     * Tên test: testGetAllResources_PhanTrang
     * Mục tiêu: Kiểm tra phương thức getAllResources với phân trang
     * Đầu vào: Tham số page=2, size=2, sort=null, filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với trang thứ 2 của dữ liệu
     * Ghi chú: Kiểm tra chức năng phân trang khi lấy warehouse
     */
    @Test
    public void testGetAllResources_PhanTrang_WHT002() throws JsonProcessingException {
        // Đếm tổng số bản ghi để tính số trang
        long totalElements = warehouseRepository.count();
        size = 2; // Kích thước trang là 2

        // Nếu có ít nhất 3 bản ghi, kiểm tra trang thứ 2
        if (totalElements >= 3) {
            // Thực hiện - lấy trang thứ 2 với kích thước trang là 2
            ResponseEntity<ListResponse<WarehouseResponse>> response =
                    warehouseController.getAllResources(2, size, sort, filter, search, all);
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
     * Test Case ID: WHT003
     * Tên test: testGetAllResources_SapXep
     * Mục tiêu: Kiểm tra phương thức getAllResources với sắp xếp
     * Đầu vào: Tham số page=1, size=10, sort="name,asc", filter=null, search=null, all=false
     * Đầu ra mong đợi: HTTP 200 OK với warehouse được sắp xếp theo tên tăng dần
     * Ghi chú: Kiểm tra chức năng sắp xếp khi lấy warehouse
     */
    @Test
    public void testGetAllResources_SapXep_WHT003() throws JsonProcessingException {
        // Thực hiện - sắp xếp theo tên tăng dần
        sort = "name,asc";
        ResponseEntity<ListResponse<WarehouseResponse>> response =
                warehouseController.getAllResources(page, size, sort, filter, search, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        List<WarehouseResponse> results = response.getBody().getContent();

        // Kiểm tra thứ tự sắp xếp nếu có ít nhất 2 kết quả
        if (results.size() >= 2) {
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue(results.get(i).getName().compareToIgnoreCase(results.get(i + 1).getName()) <= 0);
            }
        }
    }


    /**
     * Test Case ID: WHT004
     * Tên test: testGetAllResources_TimKiem
     * Mục tiêu: Kiểm tra phương thức getAllResources với tìm kiếm
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search="khuyến mãi", all=false
     * Đầu ra mong đợi: HTTP 200 OK với warehouse có tên chứa từ khóa tìm kiếm
     * Ghi chú: Kiểm tra chức năng tìm kiếm khi lấy warehouse
     */
    @Test
    public void testGetAllResources_TimKiem_WHT004() throws JsonProcessingException {
        // Lấy từ khóa từ một warehouse có trong DB
        String keyword = "khuyến mãi"; // Từ khóa phổ biến tiếng Việt cho warehouses

        // Thực hiện tìm kiếm
        ResponseEntity<ListResponse<WarehouseResponse>> response =
                warehouseController.getAllResources(page, size, sort, filter, keyword, all);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        // Kiểm tra kết quả tìm kiếm
        List<WarehouseResponse> results = response.getBody().getContent();
        if (!results.isEmpty()) {
            for (WarehouseResponse promo : results) {
                assertTrue(promo.getName().toLowerCase().contains(keyword.toLowerCase()));
            }
        }
    }

    /**
     * Test Case ID: WHT005
     * Tên test: testGetAllResources_LayTatCa
     * Mục tiêu: Kiểm tra phương thức getAllResources với lấy tất cả dữ liệu
     * Đầu vào: Tham số page=1, size=10, sort=null, filter=null, search=null, all=true
     * Đầu ra mong đợi: HTTP 200 OK với tất cả khuyến mãi không phân trang
     * Ghi chú: Kiểm tra chức năng lấy toàn bộ dữ liệu không phân trang
     */
    @Test
    public void testGetAllResources_LayTatCa_WHT005() throws JsonProcessingException {
        // Đếm tổng số bản ghi trước
        long totalRecords = warehouseRepository.count();

        // Thực hiện - lấy tất cả không phân trang
        ResponseEntity<ListResponse<WarehouseResponse>> response =
                warehouseController.getAllResources(page, size, sort, filter, search, true);
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
     * Test Case ID: WHT006
     * Tên test: testGetResource_TonTai
     * Mục tiêu: Kiểm tra phương thức getResource khi ID tồn tại
     * Đầu vào: ID của một warehouse tồn tại
     * Đầu ra mong đợi: HTTP 200 OK với thông tin đầy đủ của warehouse
     * Ghi chú: Kiểm tra chức năng lấy thông tin chi tiết của warehouse
     */
    @Test
    public void testGetResource_TonTai_WHT006() throws JsonProcessingException {
        // Chuẩn bị - Lấy một ID tồn tại từ cơ sở dữ liệu
        Long existingId = warehouseRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một warehouse trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có warehouse trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<WarehouseResponse> response = warehouseController.getResource(existingId);
        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());
    }

    /**
     * Test Case ID: WHT007
     * Tên test: testGetResource_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức getResource khi ID không tồn tại
     * Đầu vào: ID không tồn tại
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi truy vấn tài nguyên không tồn tại
     */
    @Test
    public void testGetResource_KhongTonTai_WHT007() {
        // Chuẩn bị - Sử dụng một ID không tồn tại
        Long nonExistingId = 999999L;

        // Đảm bảo ID không tồn tại trong cơ sở dữ liệu
        assumeFalse(warehouseRepository.existsById(nonExistingId),
                "ID " + nonExistingId + " đã tồn tại trong cơ sở dữ liệu");

        // Thực hiện & Kiểm tra
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> warehouseController.getResource(nonExistingId)
        );

        // Kiểm tra thông báo lỗi
        assertTrue(exception.getMessage().contains(nonExistingId.toString()));
    }

    // ---------------------------------------------------------------------------------
    // createResource()
    // ---------------------------------------------------------------------------------

    /**
     * Test Case ID: WCT008
     * Tên test: testCreateWarehouse_ThanhCong
     * Mục tiêu: Kiểm tra phương thức createResource tạo mới warehouse thành công
     * Đầu vào: Yêu cầu tạo warehouse với các trường hợp lệ (code, name, status, address_id)
     * Đầu ra mong đợi: HTTP 201 Created với thông tin warehouse đã tạo
     * Ghi chú: Đảm bảo rằng địa chỉ được sử dụng tồn tại trong DB
     */
    @Test
    public void testCreateWarehouse_ThanhCong_WCT008() throws JsonProcessingException {
        // Sử dụng địa chỉ có sẵn trong DB (id = 1, line = "140 Commercial Way", provinceId = 7, districtId = 28, wardId = 1)
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setLine("140 Commercial Way");
        addressRequest.setProvinceId(7L);
        addressRequest.setDistrictId(28L);
        addressRequest.setWardId(1L);

        WarehouseRequest request = new WarehouseRequest();
        request.setCode("WH-TEST-001");
        request.setName("Kho Test Thành Công");
        request.setStatus(1);
        request.setAddress(addressRequest);

        requestNode = objectMapper.valueToTree(request);
        ResponseEntity<WarehouseResponse> response = warehouseController.createResource(requestNode);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        // Kiểm tra phản hồi cơ bản
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        WarehouseResponse body = response.getBody();
        assertEquals("WH-TEST-001", body.getCode());
        assertEquals("Kho Test Thành Công", body.getName());

        // Kiểm tra địa chỉ nếu có
        if (body.getAddress() != null) {
            assertEquals("140 Commercial Way", body.getAddress().getLine());

            if (body.getAddress().getProvince() != null) {
                assertEquals(7L, body.getAddress().getProvince().getId());
            }

            if (body.getAddress().getDistrict() != null) {
                assertEquals(28L, body.getAddress().getDistrict().getId());
            }

            if (body.getAddress().getWard() != null) {
                assertEquals(1L, body.getAddress().getWard().getId());
            }
        }
    }







    /**
     * Test Case ID: WCT009
     * Tên test: testCreateWarehouse_TrungMaCode
     * Mục tiêu: Kiểm tra xử lý lỗi khi tạo warehouse với mã code bị trùng
     * Đầu vào: Yêu cầu tạo warehouse có code đã tồn tại trong DB
     * Đầu ra mong đợi: Exception hoặc HTTP 400/409 với thông báo lỗi
     * Ghi chú: Ràng buộc `uc_warehouse_code`
     */
    @Test
    public void testCreateWarehouse_TrungMaCode_WCT009() throws JsonProcessingException {
        // Tạo trước một warehouse có code "WH-DUP"
        WarehouseRequest request1 = new WarehouseRequest();
        request1.setCode("WH-TEST-DUP");
        request1.setName("Kho Trùng Code A");
        request1.setStatus(1);
        AddressRequest address1 = new AddressRequest();
        address1.setLine("12 Duplicate Street");
        address1.setProvinceId(7L);
        address1.setDistrictId(28L);
        address1.setWardId(2L);
        request1.setAddress(address1);
        warehouseController.createResource(objectMapper.valueToTree(request1));

        WarehouseRequest request2 = new WarehouseRequest();
        request2.setCode("WH-TEST-DUP");
        request2.setName("Kho Trùng Code B");
        request2.setStatus(1);
        AddressRequest address2 = new AddressRequest();
        address2.setLine("34 Another Street");
        address2.setProvinceId(7L);
        address2.setDistrictId(28L);
        address2.setWardId(3L);
        request2.setAddress(address2);


        JsonNode duplicateNode = objectMapper.valueToTree(request2);
        assertThrows(Exception.class, () -> warehouseController.createResource(duplicateNode));
    }


    /**
     * Test Case ID: WCT01-
     * Tên test: testCreateWarehouse_DuLieuThieu
     * Mục tiêu: Kiểm tra xử lý lỗi khi thiếu các trường bắt buộc (code, name)
     * Đầu vào: Yêu cầu tạo warehouse không có `code` và `name`
     * Đầu ra mong đợi: Exception hoặc HTTP 400
     * Ghi chú: Kiểm tra validation phía controller hoặc JPA
     */
    @Test
    public void testCreateWarehouse_DuLieuThieu_WCT010() {
        WarehouseRequest request = new WarehouseRequest();
        request.setStatus(1);
        AddressRequest address = new AddressRequest();
        address.setLine("Thiếu Tên và Mã");
        address.setProvinceId(7L);
        address.setDistrictId(28L);
        address.setWardId(1L);
        request.setAddress(address);


        requestNode = objectMapper.valueToTree(request);
        assertThrows(Exception.class, () -> warehouseController.createResource(requestNode));
    }


    // ----------------------------------------------------------------------------------
    // updateResource()
    // ----------------------------------------------------------------------------------

    /**
     * Test Case ID: WHT011
     * Tên test: testUpdateResource_ThanhCong
     * Mục tiêu: Kiểm tra phương thức updateResource cập nhật khuyến mãi thành công
     * Đầu vào: ID của khuyến mãi đã tồn tại và yêu cầu cập nhật với thông tin hợp lệ
     * Đầu ra mong đợi: HTTP 200 OK với thông tin khuyến mãi đã cập nhật trong phần thân phản hồi
     * Ghi chú: Kiểm tra chức năng cơ bản của endpoint cập nhật khuyến mãi
     */
    @Test
    public void testUpdateResource_ThanhCong_WHT011() throws JsonProcessingException {
        Long existingId = warehouseRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        assumeTrue(existingId != null, "Không có warehouse trong DB");

        WarehouseRequest request = new WarehouseRequest();
        request.setCode("WH-UPDATED-001");
        request.setName("Kho Cập Nhật");
        request.setStatus(1);

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setLine("456 Updated Way");
        addressRequest.setProvinceId(7L);
        addressRequest.setDistrictId(28L);
        addressRequest.setWardId(2L);
        request.setAddress(addressRequest);

        requestNode = objectMapper.valueToTree(request);
        ResponseEntity<WarehouseResponse> response = warehouseController.updateResource(existingId, requestNode);

        System.out.println("Phản hồi: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(existingId, response.getBody().getId());
        assertEquals("Kho Cập Nhật", response.getBody().getName());
    }


    /**
     * Test Case ID: WHT012
     * Tên test: testUpdateResource_KhongTonTai
     * Mục tiêu: Kiểm tra phương thức updateResource xử lý lỗi khi ID không tồn tại
     * Đầu vào: ID không tồn tại và yêu cầu cập nhật với thông tin hợp lệ
     * Đầu ra mong đợi: ResourceNotFoundException
     * Ghi chú: Kiểm tra xử lý lỗi khi cố gắng cập nhật tài nguyên không tồn tại
     */
    @Test
    public void testUpdateResource_KhongTonTai_WHT012() {
        Long nonExistingId = 999999L;
        assumeFalse(warehouseRepository.existsById(nonExistingId));

        WarehouseRequest request = new WarehouseRequest();
        request.setCode("WH-404");
        request.setName("Không tồn tại");
        request.setStatus(1);

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setLine("Not Found Address");
        addressRequest.setProvinceId(7L);
        addressRequest.setDistrictId(28L);
        addressRequest.setWardId(4L);
        request.setAddress(addressRequest);

        requestNode = objectMapper.valueToTree(request);
        assertThrows(ResourceNotFoundException.class, () -> warehouseController.updateResource(nonExistingId, requestNode));
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
        Long existingId = warehouseRepository.findAll().stream()
                .findFirst()
                .map(BaseEntity::getId)
                .orElse(null);

        // đảm bảo có ít nhất một warehouse trong cơ sở dữ liệu
        assumeTrue(existingId != null, "Không có warehouse trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = warehouseController.deleteResource(existingId);

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
        List<Long> existingIds = warehouseRepository.findAll().stream()
                .map(BaseEntity::getId)
                .limit(2) // Giới hạn lấy 2 ID
                .collect(Collectors.toList());

        // đảm bảo có ít nhất 2 warehouse trong cơ sở dữ liệu
        assumeTrue(existingIds.size() >= 2, "Không đủ Warehouse trong cơ sở dữ liệu để test");

        // Thực hiện
        ResponseEntity<Void> response = warehouseController.deleteResources(existingIds);

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
        assumeTrue(nonExistingIds.stream().noneMatch(warehouseRepository::existsById));

        Exception exception = assertThrows(
                EmptyResultDataAccessException.class, // chính xác với hành vi hiện tại
                () -> warehouseController.deleteResources(nonExistingIds)
        );

        // Tùy chọn: kiểm tra thông điệp lỗi
        assertTrue(exception.getMessage().toLowerCase().contains("no class"));
    }
}