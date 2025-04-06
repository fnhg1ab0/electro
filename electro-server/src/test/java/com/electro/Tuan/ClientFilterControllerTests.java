package com.electro.Tuan;

import com.electro.controller.client.ClientFilterController;
import com.electro.dto.client.ClientBrandResponse;
import com.electro.dto.client.ClientFilterResponse;
import com.electro.repository.product.BrandRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * File test: ClientFilterControllerTest.java
 * Class được test: ClientFilterController
 * <p>
 * Mục tiêu: Test trực tiếp các hàm của ClientFilterController bằng cách gọi các phương thức
 * getFilterByCategorySlug và getFilterBySearchQuery với các input khác nhau. Mỗi test case bao gồm:
 * - Mã Test Case với các ký tự đầu viết tắt file test case hiện tại (ví dụ: CFT001, CFT002, ...)
 * - Mục tiêu của test case
 * - Input: các tham số truyền vào
 * - Expected Output: kết quả mong đợi (HTTP status, nội dung response hoặc exception)
 * <p>
 * Các hàm được test:
 * - getFilterByCategorySlug: lấy danh sách nhãn hiệu theo slug danh mục.
 * - getFilterBySearchQuery: lấy danh sách nhãn hiệu theo từ khóa tìm kiếm.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClientFilterControllerTests {

    @Autowired
    private BrandRepository brandRepository; // Inject đối tượng repository

    @Autowired
    private ClientFilterController clientFilterController; // Inject controller

    /**
     * Test Case ID: CFT001
     * Mục tiêu: Kiểm tra getFilterByCategorySlug trả về danh sách các nhãn hiệu với dữ liệu hợp lệ.
     * Input: slug = "electronics" và brandRepository trả về danh sách không rỗng.
     * Expected Output: ResponseEntity với status 200, body chứa ClientFilterResponse có danh sách ClientBrandResponse đúng.
     */
    @Test
    public void testGetFilterByCategorySlug_NonEmptyList_CFT001() throws JsonProcessingException {
        String slug = "laptop";
        int statusCode = 200;
        int brandSize = 5;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterByCategorySlug(slug);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertEquals(brandSize, filterBrands.size(), "Expected 2 brands in the response");
    }

    /**
     * Test Case ID: CFT002
     * Mục tiêu: Kiểm tra getFilterByCategorySlug trả về danh sách rỗng khi không tìm thấy nhãn hiệu.
     * Input: slug = "unknown" và brandRepository trả về danh sách rỗng.
     * Expected Output: ResponseEntity với status 200, body chứa ClientFilterResponse có danh sách rỗng.
     */
    @Test
    public void testGetFilterByCategorySlug_EmptyList_CFT002() throws JsonProcessingException {
        String slug = "unknown";
        int statusCode = 200;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterByCategorySlug(slug);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertTrue(filterBrands.isEmpty(), "Expected empty list of brands");
    }

    /**
     * Test Case ID: CFT003
     * Mục tiêu: Kiểm tra getFilterByCategorySlug khi slug là null.
     * Input: slug = null.
     * Expected Output: Behavior phụ thuộc vào repository, giả sử trả về danh sách rỗng.
     * Ghi chú: Controller không kiểm tra null nên giá trị null được truyền tới repository.
     */
    @Test
    public void testGetFilterByCategorySlug_NullSlug_CFT003() throws JsonProcessingException {
        String slug = null;
        int statusCode = 200;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterByCategorySlug(slug);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK even with null slug");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertTrue(filterBrands.isEmpty(), "Expected empty list of brands for null slug");
    }

    /**
     * Test Case ID: CFT004
     * Mục tiêu: Kiểm tra getFilterBySearchQuery trả về danh sách các nhãn hiệu với dữ liệu hợp lệ.
     * Input: query = "apple" và brandRepository trả về danh sách không rỗng.
     * Expected Output: ResponseEntity với status 200, body chứa ClientFilterResponse có danh sách ClientBrandResponse đúng.
     */
    @Test
    public void testGetFilterBySearchQuery_NonEmptyList_CFT004() throws JsonProcessingException {
        String query = "Dell XPS";
        int statusCode = 200;
        int brandSize = 3;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterBySearchQuery(query);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertEquals(brandSize, filterBrands.size(), "Expected 1 brand in the response");
    }

    /**
     * Test Case ID: CFT005
     * Mục tiêu: Kiểm tra getFilterBySearchQuery trả về danh sách rỗng khi không tìm thấy nhãn hiệu.
     * Input: query = "nonexistent" và brandRepository trả về danh sách rỗng.
     * Expected Output: ResponseEntity với status 200, body chứa ClientFilterResponse có danh sách rỗng.
     */
    @Test
    public void testGetFilterBySearchQuery_EmptyList_CFT005() throws JsonProcessingException {
        String query = "nonexistent";
        int statusCode = 200;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterBySearchQuery(query);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertTrue(filterBrands.isEmpty(), "Expected empty list of brands");
    }

    /**
     * Test Case ID: CFT006
     * Mục tiêu: Kiểm tra getFilterBySearchQuery khi query là null.
     * Input: query = null.
     * Expected Output: Behavior phụ thuộc vào repository, giả sử trả về danh sách rỗng.
     * Ghi chú: Controller không kiểm tra null nên giá trị null được truyền tới repository.
     */
    @Test
    public void testGetFilterBySearchQuery_NullQuery_CFT006() throws JsonProcessingException {
        String query = null;
        int statusCode = 200;

        ResponseEntity<ClientFilterResponse> response = clientFilterController.getFilterBySearchQuery(query);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK even with null query");
        ClientFilterResponse clientFilterResponse = response.getBody();
        assertNotNull(clientFilterResponse, "Response body should not be null");
        List<ClientBrandResponse> filterBrands = clientFilterResponse.getFilterBrands();
        assertNotNull(filterBrands, "Filter brands should not be null");
        assertTrue(filterBrands.isEmpty(), "Expected empty list of brands for null query");
    }
}