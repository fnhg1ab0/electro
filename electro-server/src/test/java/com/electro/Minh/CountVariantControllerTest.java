// Package chứa các class test
package com.electro.Minh;

// Import các thành phần cần thiết để test controller
import com.electro.controller.inventory.CountVariantController;
import com.electro.dto.inventory.CountVariantKeyRequest;
import com.electro.entity.inventory.CountVariantKey;
import com.electro.service.inventory.CountVariantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Cấu hình WebMvcTest chỉ load riêng CountVariantController, loại trừ cấu hình Security
@WebMvcTest(controllers = CountVariantController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.electro.config.security.WebSecurityConfig.class)
})
@AutoConfigureMockMvc(addFilters = false) // ⭐ Tắt security filter chỉ áp dụng trong test
class CountVariantControllerTest {

    @Autowired
    private MockMvc mockMvc; // Dùng để giả lập HTTP request tới controller

    @Autowired
    private ObjectMapper objectMapper; // Dùng để chuyển đổi object -> JSON

    @MockBean
    private CountVariantService countVariantService; // Mock service để kiểm soát hành vi

    @MockBean
    private com.electro.config.security.UserDetailsServiceImpl userDetailsServiceImpl; // Mock userDetails để tránh lỗi SecurityContext

    /**
     * Test Case: TC_DeleteCountVariant_Success_001
     * Mục tiêu: Xóa 1 CountVariantKey thành công
     */
    @Test
    void testDeleteCountVariant_Success() throws Exception {
        // Chuẩn bị dữ liệu: tạo 1 CountVariantKey
        Long countId = 1L;
        Long variantId = 2L;
        CountVariantKey id = new CountVariantKey(countId, variantId);

        // Mock service: giả lập không làm gì khi gọi delete
        doNothing().when(countVariantService).delete(id);

        // Gửi HTTP DELETE request tới API và kiểm tra HTTP status
        mockMvc.perform(delete("/api/count-variants/{countId}/{variantId}", countId, variantId))
                .andExpect(status().isNoContent()); // ✅ Kỳ vọng 204 No Content
    }

    /**
     * Test Case: TC_DeleteCountVariants_Success_002
     * Mục tiêu: Xóa nhiều CountVariantKey cùng lúc thành công
     */
    @Test
    void testDeleteCountVariants_Success() throws Exception {
        // Chuẩn bị dữ liệu: danh sách các CountVariantKeyRequest
        List<CountVariantKeyRequest> requests = Arrays.asList(
                new CountVariantKeyRequest(1L, 2L),
                new CountVariantKeyRequest(3L, 4L)
        );

        // Mock service: giả lập không làm gì khi gọi delete nhiều bản ghi
        doNothing().when(countVariantService).delete(
                Arrays.asList(
                        new CountVariantKey(1L, 2L),
                        new CountVariantKey(3L, 4L)
                )
        );

        // Convert request thành JSON để gửi trong HTTP DELETE
        String jsonRequest = objectMapper.writeValueAsString(requests);

        // Gửi HTTP DELETE request với body là danh sách key cần xóa
        mockMvc.perform(delete("/api/count-variants")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isNoContent()); // ✅ Kỳ vọng 204 No Content
    }
}
