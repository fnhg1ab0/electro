package com.electro.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CountVariantKeyRequest {
    private Long countId;
    private Long variantId;
}
