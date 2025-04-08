// Package khai báo
package com.electro.Minh;

// Import các dependency cần thiết cho việc viết Unit Test Controller
import com.electro.controller.inventory.InventoryController;
import com.electro.dto.inventory.ProductInventoryResponse;
import com.electro.dto.inventory.VariantInventoryResponse;
import com.electro.entity.inventory.DocketVariant;
import com.electro.entity.product.Product;
import com.electro.entity.product.Variant;
import com.electro.mapper.product.ProductInventoryMapper;
import com.electro.mapper.product.VariantInventoryMapper;
import com.electro.projection.inventory.VariantInventory;
import com.electro.repository.inventory.DocketVariantRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.VariantRepository;
import com.electro.config.security.UserDetailsServiceImpl;
import com.electro.config.security.JwtUtils; // ✅ Mock thêm JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// WebMvcTest: chỉ load InventoryController để tối ưu tốc độ kiểm thử
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc để thực hiện HTTP Request trong test

    @MockBean
    private ProductRepository productRepository; // Mock ProductRepository để không truy cập database thực tế

    @MockBean
    private VariantRepository variantRepository; // Mock VariantRepository

    @MockBean
    private DocketVariantRepository docketVariantRepository; // Mock DocketVariantRepository

    @MockBean
    private ProductInventoryMapper productInventoryMapper; // Mock mapper để không thực hiện mapping thực

    @MockBean
    private VariantInventoryMapper variantInventoryMapper; // Mock mapper

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl; // ✅ Mock security component

    @MockBean
    private JwtUtils jwtUtils; // ✅ Mock security component

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper dùng cho JSON (ở đây chưa cần dùng trực tiếp)

    /**
     * Test Case: TC_GetProductInventories_Success_001
     * Mục tiêu: Kiểm tra lấy danh sách tồn kho sản phẩm thành công với phân trang
     */
    @Test
    void getProductInventories_shouldReturnOk() throws Exception {
        // Mock dữ liệu product trả về
        Product product = new Product();
        product.setId(1L);

        Page<Product> page = new PageImpl<>(Collections.singletonList(product));
        Mockito.when(productRepository.findDocketedProducts(any(PageRequest.class))).thenReturn(page);
        Mockito.when(docketVariantRepository.findByProductId(1L)).thenReturn(Collections.emptyList());
        Mockito.<List<ProductInventoryResponse>>when(productInventoryMapper.toResponse(anyList()))
                .thenReturn(Collections.emptyList());

        // Gửi HTTP GET request và kỳ vọng HTTP status 200 OK
        mockMvc.perform(get("/api/product-inventories")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test Case: TC_GetVariantInventories_Success_002
     * Mục tiêu: Kiểm tra lấy danh sách tồn kho biến thể thành công với phân trang
     */
    @Test
    void getVariantInventories_shouldReturnOk() throws Exception {
        // Mock dữ liệu variant trả về
        Variant variant = new Variant();
        variant.setId(1L);

        Page<Variant> page = new PageImpl<>(Collections.singletonList(variant));
        Mockito.when(variantRepository.findDocketedVariants(any(PageRequest.class))).thenReturn(page);
        Mockito.when(docketVariantRepository.findByVariantId(1L)).thenReturn(Collections.emptyList());
        Mockito.<List<VariantInventoryResponse>>when(variantInventoryMapper.toResponse(anyList()))
                .thenReturn(Collections.emptyList());

        // Gửi HTTP GET request và kỳ vọng HTTP status 200 OK
        mockMvc.perform(get("/api/variant-inventories")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test Case: TC_GetVariantInventory_Success_003
     * Mục tiêu: Kiểm tra lấy chi tiết tồn kho của 1 biến thể thành công
     */
    @Test
    void getVariantInventory_shouldReturnOk() throws Exception {
        // Mock dữ liệu cho variant
        Variant variant = new Variant();
        variant.setId(1L);

        Mockito.when(variantRepository.findById(1L)).thenReturn(java.util.Optional.of(variant));
        Mockito.when(docketVariantRepository.findByVariantId(1L)).thenReturn(Collections.emptyList());
        Mockito.when(variantInventoryMapper.toResponse(Mockito.any(VariantInventory.class)))
                .thenReturn(new VariantInventoryResponse());

        // Gửi HTTP GET request lấy chi tiết variant và kỳ vọng HTTP status 200 OK
        mockMvc.perform(get("/api/variant-inventories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
