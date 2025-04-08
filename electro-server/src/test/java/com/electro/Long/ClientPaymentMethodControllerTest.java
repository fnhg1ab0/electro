package com.electro.Long;

import com.electro.controller.client.ClientPaymentMethodController;
import com.electro.dto.CollectionWrapper;
import com.electro.dto.cashbook.ClientPaymentMethodResponse;
import com.electro.entity.cashbook.PaymentMethod;
import com.electro.mapper.cashbook.PaymentMethodMapper;
import com.electro.repository.cashbook.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientPaymentMethodControllerTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentMethodMapper paymentMethodMapper;

    @InjectMocks
    private ClientPaymentMethodController clientPaymentMethodController;

    /**
     * Test Case ID: TC-CPM-001
     * Test Name: testGetAllPaymentMethods
     * Objective: Verify that the controller correctly retrieves all active payment methods
     * Input: None
     * Expected Output: HTTP 200 OK with a collection of payment methods
     * Note: Tests retrieval of payment methods with status = 1
     */
    @Test
    public void testGetAllPaymentMethods() {
        // Given
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        List<ClientPaymentMethodResponse> expectedResponses = new ArrayList<>();
        ClientPaymentMethodResponse response = new ClientPaymentMethodResponse();
        expectedResponses.add(response);

        when(paymentMethodRepository.findAllByStatus(1)).thenReturn(paymentMethods);
        when(paymentMethodMapper.entityToClientResponse(paymentMethods)).thenReturn(expectedResponses);

        // When
        ResponseEntity<CollectionWrapper<ClientPaymentMethodResponse>> result =
                clientPaymentMethodController.getAllPaymentMethods();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(paymentMethodRepository).findAllByStatus(1);
        verify(paymentMethodMapper).entityToClientResponse(paymentMethods);
    }
}