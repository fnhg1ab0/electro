// Package khai báo
package com.electro.Minh;

// Import các dependency cần thiết cho việc viết Unit Test controller
import com.electro.controller.inventory.DocketVariantController;
import com.electro.dto.inventory.DocketVariantKeyRequest;
import com.electro.entity.inventory.DocketVariantKey;
import com.electro.service.inventory.DocketVariantService;
import com.electro.config.security.UserDetailsServiceImpl;
import com.electro.config.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sử dụng WebMvcTest chỉ load DocketVariantController để kiểm thử nhanh
@WebMvcTest(DocketVariantController.class)
class DocketVariantControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc để giả lập request HTTP

    @MockBean
    private DocketVariantService docketVariantService; // Mock Service để không phụ thuộc service thực tế

    @MockBean
    private UserDetailsServiceImpl userDetailsService; // Mock UserDetailsServiceImpl để bỏ qua security authentication

    @MockBean
    private JwtUtils jwtUtils; // ✅ Mock JwtUtils để xử lý xác thực token

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper dùng để serialize/deserialize JSON

    /**
     * Test Case: TC_DeleteDocketVariant_Success_001
     * Mục tiêu: Xóa thành công một DocketVariant theo ID
     */
    @Test
    void deleteDocketVariant_shouldReturnNoContent() throws Exception {
        // Khởi tạo dữ liệu test
        Long docketId = 1L;
        Long variantId = 2L;

        // Mock behavior cho service: không làm gì khi gọi delete
        Mockito.doNothing().when(docketVariantService).delete(any(DocketVariantKey.class));

        // Thực hiện HTTP DELETE request và kiểm tra trả về status 204
        mockMvc.perform(delete("/api/docket-variants/{docketId}/{variantId}", docketId, variantId))
                .andExpect(status().isNoContent());

        // Xác nhận rằng phương thức delete đã được gọi đúng
        Mockito.verify(docketVariantService).delete(any(DocketVariantKey.class));
    }

    /**
     * Test Case: TC_DeleteDocketVariants_Success_002
     * Mục tiêu: Xóa thành công nhiều DocketVariant theo danh sách ID
     */
    @Test
    void deleteDocketVariants_shouldReturnNoContent() throws Exception {
        // Tạo danh sách các request cần xóa
        DocketVariantKeyRequest request1 = new DocketVariantKeyRequest();
        request1.setDocketId(1L);
        request1.setVariantId(2L);

        DocketVariantKeyRequest request2 = new DocketVariantKeyRequest();
        request2.setDocketId(3L);
        request2.setVariantId(4L);

        List<DocketVariantKeyRequest> idRequests = Arrays.asList(request1, request2);

        // Mock behavior cho service: không làm gì khi gọi delete danh sách
        Mockito.doNothing().when(docketVariantService).delete(any(List.class));

        // Thực hiện HTTP DELETE với body là danh sách cần xóa và kiểm tra trả về 204
        mockMvc.perform(delete("/api/docket-variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idRequests)))
                .andExpect(status().isNoContent());

        // Xác nhận rằng phương thức delete đã được gọi với tham số bất kỳ
        Mockito.verify(docketVariantService).delete(any(List.class));
    }
}
