package com.electro.Lam;

import com.electro.controller.client.ClientWishController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientListedProductResponse;
import com.electro.dto.client.ClientWishRequest;
import com.electro.dto.client.ClientWishResponse;
import com.electro.entity.authentication.User;
import com.electro.entity.client.Wish;
import com.electro.entity.product.Product;
import com.electro.mapper.client.ClientWishMapper;
import com.electro.repository.client.WishRepository;
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
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientWishTests {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ClientWishMapper clientWishMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClientWishController clientWishController;

    /**
     * Test Case ID: CWT001
     * Mục tiêu: Kiểm tra getAllWishes trả về danh sách các wish của người dùng với dữ liệu hợp lệ.
     * Input: username = "testuser" và wishRepository trả về danh sách không rỗng.
     * Expected Output: ResponseEntity với status 200, body chứa ListResponse có danh sách ClientWishResponse đúng.
     */    @Test
    public void testGetAllWishes() {
        String username = "testuser";  // Simulate a test username
        when(authentication.getName()).thenReturn(username);  // Mock the Authentication to return the test username

        // Simulate a page of wish entities
        Wish wish = new Wish();
        Page<Wish> wishes = new PageImpl<>(Collections.singletonList(wish));
        when(wishRepository.findAllByUsername(eq(username), eq("id"), isNull(), any(PageRequest.class)))
                .thenReturn(wishes);  // Mock the wishRepository to return the wish list

        // Map the wish entity to a response DTO
        ClientWishResponse response = new ClientWishResponse();
        when(clientWishMapper.entityToResponse(any(Wish.class))).thenReturn(response);  // Mock the mapping of the wish to a response

        // Call the controller method and capture the response
        ResponseEntity<ListResponse<ClientWishResponse>> result = clientWishController.getAllWishes(authentication, 1, 10, "id", null);

        // Print test data and result for debugging purposes
        System.out.println("Test Data: username = " + username);
        System.out.println("Test Result: Status Code = " + result.getStatusCode());
        System.out.println("Test Result: Content Size = " + result.getBody().getContent().size());

        // Assert the expected result
        assertEquals(HttpStatus.OK, result.getStatusCode());  // Check that the status is OK (200)
        assertEquals(1, result.getBody().getContent().size());  // Check that the response body has one wish
    }

    /**
     * Test Case ID: CWT002
     * Mục tiêu: Kiểm tra createWish tạo mới một wish cho người dùng với dữ liệu hợp lệ.
     * Input: userId = 1, productId = 1 và wishRepository trả về Optional rỗng.
     * Expected Output: ResponseEntity với status 201, body chứa ClientWishResponse đúng.
     */    @Test
    public void testCreateWish() throws Exception {
        ClientWishRequest request = new ClientWishRequest();
        request.setUserId(1L);  // Set user ID in the request
        request.setProductId(1L);  // Set product ID in the request

        // Mock wishRepository to return an empty result when checking if a wish already exists
        when(wishRepository.findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId())).thenReturn(Optional.empty());

        // Create mock user and product entities
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        // Create a wish entity to simulate the creation of a wish
        Wish wish = new Wish();
        wish.setId(1L);
        wish.setUser(user);
        wish.setProduct(product);
        wish.setCreatedAt(Instant.now());

        // Mock the mapping of the request to the entity
        when(clientWishMapper.requestToEntity(request)).thenReturn(wish);
        when(wishRepository.save(wish)).thenReturn(wish);  // Mock the repository to save the wish

        // Create a response DTO for the created wish
        ClientWishResponse response = new ClientWishResponse();
        response.setWishId(1L);
        response.setWishCreatedAt(wish.getCreatedAt());
        response.setWishProduct(new ClientListedProductResponse());

        // Mock the mapping of the entity to the response
        when(clientWishMapper.entityToResponse(wish)).thenReturn(response);

        // Call the controller method and capture the result
        ResponseEntity<ClientWishResponse> result = clientWishController.createWish(request);

        // Print test data and result for debugging purposes
        System.out.println("Test Data: request = " + request);
        System.out.println("Test Result: Status Code = " + result.getStatusCode());
        System.out.println("Test Result: Response Body = " + result.getBody());

        // Assert the expected result
        assertEquals(HttpStatus.CREATED, result.getStatusCode());  // Check that the status is CREATED (201)
        assertEquals(response, result.getBody());  // Check that the response body matches the expected response
    }

    /**
     * Test Case ID: CWT003
     * Mục tiêu: Kiểm tra deleteWishes xóa danh sách các wish với dữ liệu hợp lệ.
     * Input: ids = [1] và wishRepository thực hiện xóa thành công.
     * Expected Output: ResponseEntity với status 204, không có nội dung trả về.
     */    @Test
    public void testDeleteWishes() {
        List<Long> ids = Collections.singletonList(1L);  // Simulate a list of wish IDs to delete

        // Mock the wishRepository to do nothing when deleting wishes by their IDs
        doNothing().when(wishRepository).deleteAllById(ids);

        // Call the controller method and capture the result
        ResponseEntity<Void> result = clientWishController.deleteWishes(ids);

        // Print test data and result for debugging purposes
        System.out.println("Test Data: ids = " + ids);
        System.out.println("Test Result: Status Code = " + result.getStatusCode());

        // Assert the expected result
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());  // Check that the status is NO_CONTENT (204)
        verify(wishRepository, times(1)).deleteAllById(ids);  // Verify that the delete method was called once
    }
}
