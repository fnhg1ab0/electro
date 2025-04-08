package com.electro;

import com.electro.controller.GenericController;
import com.electro.dto.inventory.WarehouseRequest;
import com.electro.dto.inventory.WarehouseResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseControllerConfig {

    @Bean
    public GenericController<WarehouseRequest, WarehouseResponse> warehouseController() {
        return new GenericController<>();
    }
}
