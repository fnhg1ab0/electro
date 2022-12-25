package com.electro.controller.inventory;

import com.electro.constant.AppConstants;
import com.electro.constant.FieldName;
import com.electro.constant.ResourceName;
import com.electro.dto.ListResponse;
import com.electro.dto.inventory.ProductInventoryResponse;
import com.electro.dto.inventory.VariantInventoryResponse;
import com.electro.entity.inventory.DocketVariant;
import com.electro.entity.inventory.ProductInventory;
import com.electro.entity.inventory.VariantInventory;
import com.electro.entity.product.Product;
import com.electro.entity.product.Variant;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.product.ProductInventoryMapper;
import com.electro.mapper.product.VariantInventoryMapper;
import com.electro.repository.inventory.DocketVariantRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.VariantRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@CrossOrigin("http://localhost:3000/")
public class InventoryController {

    private ProductRepository productRepository;
    private DocketVariantRepository docketVariantRepository;
    private ProductInventoryMapper productInventoryMapper;
    private VariantRepository variantRepository;
    private VariantInventoryMapper variantInventoryMapper;

    @GetMapping("/product-inventories")
    public ResponseEntity<ListResponse<ProductInventoryResponse>> getProductInventories(
            @RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        // Lấy danh sách sản phẩm từng được nhập xuất
        Page<Product> products = productRepository.findDocketedProducts(PageRequest.of(page - 1, size));

        List<ProductInventory> productInventories = new ArrayList<>();

        for (Product product : products) {
            ProductInventory productInventory = new ProductInventory();

            productInventory.setProduct(product);

            List<DocketVariant> transactions = docketVariantRepository.findByProductId(product.getId());
            productInventory.setTransactions(transactions);

            Map<String, Integer> inventoryIndices = calculateInventoryIndices(transactions);

            productInventory.setInventory(inventoryIndices.get("inventory"));
            productInventory.setWaitingForDelivery(inventoryIndices.get("waitingForDelivery"));
            productInventory.setCanBeSold(inventoryIndices.get("canBeSold"));
            productInventory.setAreComing(inventoryIndices.get("areComing"));

            productInventories.add(productInventory);
        }

        List<ProductInventoryResponse> productInventoryResponses = productInventoryMapper.toResponse(productInventories);

        return ResponseEntity.status(HttpStatus.OK).body(new ListResponse<>(productInventoryResponses, products));
    }

    @GetMapping("/variant-inventories")
    public ResponseEntity<ListResponse<VariantInventoryResponse>> getVariantInventories(
            @RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        // Lấy danh sách phiên bản sản phẩm từng được nhập xuất
        Page<Variant> variants = variantRepository.findDocketedVariants(PageRequest.of(page - 1, size));

        List<VariantInventory> variantInventories = new ArrayList<>();

        for (Variant variant : variants) {
            VariantInventory variantInventory = new VariantInventory();

            variantInventory.setVariant(variant);

            List<DocketVariant> transactions = docketVariantRepository.findByVariantId(variant.getId());
            variantInventory.setTransactions(transactions);

            Map<String, Integer> inventoryIndices = calculateInventoryIndices(transactions);

            variantInventory.setInventory(inventoryIndices.get("inventory"));
            variantInventory.setWaitingForDelivery(inventoryIndices.get("waitingForDelivery"));
            variantInventory.setCanBeSold(inventoryIndices.get("canBeSold"));
            variantInventory.setAreComing(inventoryIndices.get("areComing"));

            variantInventories.add(variantInventory);
        }

        List<VariantInventoryResponse> variantInventoryResponses = variantInventoryMapper.toResponse(variantInventories);

        return ResponseEntity.status(HttpStatus.OK).body(new ListResponse<>(variantInventoryResponses, variants));
    }

    @GetMapping("/variant-inventories/{variantId}")
    public ResponseEntity<VariantInventoryResponse> getVariantInventory(@PathVariable("variantId") Long variantId) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceName.VARIANT, FieldName.ID, variantId));

        VariantInventory variantInventory = new VariantInventory();

        variantInventory.setVariant(variant);

        List<DocketVariant> transactions = docketVariantRepository.findByVariantId(variant.getId());
        variantInventory.setTransactions(transactions);

        Map<String, Integer> inventoryIndices = calculateInventoryIndices(transactions);

        variantInventory.setInventory(inventoryIndices.get("inventory"));
        variantInventory.setWaitingForDelivery(inventoryIndices.get("waitingForDelivery"));
        variantInventory.setCanBeSold(inventoryIndices.get("canBeSold"));
        variantInventory.setAreComing(inventoryIndices.get("areComing"));

        VariantInventoryResponse variantInventoryResponse = variantInventoryMapper.toResponse(variantInventory);

        return ResponseEntity.status(HttpStatus.OK).body(variantInventoryResponse);
    }

    private Map<String, Integer> calculateInventoryIndices(List<DocketVariant> transactions) {
        int inventory = 0;
        int waitingForDelivery = 0;
        int canBeSold;
        int areComing = 0;

        for (DocketVariant transaction : transactions) {
            // Phiếu Nhập và trạng thái phiếu là Hoàn thành (3)
            if (transaction.getDocket().getType().equals(1) && transaction.getDocket().getStatus().equals(3)) {
                inventory += transaction.getQuantity();
            }

            // Phiếu Xuất và trạng thái phiếu là Hoàn thành (3)
            if (transaction.getDocket().getType().equals(2) && transaction.getDocket().getStatus().equals(3)) {
                inventory -= transaction.getQuantity();
            }

            // Phiếu Xuất và trạng thái phiếu là Mới (1) hoặc Đang xử lý (2)
            if (transaction.getDocket().getType().equals(2) && List.of(1, 2).contains(transaction.getDocket().getStatus())) {
                waitingForDelivery += transaction.getQuantity();
            }

            // Phiếu Nhập và trạng thái phiếu là Mới (1) hoặc Đang xử lý (2)
            if (transaction.getDocket().getType().equals(1) && List.of(1, 2).contains(transaction.getDocket().getStatus())) {
                areComing += transaction.getQuantity();
            }
        }

        canBeSold = inventory - waitingForDelivery;

        Map<String, Integer> indices = new HashMap<>();

        indices.put("inventory", inventory);
        indices.put("waitingForDelivery", waitingForDelivery);
        indices.put("canBeSold", canBeSold);
        indices.put("areComing", areComing);

        return indices;
    }

}
