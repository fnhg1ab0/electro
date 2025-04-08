package com.electro.Tuan;

import com.electro.controller.promotion.PromotionController;
import com.electro.dto.promotion.PromotionCheckingResponse;
import com.electro.service.promotion.PromotionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.electro.utils.TestDataFactory.objectMapperLogger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * File test: PromotionControllerTest.java
 * Class được test: PromotionController
 * <p>
 * Mục tiêu: Test trực tiếp các hàm của PromotionController bằng cách gọi phương thức
 * checkCanCreatePromotionForProduct với các input khác nhau. Mỗi test case bao gồm:
 * - Mã Test Case với các ký tự đầu viết tắt file test case hiện tại (ví dụ: PCT001, PCT002, ...)
 * - Mục tiêu của test case
 * - Input: các tham số truyền vào
 * - Expected Output: kết quả mong đợi (HTTP status, nội dung response hoặc exception)
 * <p>
 * Các hàm được test:
 * - checkCanCreatePromotionForProduct: kiểm tra xem sản phẩm có thể tạo khuyến mãi hay không.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PromotionControllerTests {

    @Autowired
    private PromotionService promotionService; // Service thật kết nối với DB

    @Autowired
    private PromotionController promotionController; // Controller thật

    /**
     * Test Case ID: PCT001
     * Mục tiêu: Kiểm tra trường hợp sản phẩm có thể tạo khuyến mãi.
     * Input: productId = 1, startDate = hiện tại, endDate = startDate + 1 giờ.
     * Expected Output: ResponseEntity với status 200 và body chứa PromotionCheckingResponse với promotionable = true.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_PromotionAllowed_TC001() {
        Long productId = 1L;
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(3600);

        ResponseEntity<PromotionCheckingResponse> response =
                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);

        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().isPromotionable(), "Promotion should be allowed (true)");
    }

    /**
     * Test Case ID: PCT002
     * Mục tiêu: Kiểm tra trường hợp sản phẩm không thể tạo khuyến mãi.
     * Input: productId = 999 (không tồn tại), startDate = hiện tại, endDate = startDate + 1 giờ.
     * Expected Output: ResponseEntity với status 200 và body chứa PromotionCheckingResponse với promotionable = false.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_PromotionNotAllowed_TC002() throws JsonProcessingException {
        Long productId = 9999L;
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(3600);

        ResponseEntity<PromotionCheckingResponse> response =
                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);

        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isPromotionable(), "Promotion should not be allowed (false)");
    }

    /**
     * Test Case ID: PCT003
     * Mục tiêu: Kiểm tra trường hợp productId là null.
     * Input: productId = null, startDate = hiện tại, endDate = startDate + 1 giờ.
     * Expected Output: ResponseEntity với status 200, response theo logic service với DB thật.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_NullProductId_TC003() {
        Long productId = null;
        Instant startDate = Instant.now();
        Instant endDate = startDate.plusSeconds(3600);

        ResponseEntity<PromotionCheckingResponse> response =
                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);

        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK even with null productId");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    /**
     * Test Case ID: PCT004
     * Mục tiêu: Kiểm tra trường hợp startDate là null.
     * Input: productId = 1, startDate = null, endDate = hiện tại + 1 giờ.
     * Expected Output: ResponseEntity với status 200, response theo logic service với DB thật.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_NullStartDate_TC004() {
        Long productId = 1L;
        Instant startDate = null;
        Instant endDate = Instant.now().plusSeconds(3600);

//        ResponseEntity<PromotionCheckingResponse> response =
//                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);
//
//        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK even with null startDate");
//        assertNotNull(response.getBody(), "Response body should not be null");

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);
        }, "Expected IllegalArgumentException when startDate is null");
    }

    /**
     * Test Case ID: PCT005
     * Mục tiêu: Kiểm tra trường hợp endDate là null.
     * Input: productId = 1, startDate = hiện tại, endDate = null.
     * Expected Output: ResponseEntity với status 200, response theo logic service với DB thật.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_NullEndDate_TC005() {
        Long productId = 1L;
        Instant startDate = Instant.now();
        Instant endDate = null;

//        ResponseEntity<PromotionCheckingResponse> response =
//                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);
        }, "Expected IllegalArgumentException when endDate is null");
    }

    /**
     * Test Case ID: PCT006
     * Mục tiêu: Kiểm tra trường hợp startDate bằng endDate.
     * Input: productId = 1, startDate = now, endDate = now.
     * Expected Output: ResponseEntity với status 200, response theo logic service với DB thật.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_StartDateEqualsEndDate_TC006() {
        Long productId = 1L;
        Instant now = Instant.now();

        ResponseEntity<PromotionCheckingResponse> response =
                promotionController.checkCanCreatePromotionForProduct(productId, now, now);

        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK when startDate equals endDate");
        assertNotNull(response.getBody(), "Response body should not be null");
    }

    /**
     * Test Case ID: PCT007
     * Mục tiêu: Kiểm tra trường hợp sản phẩm đang có khuyến mãi active.
     * Input: productId = 2 (sản phẩm có khuyến mãi), startDate = hiện tại, endDate = startDate + 1 giờ.
     * Expected Output: ResponseEntity với status 200 và body chứa PromotionCheckingResponse với promotionable = false.
     */
    @Test
    public void testCheckCanCreatePromotionForProduct_ExistingActivePromotion_TC007() {
        Long productId = 1L; // Giả sử product id 2 đang có khuyến mãi
        Instant startDate = Instant.parse("2023-03-08T00:00:00Z");
        Instant endDate = Instant.parse("2023-03-19T00:00:00Z");

        ResponseEntity<PromotionCheckingResponse> response =
                promotionController.checkCanCreatePromotionForProduct(productId, startDate, endDate);

        assertEquals(200, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isPromotionable(), "Promotion should not be allowed for product with active promotion");
    }
}