package com.electro.Minh;

import com.electro.controller.inventory.InventoryController;
import com.electro.dto.inventory.ProductInventoryResponse;
import com.electro.dto.inventory.VariantInventoryResponse;
import com.electro.entity.product.Product;
import com.electro.entity.product.Variant;
import com.electro.mapper.product.ProductInventoryMapper;
import com.electro.mapper.product.VariantInventoryMapper;
import com.electro.projection.inventory.VariantInventory;
import com.electro.repository.inventory.DocketVariantRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.VariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InventoryControllerTest {

    @Nested
    @DisplayName("Unit Tests")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {

        @Mock private ProductRepository productRepository;
        @Mock private VariantRepository variantRepository;
        @Mock private DocketVariantRepository docketVariantRepository;
        @Mock private ProductInventoryMapper productInventoryMapper;
        @Mock private VariantInventoryMapper variantInventoryMapper;

        @InjectMocks private InventoryController inventoryController;

        private Product product;
        private Variant variant;

        @BeforeEach
        void setup() {
            product = new Product();
            product.setId(1L);

            variant = new Variant();
            variant.setId(1L);
        }

        @Test
        @DisplayName("IC-UT001 - getProductInventories returns OK")
        void testGetProductInventories() {
            Page<Product> page = new PageImpl<>(Collections.singletonList(product));
            when(productRepository.findDocketedProducts(any(PageRequest.class))).thenReturn(page);
            when(docketVariantRepository.findByProductId(1L)).thenReturn(Collections.emptyList());
            when(productInventoryMapper.toResponse(anyList())).thenReturn(Collections.emptyList());

            ResponseEntity<?> response = inventoryController.getProductInventories(1, 10);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("IC-UT002 - getVariantInventories returns OK")
        void testGetVariantInventories() {
            Page<Variant> page = new PageImpl<>(Collections.singletonList(variant));
            when(variantRepository.findDocketedVariants(any(PageRequest.class))).thenReturn(page);
            when(docketVariantRepository.findByVariantId(1L)).thenReturn(Collections.emptyList());
            when(variantInventoryMapper.toResponse(anyList())).thenReturn(Collections.emptyList());

            ResponseEntity<?> response = inventoryController.getVariantInventories(1, 10);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("IC-UT003 - getVariantInventory returns OK")
        void testGetVariantInventory() {
            when(variantRepository.findById(1L)).thenReturn(java.util.Optional.of(variant));
            when(docketVariantRepository.findByVariantId(1L)).thenReturn(Collections.emptyList());
            when(variantInventoryMapper.toResponse(any(VariantInventory.class)))
                    .thenReturn(new VariantInventoryResponse());

            ResponseEntity<VariantInventoryResponse> response = inventoryController.getVariantInventory(1L);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    @SpringBootTest
    @AutoConfigureMockMvc
    @Transactional
    @ActiveProfiles("test")
    @Sql(scripts = { "classpath:schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class DatabaseTests {

        @Autowired private MockMvc mockMvc;
        @Autowired private EntityManager entityManager;
        @Autowired private VariantRepository variantRepository;
        @Autowired private ProductRepository productRepository;

        @Test
        @DisplayName("IC-IT001 - GET /product-inventories returns OK")
        void testGetProductInventoriesIntegration() throws Exception {
            mockMvc.perform(get("/api/product-inventories?page=1&size=10"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("IC-IT002 - GET /variant-inventories returns OK")
        void testGetVariantInventoriesIntegration() throws Exception {
            mockMvc.perform(get("/api/variant-inventories?page=1&size=10"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("IC-IT003 - GET /variant-inventories/{id} returns OK and prints response")
        void testGetVariantInventoryIntegration() throws Exception {
            // Setup dữ liệu
            Product product = (Product) new Product()
                    .setName("Sample Product")
                    .setCode("P123")
                    .setSlug("sample-product")
                    .setStatus(1)
                    .setCreatedAt(Instant.now())
                    .setUpdatedAt(Instant.now());

            product = productRepository.save(product);

            Variant variant = (Variant) new Variant()
                    .setSku("SKU-123")
                    .setPrice(100000.0)
                    .setCost(80000.0)
                    .setProduct(product)
                    .setStatus(1)
                    .setCreatedAt(Instant.now())
                    .setUpdatedAt(Instant.now());

            variant = variantRepository.save(variant);
            entityManager.flush();

            MvcResult result = mockMvc.perform(get("/api/variant-inventories/" + variant.getId()))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            System.out.println("👉 Response Body:\n" + responseBody);
        }
    }
}
