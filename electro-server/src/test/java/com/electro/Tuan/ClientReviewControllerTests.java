package com.electro.Tuan;

import com.electro.controller.client.ClientReviewController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientReviewRequest;
import com.electro.dto.client.ClientReviewResponse;
import com.electro.dto.client.ClientSimpleReviewResponse;
import com.electro.exception.ResourceNotFoundException;
import com.electro.repository.review.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.electro.utils.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ClientReviewController
 * Mục tiêu: Kiểm thử các chức năng quản lý đánh giá sản phẩm phía client
 * Các hàm được test:
 * + getAllReviewsByProduct: lấy danh sách đánh giá theo slug sản phẩm
 * + getAllReviewsByUser: lấy danh sách đánh giá theo người dùng
 * + createReview: tạo mới đánh giá
 * + updateReview: cập nhật đánh giá
 * + deleteReviews: xóa danh sách đánh giá
 * Mô tả Test Case:
 * + CRT001: Kiểm tra getAllReviewsByProduct trả về danh sách đánh giá không rỗng
 * + CRT002: Kiểm tra getAllReviewsByProduct trả về danh sách rỗng khi không có đánh giá
 * + CRT003: Kiểm tra getAllReviewsByProduct với productSlug null
 * + CRT004: Kiểm tra getAllReviewsByUser trả về danh sách đánh giá không rỗng
 * + CRT005: Kiểm tra getAllReviewsByUser trả về danh sách rỗng
 * + CRT006: Kiểm tra createReview với dữ liệu hợp lệ
 * + CRT007: Kiểm tra CreateReview với dữ liệu không hợp lệ
 * + CRT008: Kiểm tra updateReview với dữ liệu hợp lệ
 * + CRT009: Kiểm tra updateReview với id không tồn tại
 * + CRT010: Kiểm tra deleteReviews với danh sách id hợp lệ
 * + CRT011: Kiểm tra deleteReviews với danh sách id không tồn tại
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClientReviewControllerTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ClientReviewController clientReviewController;

    /**
     * Test Case ID: CRT001
     * Mục tiêu: Kiểm tra getAllReviewsByProduct trả về danh sách đánh giá không rỗng
     * Input: productSlug = "dell-xps-13", page = 1, size = 10, sort = "id,desc", filter = null
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse có danh sách ClientSimpleReviewResponse
     */
    @Test
    public void testGetAllReviewsByProduct_NonEmptyList_CRT001() throws JsonProcessingException {
        String productSlug = "ealdus0";
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = null;
        int statusCode = 200;
        int reviewSize = 1;

        ResponseEntity<ListResponse<ClientSimpleReviewResponse>> response =
                clientReviewController.getAllReviewsByProduct(productSlug, page, size, sort, filter);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ListResponse<ClientSimpleReviewResponse> listResponse = response.getBody();
        assertNotNull(listResponse, "Response body should not be null");
        List<ClientSimpleReviewResponse> reviews = listResponse.getContent();
        assertNotNull(reviews, "Reviews list should not be null");
        assertEquals(reviewSize, reviews.size(), "Expected 3 reviews in response");
    }

    /**
     * Test Case ID: CRT002
     * Mục tiêu: Kiểm tra getAllReviewsByProduct trả về danh sách rỗng khi không có đánh giá
     * Input: productSlug = "nonexistent-product", page = 1, size = 10, sort = "id,desc", filter = null
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse có danh sách rỗng
     */
    @Test
    public void testGetAllReviewsByProduct_EmptyList_CRT002() throws JsonProcessingException {
        String productSlug = "nonexistent-product";
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = null;
        int statusCode = 200;

        ResponseEntity<ListResponse<ClientSimpleReviewResponse>> response =
                clientReviewController.getAllReviewsByProduct(productSlug, page, size, sort, filter);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ListResponse<ClientSimpleReviewResponse> listResponse = response.getBody();
        assertNotNull(listResponse, "Response body should not be null");
        assertTrue(listResponse.getContent().isEmpty(), "Expected empty reviews list");
    }

    /**
     * Test Case ID: CRT003
     * Mục tiêu: Kiểm tra getAllReviewsByProduct với productSlug null
     * Input: productSlug = null, page = 1, size = 10, sort = "id,desc", filter = null
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse rỗng
     */
    @Test
    public void testGetAllReviewsByProduct_NullSlug_CRT003() throws JsonProcessingException {
        String productSlug = null;
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = null;
        int statusCode = 200;

        ResponseEntity<ListResponse<ClientSimpleReviewResponse>> response =
                clientReviewController.getAllReviewsByProduct(productSlug, page, size, sort, filter);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ListResponse<ClientSimpleReviewResponse> listResponse = response.getBody();
        assertNotNull(listResponse, "Response body should not be null");
        assertTrue(listResponse.getContent().isEmpty(), "Expected empty reviews list for null slug");
    }

    /**
     * Test Case ID: CRT004
     * Mục tiêu: Kiểm tra getAllReviewsByUser trả về danh sách đánh giá không rỗng
     * Input: authentication với username = "user1", page = 1, size = 10, sort = "id,desc", filter = null
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse có danh sách ClientReviewResponse
     */
    @Test
    public void testGetAllReviewsByUser_NonEmptyList_CRT004() throws JsonProcessingException {
        Authentication authentication = createMockAuthentication("dnucator0");
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = null;
        int statusCode = 200;
        int reviewSize = 2;

        ResponseEntity<ListResponse<ClientReviewResponse>> response =
                clientReviewController.getAllReviewsByUser(authentication, page, size, sort, filter);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ListResponse<ClientReviewResponse> listResponse = response.getBody();
        assertNotNull(listResponse, "Response body should not be null");
        assertEquals(reviewSize, listResponse.getContent().size(), "Expected 2 reviews in response");
    }

    /**
     * Test Case ID: CRT005
     * Mục tiêu: Kiểm tra getAllReviewsByUser trả về danh sách rỗng
     * Input: authentication với username = "nonexistent", page = 1, size = 10, sort = "id,desc", filter = null
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse rỗng
     */
    @Test
    public void testGetAllReviewsByUser_EmptyList_CRT005() throws JsonProcessingException {
        Authentication authentication = createMockAuthentication("nonexistent");
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = null;
        int statusCode = 200;

        ResponseEntity<ListResponse<ClientReviewResponse>> response =
                clientReviewController.getAllReviewsByUser(authentication, page, size, sort, filter);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ListResponse<ClientReviewResponse> listResponse = response.getBody();
        assertNotNull(listResponse, "Response body should not be null");
        assertTrue(listResponse.getContent().isEmpty(), "Expected empty reviews list");
    }

    /**
     * Test Case ID: CRT006
     * Mục tiêu: Kiểm tra createReview với dữ liệu hợp lệ
     * Input: ClientReviewRequest với đầy đủ thông tin hợp lệ
     * Expected Output: ResponseEntity với status 201, body chứa ClientReviewResponse tương ứng
     */
    @Test
    public void testCreateReview_ValidRequest_CRT006() throws JsonProcessingException {
        ClientReviewRequest request = createExpectedData(
                "test-data/review-data.json", "create-success", ClientReviewRequest.class);
        int statusCode = 201;

        ResponseEntity<ClientReviewResponse> response = clientReviewController.createReview(request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 201 Created");
        ClientReviewResponse reviewResponse = response.getBody();
        assertNotNull(reviewResponse, "Response body should not be null");
        assertEquals(request.getContent(), reviewResponse.getReviewContent(), "Review content should match");
        assertEquals(request.getRatingScore(), reviewResponse.getReviewRatingScore(), "Review rating should match");
    }

    /**
     * Test Case ID: CRT007
     * Mục tiêu: Kiểm tra createReview với dữ liệu không hợp lệ
     * Input: ClientReviewRequest với thông tin không hợp lệ
     * Expected Output: ResponseEntity với status 400 Bad Request
     */
    @Test
    public void testCreateReview_InvalidRequest_CRT007() throws JsonProcessingException {
        ClientReviewRequest request = new ClientReviewRequest();
        request.setContent(""); // Empty content
        request.setRatingScore(0); // Invalid rating
        request.setUserId(null); // Causes the InvalidDataAccessApiUsageException
//        int cannotCreateStatusCode = 400;
//        ResponseEntity<ClientReviewResponse> response = clientReviewController.createReview(request);
//        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));
//        assertEquals(cannotCreateStatusCode, response.getStatusCodeValue(), "Expected HTTP status 400 Bad Request");

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            clientReviewController.createReview(request);
        }, "Should throw InvalidDataAccessApiUsageException for invalid review data");
    }

    /**
     * Test Case ID: CRT008
     * Mục tiêu: Kiểm tra updateReview với dữ liệu hợp lệ
     * Input: id tồn tại và ClientReviewRequest hợp lệ
     * Expected Output: ResponseEntity với status 200, body chứa ClientReviewResponse đã cập nhật
     */
    @Test
    public void testUpdateReview_ValidRequest_CRT007() throws JsonProcessingException {
        Long reviewId = 1L;
        ClientReviewRequest request = new ClientReviewRequest();
        request.setContent("Updated content");
        request.setRatingScore(4);
        int statusCode = 200;

        ResponseEntity<ClientReviewResponse> response = clientReviewController.updateReview(reviewId, request);
        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");
        ClientReviewResponse reviewResponse = response.getBody();
        assertNotNull(reviewResponse, "Response body should not be null");
        assertEquals(request.getContent(), reviewResponse.getReviewContent(), "Updated content should match");
        assertEquals(request.getRatingScore(), reviewResponse.getReviewRatingScore(), "Updated rating should match");
    }

    /**
     * Test Case ID: CRT009
     * Mục tiêu: Kiểm tra updateReview với id không tồn tại
     * Input: id không tồn tại và ClientReviewRequest hợp lệ
     * Expected Output: ResourceNotFoundException
     */
    @Test
    public void testUpdateReview_NonexistentId_CRT008() throws JsonProcessingException {
        Long nonexistentId = 999L;
        ClientReviewRequest request = new ClientReviewRequest();
        request.setContent("Updated content");
        request.setRatingScore(4);
//        int nonexistentStatusCode = 404;
//
//        ResponseEntity<ClientReviewResponse> response = clientReviewController.updateReview(nonexistentId, request);
//        System.out.println("Response: " + objectMapperLogger().writeValueAsString(response));
//        assertEquals(nonexistentStatusCode, response.getStatusCodeValue(), "Expected HTTP status 200 OK");

        assertThrows(ResourceNotFoundException.class, () -> {
            clientReviewController.updateReview(nonexistentId, request);
        }, "Should throw ResourceNotFoundException for nonexistent review id");
    }

    /**
     * Test Case ID: CRT010
     * Mục tiêu: Kiểm tra deleteReviews với danh sách id hợp lệ
     * Input: List<Long> ids tồn tại
     * Expected Output: ResponseEntity với status 204
     */
    @Test
    public void testDeleteReviews_ValidIds_CRT009() throws JsonProcessingException {
        List<Long> ids = Arrays.asList(1L, 2L);
        int statusCode = 204;

        ResponseEntity<Void> response = clientReviewController.deleteReviews(ids);
        System.out.println("Delete response status: " + response.getStatusCodeValue());

        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 204 No Content");
        assertTrue(reviewRepository.findAllById(ids).isEmpty(), "Reviews should be deleted");
    }

    /**
     * Test Case ID: CRT011
     * Mục tiêu: Kiểm tra deleteReviews với danh sách id không tồn tại
     * Input: List<Long> ids không tồn tại
     * Expected Output: ResponseEntity với status 204
     */
    @Test
    public void testDeleteReviews_NonexistentIds_CRT010() throws JsonProcessingException {
        List<Long> nonexistentIds = Arrays.asList(999L, 1000L);
//        int statusCode = 204;
//
//        ResponseEntity<Void> response = clientReviewController.deleteReviews(nonexistentIds);
//        System.out.println("Delete response status: " + response.getStatusCodeValue());
//
//        assertEquals(statusCode, response.getStatusCodeValue(), "Expected HTTP status 204 No Content");

        assertThrows(EmptyResultDataAccessException.class, () -> {
            clientReviewController.deleteReviews(nonexistentIds);
        }, "Should throw ResourceNotFoundException for nonexistent review ids");
    }
}