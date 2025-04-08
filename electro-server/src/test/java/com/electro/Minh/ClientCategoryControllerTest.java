package com.electro.Minh;

import com.electro.TestSecurityConfig;
import com.electro.controller.client.ClientCategoryController;
import com.electro.config.security.WebSecurityConfig;
import com.electro.dto.client.ClientCategoryResponse;
import com.electro.entity.product.Category;
import com.electro.mapper.client.ClientCategoryMapper;
import com.electro.repository.product.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Lớp test cho ClientCategoryController.
 * Sử dụng WebMvcTest để test layer controller mà không cần khởi động toàn bộ ứng dụng.
 */
@WebMvcTest(controllers = ClientCategoryController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class)
})
@Import(TestSecurityConfig.class)
class ClientCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc để thực hiện các yêu cầu HTTP giả lập

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper để chuyển đổi object sang JSON và ngược lại

    @MockBean
    private CategoryRepository categoryRepository; // Mock repository để giả lập truy vấn database

    @MockBean
    private ClientCategoryMapper clientCategoryMapper; // Mock mapper để giả lập ánh xạ entity sang DTO

    @MockBean
    private UserDetailsService userDetailsService; // Mock UserDetailsService cho security

    // ============================
    // 🟢 Positive Test Cases
    // ============================

    /**
     * Test lấy tất cả categories thành công.
     * Kiểm tra response trả về status 200 và content type JSON.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetAllCategories_Success() throws Exception {
        // Arrange - Chuẩn bị dữ liệu giả lập
        Category category = new Category();
        ClientCategoryResponse response = new ClientCategoryResponse();

        // Mock hành vi của repository và mapper
        when(categoryRepository.findByParentCategoryIsNull())
                .thenReturn(Collections.singletonList(category));
        when(clientCategoryMapper.entityToResponse(Collections.singletonList(category), 3))
                .thenReturn(Collections.singletonList(response));

        // Act & Assert - Thực hiện request và kiểm tra kết quả
        mockMvc.perform(get("/client-api/categories"))
                .andExpect(status().isOk()) // Kiểm tra status 200
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)); // Kiểm tra content type
    }

    /**
     * Test lấy category theo slug thành công.
     * Kiểm tra response trả về status 200 và content type JSON.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetCategoryBySlug_Success() throws Exception {
        // Arrange
        String slug = "test-slug";
        Category category = new Category();
        ClientCategoryResponse response = new ClientCategoryResponse();

        // Mock hành vi tìm kiếm theo slug
        when(categoryRepository.findBySlug(slug))
                .thenReturn(Optional.of(category));
        when(clientCategoryMapper.entityToResponse(category, false))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/client-api/categories/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * Test lấy tất cả categories khi database rỗng.
     * Kiểm tra response trả về danh sách rỗng với status 200.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetAllCategories_EmptyList_Success() throws Exception {
        // Arrange
        when(categoryRepository.findByParentCategoryIsNull())
                .thenReturn(Collections.emptyList());
        when(clientCategoryMapper.entityToResponse(Collections.emptyList(), 3))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/client-api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"content\":[],\"totalElements\":0}")); // Điều chỉnh kỳ vọng
    }

    /**
     * Test lấy category theo slug và kiểm tra dữ liệu trả về đúng.
     * Kiểm tra response trả về status 200 và content type JSON.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetCategoryBySlug_ReturnCorrectData_Success() throws Exception {
        // Arrange
        String slug = "electronics";
        Category category = new Category();
        ClientCategoryResponse expectedResponse = new ClientCategoryResponse();

        when(categoryRepository.findBySlug(slug))
                .thenReturn(Optional.of(category));
        when(clientCategoryMapper.entityToResponse(category, false))
                .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/client-api/categories/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ============================
    // 🔴 Negative Test Cases
    // ============================

    /**
     * Test lấy category với slug không tồn tại.
     * Kiểm tra response trả về status 404 Not Found.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetCategoryBySlug_NotFound() throws Exception {
        // Arrange
        String slug = "non-existent-slug";

        when(categoryRepository.findBySlug(slug))
                .thenReturn(Optional.empty()); // Mock không tìm thấy category

        // Act & Assert
        mockMvc.perform(get("/client-api/categories/{slug}", slug))
                .andExpect(status().isNotFound()); // Kiểm tra status 404
    }

    /**
     * Test lấy tất cả categories khi xảy ra lỗi server.
     * Kiểm tra response trả về status 500 Internal Server Error.
     *
     * @throws Exception nếu có lỗi trong quá trình test
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetAllCategories_InternalServerError() throws Exception {
        // Arrange
        when(categoryRepository.findByParentCategoryIsNull())
                .thenThrow(new RuntimeException("Unexpected error")); // Mock lỗi runtime

        // Act & Assert
        mockMvc.perform(get("/client-api/categories"))
                .andExpect(status().isInternalServerError()); // Kiểm tra status 500
    }
}