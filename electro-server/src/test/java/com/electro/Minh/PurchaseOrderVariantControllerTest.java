// Khai báo package
package com.electro.Minh;

// Import các dependency cần thiết để viết Unit Test cho Controller
import com.electro.controller.inventory.PurchaseOrderVariantController;
import com.electro.dto.inventory.PurchaseOrderVariantKeyRequest;
import com.electro.entity.inventory.PurchaseOrderVariantKey;
import com.electro.service.inventory.PurchaseOrderVariantService;
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

// WebMvcTest: chỉ load PurchaseOrderVariantController để test mà không cần khởi động toàn bộ application
@WebMvcTest(PurchaseOrderVariantController.class)
class PurchaseOrderVariantControllerTest {

    @Autowired
    private MockMvc mockMvc; // Công cụ để thực thi các HTTP Request trong test

    @MockBean
    private PurchaseOrderVariantService purchaseOrderVariantService; // Mock Service để không phụ thuộc service thực tế

    @MockBean
    private com.electro.config.security.UserDetailsServiceImpl userDetailsServiceImpl; // ✅ Mock Security component

    @MockBean
    private com.electro.config.security.JwtUtils jwtUtils; // ✅ Mock Security component

    @Autowired
    private ObjectMapper objectMapper; // Dùng để serialize/deserialize JSON

    /**
     * Test Case: TC_DeletePurchaseOrderVariant_Success_001
     * Mục tiêu: Xóa thành công một PurchaseOrderVariantKey dựa trên purchaseOrderId và variantId
     */
    @Test
    void deletePurchaseOrderVariant_shouldReturnNoContent() throws Exception {
        // Khởi tạo dữ liệu test
        Long purchaseOrderId = 1L;
        Long variantId = 2L;

        // Mock service: không làm gì khi gọi delete với một key
        Mockito.doNothing().when(purchaseOrderVariantService)
                .delete(new PurchaseOrderVariantKey(purchaseOrderId, variantId));

        // Gửi HTTP DELETE request và kiểm tra trả về status 204 No Content
        mockMvc.perform(delete("/api/purchase-order-variants/{purchaseOrderId}/{variantId}", purchaseOrderId, variantId))
                .andExpect(status().isNoContent());

        // Xác thực rằng phương thức delete đã được gọi đúng
        Mockito.verify(purchaseOrderVariantService)
                .delete(new PurchaseOrderVariantKey(purchaseOrderId, variantId));
    }

    /**
     * Test Case: TC_DeletePurchaseOrderVariants_Success_002
     * Mục tiêu: Xóa thành công nhiều PurchaseOrderVariantKeys bằng request body JSON
     */
    @Test
    void deletePurchaseOrderVariants_shouldReturnNoContent() throws Exception {
        // Chuẩn bị danh sách request
        PurchaseOrderVariantKeyRequest request1 = new PurchaseOrderVariantKeyRequest();
        request1.setPurchaseOrderId(1L);
        request1.setVariantId(2L);

        PurchaseOrderVariantKeyRequest request2 = new PurchaseOrderVariantKeyRequest();
        request2.setPurchaseOrderId(3L);
        request2.setVariantId(4L);

        List<PurchaseOrderVariantKeyRequest> idRequests = Arrays.asList(request1, request2);

        // Mapping request thành entity keys
        List<PurchaseOrderVariantKey> ids = Arrays.asList(
                new PurchaseOrderVariantKey(1L, 2L),
                new PurchaseOrderVariantKey(3L, 4L)
        );

        // Mock service: không làm gì khi gọi delete với danh sách keys
        Mockito.doNothing().when(purchaseOrderVariantService).delete(ids);

        // Thực hiện HTTP DELETE với body JSON và kiểm tra trả về 204 No Content
        mockMvc.perform(delete("/api/purchase-order-variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idRequests)))
                .andExpect(status().isNoContent());

        // Xác thực rằng phương thức delete đã được gọi đúng
        Mockito.verify(purchaseOrderVariantService).delete(ids);
    }
}
