package com.electro.Long;

import com.electro.controller.client.ClientPreorderController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientPreorderRequest;
import com.electro.dto.client.ClientPreorderResponse;
import com.electro.entity.client.Preorder;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.client.ClientPreorderMapper;
import com.electro.repository.client.PreorderRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for ClientPreorderController
 * This class tests the functionality of managing preorders in the client API
 */
@ExtendWith(MockitoExtension.class)
public class ClientPreorderControllerTest {

    @Mock
    private PreorderRepository preorderRepository;

    @Mock
    private ClientPreorderMapper clientPreorderMapper;

    @InjectMocks
    private ClientPreorderController clientPreorderController;

    @Mock
    private Authentication authentication;

    /**
     * Test Case ID: CPO001
     * Test Name: testGetAllPreorders
     * Objective: Verify that the controller correctly retrieves all preorders for a user
     * Input: Page=1, Size=10, Sort="updatedAt,desc", Filter=status==1, Username="testuser"
     * Expected Output: HTTP 200 OK with a list of preorders
     * Note: Tests pagination and sorting for client preorders
     */
    @Test
    public void testGetAllPreorders() {
        // Given
        int page = 1;
        int size = 10;
        String sort = "updatedAt,desc";
        String filter = "status==1";
        String username = "testuser";

        when(authentication.getName()).thenReturn(username);

        List<Preorder> preorders = new ArrayList<>();
        Page<Preorder> preorderPage = new PageImpl<>(preorders, PageRequest.of(page - 1, size), 0);

        when(preorderRepository.findAllByUsername(eq(username), eq(sort), eq(filter), any(PageRequest.class)))
                .thenReturn(preorderPage);

        // Remove the unnecessary stubbing for clientPreorderMapper.entityToResponse

        // When
        ResponseEntity<ListResponse<ClientPreorderResponse>> result =
                clientPreorderController.getAllPreorders(authentication, page, size, sort, filter);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(preorderRepository).findAllByUsername(eq(username), eq(sort), eq(filter), any(PageRequest.class));
    }

    /**
     * Test Case ID: CPO002
     * Test Name: testCreatePreorderNew
     * Objective: Verify that the controller correctly creates a new preorder
     * Input: ClientPreorderRequest with userId=1L and productId=2L
     * Expected Output: HTTP 201 CREATED with preorder details
     * Note: Tests creation of a new preorder when none exists for this user-product combination
     */
    @Test
    public void testCreatePreorderNew() throws Exception {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder entity = new Preorder();
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(clientPreorderMapper.requestToEntity(any(ClientPreorderRequest.class))).thenReturn(entity);
        when(preorderRepository.save(any(Preorder.class))).thenReturn(entity);
        when(clientPreorderMapper.entityToResponse(any(Preorder.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.createPreorder(request);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(preorderRepository).findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());
        verify(clientPreorderMapper).requestToEntity(request);
        verify(preorderRepository).save(entity);
        verify(clientPreorderMapper).entityToResponse(entity);
    }

    /**
     * Test Case ID: CPO003
     * Test Name: testCreatePreorderExistingInactive
     * Objective: Verify reactivation of an inactive preorder
     * Input: ClientPreorderRequest for a preorder that exists but is inactive
     * Expected Output: HTTP 200 OK with updated preorder details
     * Note: Tests reactivation of an existing but inactive preorder
     */
    @Test
    public void testCreatePreorderExistingInactive() throws Exception {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        existingPreorder.setStatus(0);
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(anyLong(), anyLong())).thenReturn(Optional.of(existingPreorder));
        when(preorderRepository.save(any(Preorder.class))).thenReturn(existingPreorder);
        when(clientPreorderMapper.entityToResponse(any(Preorder.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.createPreorder(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        assertEquals(1, existingPreorder.getStatus());
        verify(preorderRepository).findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());
        verify(preorderRepository).save(existingPreorder);
        verify(clientPreorderMapper).entityToResponse(existingPreorder);
    }

    /**
     * Test Case ID: CPO004
     * Test Name: testCreatePreorderDuplicate
     * Objective: Verify error handling when attempting to create a duplicate active preorder
     * Input: ClientPreorderRequest for a preorder that already exists and is active
     * Expected Output: Exception with message "Duplicated preorder"
     * Note: Tests error handling for duplicate preorder creation
     */
    @Test
    public void testCreatePreorderDuplicate() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        existingPreorder.setStatus(1);

        when(preorderRepository.findByUser_IdAndProduct_Id(anyLong(), anyLong())).thenReturn(Optional.of(existingPreorder));

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> clientPreorderController.createPreorder(request));
        assertEquals("Duplicated preorder", exception.getMessage());
        verify(preorderRepository).findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());
    }

    /**
     * Test Case ID: CPO005
     * Test Name: testUpdatePreorder
     * Objective: Verify that the controller correctly updates an existing preorder
     * Input: ClientPreorderRequest with updated preorder information
     * Expected Output: HTTP 200 OK with updated preorder details
     * Note: Tests preorder update functionality
     */
    @Test
    public void testUpdatePreorder() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        Preorder updatedPreorder = new Preorder();
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(anyLong(), anyLong())).thenReturn(Optional.of(existingPreorder));
        when(clientPreorderMapper.partialUpdate(any(Preorder.class), any(ClientPreorderRequest.class))).thenReturn(updatedPreorder);
        when(preorderRepository.save(any(Preorder.class))).thenReturn(updatedPreorder);
        when(clientPreorderMapper.entityToResponse(any(Preorder.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.updatePreorder(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(preorderRepository).findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());
        verify(clientPreorderMapper).partialUpdate(existingPreorder, request);
        verify(preorderRepository).save(updatedPreorder);
        verify(clientPreorderMapper).entityToResponse(updatedPreorder);
    }

    /**
     * Test Case ID: CPO006
     * Test Name: testUpdatePreorderNotFound
     * Objective: Verify error handling when attempting to update a non-existent preorder
     * Input: ClientPreorderRequest for a preorder that doesn't exist
     * Expected Output: ResourceNotFoundException
     * Note: Tests error handling for updating non-existent preorders
     */
    @Test
    public void testUpdatePreorderNotFound() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        when(preorderRepository.findByUser_IdAndProduct_Id(anyLong(), anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> clientPreorderController.updatePreorder(request));
        verify(preorderRepository).findByUser_IdAndProduct_Id(request.getUserId(), request.getProductId());
    }

    /**
     * Test Case ID: CPO007
     * Test Name: testDeletePreorders
     * Objective: Verify that the controller correctly deletes multiple preorders
     * Input: List of preorder IDs to delete
     * Expected Output: HTTP 204 NO CONTENT
     * Note: Tests bulk deletion of preorders
     */
    @Test
    public void testDeletePreorders() {
        // Given
        List<Long> ids = List.of(1L, 2L, 3L);
        doNothing().when(preorderRepository).deleteAllById(any());

        // When
        ResponseEntity<Void> result = clientPreorderController.deletePreorders(ids);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(preorderRepository, times(1)).deleteAllById(ids);
    }
}