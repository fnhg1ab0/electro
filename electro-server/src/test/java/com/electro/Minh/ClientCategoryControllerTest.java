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

@WebMvcTest(controllers = ClientCategoryController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class)
})
@Import(TestSecurityConfig.class)
class ClientCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private ClientCategoryMapper clientCategoryMapper;

    @MockBean
    private UserDetailsService userDetailsService; // ⭐ Mock security

    // ============================
    // 🟢 Positive Test Cases
    // ============================

    /**
     * TC_ClientCategory_GetAllCategories_Success
     * Mục tiêu: Lấy tất cả category thành công.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetAllCategories_Success() throws Exception {
        // Arrange
        Category category = new Category();
        ClientCategoryResponse response = new ClientCategoryResponse();

        when(categoryRepository.findByParentCategoryIsNull())
                .thenReturn(Collections.singletonList(category));
        when(clientCategoryMapper.entityToResponse(Collections.singletonList(category), 3))
                .thenReturn(Collections.singletonList(response));

        // Act & Assert
        mockMvc.perform(get("/client-api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * TC_ClientCategory_GetCategoryBySlug_Success
     * Mục tiêu: Lấy chi tiết category theo slug thành công.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetCategoryBySlug_Success() throws Exception {
        // Arrange
        String slug = "test-slug";
        Category category = new Category();
        ClientCategoryResponse response = new ClientCategoryResponse();

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
     * TC_ClientCategory_GetAllCategories_EmptyList_Positive
     * Mục tiêu: Lấy tất cả category khi database rỗng.
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
                .andExpect(content().json("[]"));
    }

    /**
     * TC_ClientCategory_GetCategoryBySlug_ReturnCorrectData_Positive
     * Mục tiêu: Lấy chi tiết category theo slug và so sánh nội dung trả về.
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
     * TC_ClientCategory_GetCategoryBySlug_NotFound_Negative
     * Mục tiêu: Lấy category bằng slug không tồn tại -> trả 404.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetCategoryBySlug_NotFound() throws Exception {
        // Arrange
        String slug = "non-existent-slug";

        when(categoryRepository.findBySlug(slug))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/client-api/categories/{slug}", slug))
                .andExpect(status().isNotFound());
    }

    /**
     * TC_ClientCategory_GetAllCategories_InternalServerError_Negative
     * Mục tiêu: Simulate lỗi server khi lấy danh sách category -> trả 500.
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"CUSTOMER"})
    void testGetAllCategories_InternalServerError() throws Exception {
        // Arrange
        when(categoryRepository.findByParentCategoryIsNull())
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/client-api/categories"))
                .andExpect(status().isInternalServerError());
    }
}
