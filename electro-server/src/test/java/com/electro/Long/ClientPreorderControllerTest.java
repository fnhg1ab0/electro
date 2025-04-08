package com.electro.Long;

import com.electro.controller.client.ClientPreorderController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientPreorderRequest;
import com.electro.dto.client.ClientPreorderResponse;
import com.electro.entity.authentication.User;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Test
    public void testCreatePreorderNew() throws Exception {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder newPreorder = new Preorder();
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(1L, 2L)).thenReturn(Optional.empty());
        when(clientPreorderMapper.requestToEntity(request)).thenReturn(newPreorder);
        when(preorderRepository.save(newPreorder)).thenReturn(newPreorder);
        when(clientPreorderMapper.entityToResponse(newPreorder)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.createPreorder(request);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(preorderRepository).findByUser_IdAndProduct_Id(1L, 2L);
        verify(clientPreorderMapper).requestToEntity(request);
        verify(preorderRepository).save(newPreorder);
        verify(clientPreorderMapper).entityToResponse(newPreorder);
    }

    @Test
    public void testCreatePreorderReactivate() throws Exception {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        existingPreorder.setStatus(0);
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(1L, 2L)).thenReturn(Optional.of(existingPreorder));
        when(preorderRepository.save(any(Preorder.class))).thenReturn(existingPreorder);
        when(clientPreorderMapper.entityToResponse(existingPreorder)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.createPreorder(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        assertEquals(1, existingPreorder.getStatus());
        verify(preorderRepository).findByUser_IdAndProduct_Id(1L, 2L);
        verify(preorderRepository).save(existingPreorder);
        verify(clientPreorderMapper).entityToResponse(existingPreorder);
    }

    @Test
    public void testCreatePreorderDuplicate() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        existingPreorder.setStatus(1);

        when(preorderRepository.findByUser_IdAndProduct_Id(1L, 2L)).thenReturn(Optional.of(existingPreorder));

        // When & Then
        assertThrows(Exception.class, () -> clientPreorderController.createPreorder(request));
        verify(preorderRepository).findByUser_IdAndProduct_Id(1L, 2L);
        verify(preorderRepository, never()).save(any(Preorder.class));
    }

    @Test
    public void testUpdatePreorder() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder existingPreorder = new Preorder();
        Preorder updatedPreorder = new Preorder();
        ClientPreorderResponse expectedResponse = new ClientPreorderResponse();

        when(preorderRepository.findByUser_IdAndProduct_Id(1L, 2L)).thenReturn(Optional.of(existingPreorder));
        when(clientPreorderMapper.partialUpdate(existingPreorder, request)).thenReturn(updatedPreorder);
        when(preorderRepository.save(updatedPreorder)).thenReturn(updatedPreorder);
        when(clientPreorderMapper.entityToResponse(updatedPreorder)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientPreorderResponse> result = clientPreorderController.updatePreorder(request);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(preorderRepository).findByUser_IdAndProduct_Id(1L, 2L);
        verify(clientPreorderMapper).partialUpdate(existingPreorder, request);
        verify(preorderRepository).save(updatedPreorder);
    }

    @Test
    public void testUpdatePreorderNotFound() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        when(preorderRepository.findByUser_IdAndProduct_Id(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientPreorderController.updatePreorder(request));
        verify(preorderRepository).findByUser_IdAndProduct_Id(1L, 2L);
    }

    @Test
    public void testDeletePreorders() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(preorderRepository).deleteAllById(ids);

        // When
        ResponseEntity<Void> result = clientPreorderController.deletePreorders(ids);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(preorderRepository).deleteAllById(ids);
    }
}