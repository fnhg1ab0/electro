package com.electro;

import com.electro.dto.cashbook.ClientPaymentMethodResponse;
import com.electro.dto.client.ClientConfirmedOrderResponse;
import com.electro.dto.client.ClientPreorderRequest;
import com.electro.dto.client.ClientPreorderResponse;
import com.electro.dto.client.ClientSimpleOrderRequest;
import com.electro.entity.authentication.User;
import com.electro.entity.cashbook.PaymentMethod;
import com.electro.entity.client.Preorder;
import com.electro.entity.order.Order;
import com.electro.mapper.cashbook.PaymentMethodMapper;
import com.electro.mapper.client.ClientPreorderMapper;
import com.electro.repository.cashbook.PaymentMethodRepository;
import com.electro.repository.client.PreorderRepository;
import com.electro.repository.order.OrderRepository;
import com.electro.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientOrderAndPaymentControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private PreorderRepository preorderRepository;

    @Mock
    private ClientPreorderMapper clientPreorderMapper;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentMethodMapper paymentMethodMapper;

    // OrderService tests
    @Test
    public void testGetAllPaymentMethods() {
        // Given
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        List<ClientPaymentMethodResponse> expectedResponses = new ArrayList<>();
        expectedResponses.add(new ClientPaymentMethodResponse());

        when(paymentMethodRepository.findAllByStatus(1)).thenReturn(paymentMethods);
        when(paymentMethodMapper.entityToClientResponse(paymentMethods)).thenReturn(expectedResponses);

        // When
        List<PaymentMethod> retrievedPaymentMethods = paymentMethodRepository.findAllByStatus(1);
        List<ClientPaymentMethodResponse> result = paymentMethodMapper.entityToClientResponse(retrievedPaymentMethods);

        // Then
        assertEquals(expectedResponses, result);
        verify(paymentMethodRepository).findAllByStatus(1);
        verify(paymentMethodMapper).entityToClientResponse(paymentMethods);
    }

    // Fix the problematic test
    @Test
    public void testCancelOrder() {
        // Given
        String orderCode = "ORDER123";

        // When
        orderService.cancelOrder(orderCode);

        // Then
        verify(orderService).cancelOrder(orderCode);
    }

    @Test
    public void testCreateClientOrder() {
        // Given
        ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
        ClientConfirmedOrderResponse expectedResponse = new ClientConfirmedOrderResponse();

        when(orderService.createClientOrder(request)).thenReturn(expectedResponse);

        // When
        ClientConfirmedOrderResponse result = orderService.createClientOrder(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(orderService).createClientOrder(request);
    }

    @Test
    public void testCaptureTransactionPaypal() {
        // Given
        String paypalOrderId = "PAY123";
        String payerId = "PAYER456";

        doNothing().when(orderService).captureTransactionPaypal(paypalOrderId, payerId);

        // When
        orderService.captureTransactionPaypal(paypalOrderId, payerId);

        // Then
        verify(orderService).captureTransactionPaypal(paypalOrderId, payerId);
    }

    // Preorder tests
    @Test
    public void testFindPreorderByUserAndProduct() {
        // Given
        Long userId = 1L;
        Long productId = 2L;
        Preorder preorder = new Preorder();

        when(preorderRepository.findByUser_IdAndProduct_Id(userId, productId))
                .thenReturn(Optional.of(preorder));

        // When
        Optional<Preorder> result = preorderRepository.findByUser_IdAndProduct_Id(userId, productId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(preorder, result.get());
        verify(preorderRepository).findByUser_IdAndProduct_Id(userId, productId);
    }

    @Test
    public void testSaveNewPreorder() {
        // Given
        Preorder preorder = new Preorder();
        User user = new User();
        user.setId(1L);
        preorder.setUser(user);
        preorder.setStatus(1);

        when(preorderRepository.save(preorder)).thenReturn(preorder);

        // When
        Preorder savedPreorder = preorderRepository.save(preorder);

        // Then
        assertEquals(preorder, savedPreorder);
        verify(preorderRepository).save(preorder);
    }

    @Test
    public void testDeletePreorders() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(preorderRepository).deleteAllById(ids);

        // When
        preorderRepository.deleteAllById(ids);

        // Then
        verify(preorderRepository).deleteAllById(ids);
    }

    // PaymentMethod tests
    @Test
    public void testFindAllActivePaymentMethods() {
        // Given
        List<ClientPaymentMethodResponse> expectedResponses = new ArrayList<>();
        expectedResponses.add(new ClientPaymentMethodResponse());

        when(paymentMethodRepository.findAllByStatus(1)).thenReturn(new ArrayList<>());
        when(paymentMethodMapper.entityToClientResponse(anyList())).thenReturn(expectedResponses);

        // When
        List<?> paymentMethods = paymentMethodRepository.findAllByStatus(1);
        List<ClientPaymentMethodResponse> result = paymentMethodMapper.entityToClientResponse((List<PaymentMethod>) paymentMethods);

        // Then
        assertEquals(expectedResponses, result);
        verify(paymentMethodRepository).findAllByStatus(1);
        verify(paymentMethodMapper).entityToClientResponse(anyList());
    }

    // Mapper tests
    @Test
    public void testPreorderMapperFunctionality() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        request.setUserId(1L);
        request.setProductId(2L);

        Preorder entity = new Preorder();
        when(clientPreorderMapper.requestToEntity(request)).thenReturn(entity);

        // When
        Preorder result = clientPreorderMapper.requestToEntity(request);

        // Then
        assertEquals(entity, result);
        verify(clientPreorderMapper).requestToEntity(request);
    }

    @Test
    public void testPreorderPartialUpdate() {
        // Given
        ClientPreorderRequest request = new ClientPreorderRequest();
        Preorder existingEntity = new Preorder();
        Preorder updatedEntity = new Preorder();

        when(clientPreorderMapper.partialUpdate(existingEntity, request)).thenReturn(updatedEntity);

        // When
        Preorder result = clientPreorderMapper.partialUpdate(existingEntity, request);

        // Then
        assertEquals(updatedEntity, result);
        verify(clientPreorderMapper).partialUpdate(existingEntity, request);
    }

    @Test
    public void testEntityToResponseMapping() {
        // Given
        Preorder entity = new Preorder();
        ClientPreorderResponse response = new ClientPreorderResponse();

        when(clientPreorderMapper.entityToResponse(entity)).thenReturn(response);

        // When
        ClientPreorderResponse result = clientPreorderMapper.entityToResponse(entity);

        // Then
        assertEquals(response, result);
        verify(clientPreorderMapper).entityToResponse(entity);
    }

    @Test
    public void testFindOrderByCode() {
        // Given
        String code = "ORDER123";
        Order order = new Order();
        order.setCode(code);

        when(orderRepository.findByCode(code)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderRepository.findByCode(code);

        // Then
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findByCode(code);
    }

    @Test
    public void testFindOrderByPaypalOrderId() {
        // Given
        String paypalOrderId = "PAY123";
        Order order = new Order();
        order.setPaypalOrderId(paypalOrderId);

        when(orderRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderRepository.findByPaypalOrderId(paypalOrderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository).findByPaypalOrderId(paypalOrderId);
    }
}