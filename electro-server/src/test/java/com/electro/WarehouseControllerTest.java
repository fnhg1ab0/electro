package com.electro;

import com.electro.controller.GenericController;
import com.electro.dto.ListResponse;
import com.electro.dto.inventory.WarehouseRequest;
import com.electro.dto.inventory.WarehouseResponse;
import com.electro.service.CrudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenericController.class)
@Import(WarehouseControllerConfig.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrudService<Long, WarehouseRequest, WarehouseResponse> warehouseService;

    @Autowired
    private ObjectMapper objectMapper;

    private WarehouseResponse warehouseResponse;

    @BeforeEach
    void setUp() {
        warehouseResponse = new WarehouseResponse();
        warehouseResponse.setId(1L);
        warehouseResponse.setCode("WH-001");
        warehouseResponse.setName("Kho Hà Nội");
        warehouseResponse.setCreatedAt(Instant.now());
        warehouseResponse.setUpdatedAt(Instant.now());
        warehouseResponse.setStatus(1);
    }

    @Test
    void testGetAllWarehouses() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WarehouseResponse> page = new PageImpl<>(Collections.singletonList(warehouseResponse), pageable, 1);

        ListResponse<WarehouseResponse> listResponse = ListResponse.of(Collections.singletonList(warehouseResponse), page);

        Mockito.when(warehouseService.findAll(0, 10, "id,asc", null, null, false))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("WH-001"));
    }

    @Test
    void testGetWarehouseById() throws Exception {
        Mockito.when(warehouseService.findById(1L))
                .thenReturn(warehouseResponse);

        mockMvc.perform(get("/api/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WH-001"));
    }

    @Test
    void testCreateWarehouse() throws Exception {
        WarehouseRequest request = new WarehouseRequest();
        request.setCode("WH-001");
        request.setName("Kho Hà Nội");
        request.setStatus(1);

        // Convert request thành JsonNode
        JsonNode jsonRequest = objectMapper.valueToTree(request);

        Mockito.when(warehouseService.save(any(JsonNode.class), any()))
                .thenReturn(warehouseResponse);

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WH-001"));
    }

    @Test
    void testUpdateWarehouse() throws Exception {
        WarehouseRequest request = new WarehouseRequest();
        request.setCode("WH-001");
        request.setName("Kho Hà Nội Updated");
        request.setStatus(1);

        warehouseResponse.setName("Kho Hà Nội Updated");

        Mockito.when(warehouseService.save(Mockito.eq(1L), any()))
                .thenReturn(warehouseResponse);

        mockMvc.perform(put("/api/warehouses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kho Hà Nội Updated"));
    }

    @Test
    void testDeleteWarehouse() throws Exception {
        mockMvc.perform(delete("/api/warehouses/1"))
                .andExpect(status().isNoContent());
    }
}
