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
}
