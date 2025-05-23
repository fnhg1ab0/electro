package com.electro.dto.order;

import lombok.Data;

@Data
public class OrderVariantKeyRequest {
    private Long orderId;
    private Long variantId;

    public OrderVariantKeyRequest(long orderId, long variantId) {
        this.orderId = orderId;
        this.variantId = variantId;
    }
}
