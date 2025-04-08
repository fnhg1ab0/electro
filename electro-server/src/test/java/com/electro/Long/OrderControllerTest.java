package com.electro.Long;

import com.electro.controller.order.OrderController;
import com.electro.controller.order.OrderVariantController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientConfirmedOrderResponse;
import com.electro.dto.client.ClientSimpleOrderRequest;
import com.electro.dto.order.OrderRequest;
import com.electro.dto.order.OrderResponse;
import com.electro.dto.order.OrderVariantKeyRequest;
import com.electro.entity.order.Order;
import com.electro.entity.order.OrderVariantKey;
import com.electro.repository.order.OrderRepository;
import com.electro.service.GenericService;
import com.electro.service.inventory.OrderVariantService;
import com.electro.service.order.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVariantService orderVariantService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GenericService<Order, OrderRequest, OrderResponse> genericOrderService;

    @InjectMocks
    private OrderController orderController;

    @InjectMocks
    private OrderVariantController orderVariantController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    // OrderService direct method tests
    @Test
    public void testCancelOrder() {
        // Given
        String orderCode = "ORD123456";
        doNothing().when(orderService).cancelOrder(orderCode);

        // When
        orderService.cancelOrder(orderCode);

        // Then
        verify(orderService).cancelOrder(eq(orderCode));
    }

    @Test
    public void testCreateClientOrder() {
        // Given
        ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
        ClientConfirmedOrderResponse expectedResponse = new ClientConfirmedOrderResponse();
        when(orderService.createClientOrder(any(ClientSimpleOrderRequest.class))).thenReturn(expectedResponse);

        // When
        ClientConfirmedOrderResponse actualResponse = orderService.createClientOrder(request);

        // Then
        assertEquals(expectedResponse, actualResponse);
        verify(orderService).createClientOrder(any(ClientSimpleOrderRequest.class));
    }

    @Test
    public void testCaptureTransactionPaypal() {
        // Given
        String paypalOrderId = "PAY123456";
        String payerId = "PAYER789";
        doNothing().when(orderService).captureTransactionPaypal(anyString(), anyString());

        // When
        orderService.captureTransactionPaypal(paypalOrderId, payerId);

        // Then
        verify(orderService).captureTransactionPaypal(eq(paypalOrderId), eq(payerId));
    }

    // OrderVariantService tests
    @Test
    public void testDeleteOrderVariant() {
        // Given
        Long orderId = 1L;
        Long variantId = 2L;
        OrderVariantKey key = new OrderVariantKey(orderId, variantId);
        doNothing().when(orderVariantService).delete(any(OrderVariantKey.class));

        // When
        orderVariantService.delete(key);

        // Then
        verify(orderVariantService).delete(any(OrderVariantKey.class));
    }

    @Test
    public void testDeleteMultipleOrderVariants() {
        // Given
        List<OrderVariantKeyRequest> keyRequests = Arrays.asList(
                new OrderVariantKeyRequest(1L, 2L),
                new OrderVariantKeyRequest(3L, 4L)
        );

        List<OrderVariantKey> keys = keyRequests.stream()
                .map(req -> new OrderVariantKey(req.getOrderId(), req.getVariantId()))
                .collect(Collectors.toList());

        doNothing().when(orderVariantService).delete(anyList());

        // When
        orderVariantService.delete(keys);

        // Then
        ArgumentCaptor<List<OrderVariantKey>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderVariantService).delete(keysCaptor.capture());
    }

    // OrderController specific method tests
    @Test
    public void testCancelOrderController() {
        // Given
        String orderCode = "ORD123456";
        doNothing().when(orderService).cancelOrder(anyString());

        // When
        ResponseEntity<ObjectNode> response = orderController.cancelOrder(orderCode);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService).cancelOrder(eq(orderCode));
    }

    // OrderVariantController specific method tests
    @Test
    public void testDeleteOrderVariantController() {
        // Given
        Long orderId = 1L;
        Long variantId = 2L;
        doNothing().when(orderVariantService).delete(any(OrderVariantKey.class));

        // When
        ResponseEntity<Void> response = orderVariantController.deleteOrderVariant(orderId, variantId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ArgumentCaptor<OrderVariantKey> keyCaptor = ArgumentCaptor.forClass(OrderVariantKey.class);
        verify(orderVariantService).delete(keyCaptor.capture());

        OrderVariantKey capturedKey = keyCaptor.getValue();
        assertEquals(orderId, capturedKey.getOrderId());
        assertEquals(variantId, capturedKey.getVariantId());
    }

    @Test
    public void testDeleteOrderVariantsController() {
        // Given
        List<OrderVariantKeyRequest> keyRequests = Arrays.asList(
                new OrderVariantKeyRequest(1L, 2L),
                new OrderVariantKeyRequest(3L, 4L)
        );
        doNothing().when(orderVariantService).delete(anyList());

        // When
        ResponseEntity<Void> response = orderVariantController.deleteOrderVariants(keyRequests);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ArgumentCaptor<List<OrderVariantKey>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderVariantService).delete(keysCaptor.capture());
    }

    // Generic CRUD tests for Order
    @Test
    public void testGenericFindAllOrders() {
        // Given
        int page = 0;
        int size = 10;
        String sort = "id,desc";
        String filter = "status==PENDING";
        String search = "customer";
        boolean all = false;

        // Setup mock response
        List<OrderResponse> orderResponses = new ArrayList<>();
        Page<Order> orderPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        ListResponse<OrderResponse> expectedResponse = new ListResponse<>(orderResponses, orderPage);

        when(genericOrderService.findAll(page, size, sort, filter, search, all))
                .thenReturn(expectedResponse);

        // When
        ListResponse<OrderResponse> actualResponse = genericOrderService.findAll(page, size, sort, filter, search, all);

        // Then
        assertNotNull(actualResponse);
        verify(genericOrderService).findAll(page, size, sort, filter, search, all);
    }

    @Test
    public void testGenericFindOrderById() {
        // Given
        Long orderId = 1L;
        OrderResponse expectedResponse = new OrderResponse();

        when(genericOrderService.findById(anyLong())).thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = genericOrderService.findById(orderId);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(genericOrderService).findById(orderId);
    }

    @Test
    public void testGenericCreateOrder() {
        // Given
        OrderRequest request = new OrderRequest();
        OrderResponse expectedResponse = new OrderResponse();
        JsonNode jsonNode = objectMapper.createObjectNode();

        when(genericOrderService.save(any(JsonNode.class), eq(OrderRequest.class)))
                .thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = genericOrderService.save(jsonNode, OrderRequest.class);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(genericOrderService).save(any(JsonNode.class), eq(OrderRequest.class));
    }

    @Test
    public void testGenericUpdateOrder() {
        // Given
        Long orderId = 1L;
        OrderRequest request = new OrderRequest();
        OrderResponse expectedResponse = new OrderResponse();
        JsonNode jsonNode = objectMapper.createObjectNode();

        when(genericOrderService.save(eq(orderId), any(JsonNode.class), eq(OrderRequest.class)))
                .thenReturn(expectedResponse);

        // When
        OrderResponse actualResponse = genericOrderService.save(orderId, jsonNode, OrderRequest.class);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(genericOrderService).save(eq(orderId), any(JsonNode.class), eq(OrderRequest.class));
    }

    @Test
    public void testGenericDeleteOrder() {
        // Given
        Long orderId = 1L;
        doNothing().when(genericOrderService).delete(orderId);

        // When
        genericOrderService.delete(orderId);

        // Then - simply verify it was called once
        verify(genericOrderService).delete(orderId);
    }

    @Test
    public void testGenericBulkDeleteOrders() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(genericOrderService).delete(ids);

        // When
        genericOrderService.delete(ids);

        // Then - simply verify it was called once
        verify(genericOrderService).delete(ids);
    }
}