package com.electro.Minh;

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

@WebMvcTest(PurchaseOrderVariantController.class)
class PurchaseOrderVariantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseOrderVariantService purchaseOrderVariantService;

    @MockBean
    private com.electro.config.security.UserDetailsServiceImpl userDetailsServiceImpl; // ✅ Mock Security bean

    @MockBean
    private com.electro.config.security.JwtUtils jwtUtils; // ✅ Mock Security bean

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deletePurchaseOrderVariant_shouldReturnNoContent() throws Exception {
        Long purchaseOrderId = 1L;
        Long variantId = 2L;

        Mockito.doNothing().when(purchaseOrderVariantService)
                .delete(new PurchaseOrderVariantKey(purchaseOrderId, variantId));

        mockMvc.perform(delete("/api/purchase-order-variants/{purchaseOrderId}/{variantId}", purchaseOrderId, variantId))
                .andExpect(status().isNoContent());

        Mockito.verify(purchaseOrderVariantService)
                .delete(new PurchaseOrderVariantKey(purchaseOrderId, variantId));
    }

    @Test
    void deletePurchaseOrderVariants_shouldReturnNoContent() throws Exception {
        PurchaseOrderVariantKeyRequest request1 = new PurchaseOrderVariantKeyRequest();
        request1.setPurchaseOrderId(1L);
        request1.setVariantId(2L);

        PurchaseOrderVariantKeyRequest request2 = new PurchaseOrderVariantKeyRequest();
        request2.setPurchaseOrderId(3L);
        request2.setVariantId(4L);

        List<PurchaseOrderVariantKeyRequest> idRequests = Arrays.asList(request1, request2);

        List<PurchaseOrderVariantKey> ids = Arrays.asList(
                new PurchaseOrderVariantKey(1L, 2L),
                new PurchaseOrderVariantKey(3L, 4L)
        );

        Mockito.doNothing().when(purchaseOrderVariantService).delete(ids);

        mockMvc.perform(delete("/api/purchase-order-variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(idRequests)))
                .andExpect(status().isNoContent());

        Mockito.verify(purchaseOrderVariantService).delete(ids);
    }
}
