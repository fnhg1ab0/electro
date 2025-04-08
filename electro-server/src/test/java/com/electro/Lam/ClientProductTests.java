package com.electro.Lam;

import com.electro.controller.client.ClientProductController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientListedProductResponse;
import com.electro.dto.client.ClientProductResponse;
import com.electro.entity.product.Product;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.client.ClientProductMapper;
import com.electro.projection.inventory.SimpleProductInventory;
import com.electro.repository.ProjectionRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.review.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientProductTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProjectionRepository projectionRepository;

    @Mock
    private ClientProductMapper clientProductMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ClientProductController clientProductController;


    // Mã kiểm thử: CPT_001
    // Mục tiêu kiểm thử: Xác minh rằng getAllProducts trả về danh sách sản phẩm với trạng thái OK.
    // Đầu vào: page = 1, size = 10, sortBy = null, sortDirection = null, categorySlug = null, saleable = false, active = false
    // Đầu ra mong đợi: ResponseEntity với HttpStatus.OK và một ListResponse chứa một ClientListedProductResponse.
    // Ghi chú: Mock repository sản phẩm để trả về một sản phẩm duy nhất và đảm bảo mapper và projection repository hoạt động như mong đợi.
    @Test
    public void testGetAllProducts() {
        Product product = new Product();  // Create a mock product
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));  // Create a mock paginated product list
        SimpleProductInventory inventory = new SimpleProductInventory(1L, 100L, 10L, 80L, 5L);  // Mock product inventory
        ClientListedProductResponse response = new ClientListedProductResponse();  // Create a mock response DTO

        // Mock the behavior of the repository methods
        when(productRepository.findByParams(any(), any(), any(), anyBoolean(), anyBoolean(), any(PageRequest.class)))
                .thenReturn(productPage);
        when(projectionRepository.findSimpleProductInventories(anyList())).thenReturn(Collections.singletonList(inventory));
        when(clientProductMapper.entityToListedResponse(any(Product.class), anyList())).thenReturn(response);

        // Call the controller method and capture the result
        ResponseEntity<ListResponse<ClientListedProductResponse>> result = clientProductController.getAllProducts(1, 10, null, null, null, false, false);

        // Print test data and result for debugging purposes
        System.out.println("Test Data: product = " + product);
        System.out.println("Test Result: Content Size = " + result.getBody().getContent().size());

        // Assert that the response status is OK and the content size is 1
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getContent().size());
    }

    // Mã kiểm thử: CPT_002
    // Mục tiêu kiểm thử: Xác minh rằng getProduct trả về thông tin chi tiết sản phẩm chính xác với trạng thái OK cho một slug nhất định.
    // Đầu vào: slug = "test-slug"
    // Đầu ra mong đợi: ResponseEntity với HttpStatus.OK và một ClientProductResponse chứa thông tin chi tiết của sản phẩm với slug đã cho.
    // Ghi chú: Mock repository sản phẩm để trả về một sản phẩm với slug được chỉ định và xác minh rằng mapper, projection và review repository được gọi đúng cách.
    @Test
    public void testGetProduct() {
        String slug = "test-slug";  // Define a test product slug
        Product product = new Product();
        product.setId(1L);  // Set the product ID
        product.setName("Test Product");  // Set the product name
        product.setSlug(slug);  // Set the product slug
        SimpleProductInventory inventory = new SimpleProductInventory(1L, 100L, 10L, 80L, 5L);  // Mock product inventory

        // Create a mock response DTO for the product
        ClientProductResponse response = new ClientProductResponse();
        response.setProductId(1L);
        response.setProductName("Test Product");
        response.setProductSlug(slug);
        response.setProductShortDescription("Short description");
        response.setProductDescription("Detailed description");
        response.setProductSaleable(true);
        response.setProductAverageRatingScore(5);
        response.setProductCountReviews(10);

        // Create a mock paginated list of related products
        Page<Product> relatedProducts = new PageImpl<>(Collections.singletonList(product));
        ClientListedProductResponse relatedResponse = new ClientListedProductResponse();

        // Mock repository methods for the product and related products
        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));
        when(projectionRepository.findSimpleProductInventories(anyList())).thenReturn(Collections.singletonList(inventory));
        when(reviewRepository.findAverageRatingScoreByProductId(anyLong())).thenReturn(5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10);
        when(productRepository.findByParams(any(), any(), any(), anyBoolean(), anyBoolean(), any(PageRequest.class)))
                .thenReturn(relatedProducts);
        when(clientProductMapper.entityToListedResponse(any(Product.class), anyList())).thenReturn(relatedResponse);
        when(clientProductMapper.entityToResponse(any(Product.class), anyList(), anyInt(), anyInt(), anyList()))
                .thenReturn(response);

        // Call the controller method and capture the result
        ResponseEntity<ClientProductResponse> result = clientProductController.getProduct(slug);

        // Print test data and result for debugging purposes
        System.out.println("Test Data: product = " + product);
        System.out.println("Test Result: Response Body = " + result.getBody());

        // Assert that the response status is OK and the response body matches the expected result
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    // Test case to handle the scenario when the requested product is not found

    // Mã kiểm thử: CPT_003
    // Mục tiêu kiểm thử: Xác minh rằng getProduct ném ra ResourceNotFoundException khi sản phẩm với slug đã cho không tồn tại.
    // Đầu vào: slug = "non-existent-slug"
    // Đầu ra mong đợi: ResourceNotFoundException được ném ra.
    // Ghi chú: Mock repository sản phẩm để trả về một Optional rỗng cho slug đã cho.

    @Test
    public void testGetProduct_NotFound() {
        String slug = "non-existent-slug";  // Define a non-existent product slug

        // Mock the repository to return an empty result (product not found)
        when(productRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // Print test data for debugging
        System.out.println("Test Data: slug = " + slug);

        // Assert that the ResourceNotFoundException is thrown when the product is not found
        assertThrows(ResourceNotFoundException.class, () -> {
            clientProductController.getProduct(slug);
        });
    }
}
