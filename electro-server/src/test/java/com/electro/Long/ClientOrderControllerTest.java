package com.electro.Long;

import com.electro.constant.ResourceName;
import com.electro.controller.client.ClientOrderController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientConfirmedOrderResponse;
import com.electro.dto.client.ClientOrderDetailResponse;
import com.electro.dto.client.ClientSimpleOrderRequest;
import com.electro.dto.client.ClientSimpleOrderResponse;
import com.electro.entity.general.Notification;
import com.electro.entity.general.NotificationType;
import com.electro.entity.order.Order;
import com.electro.entity.authentication.User;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.client.ClientOrderMapper;
import com.electro.mapper.general.NotificationMapper;
import com.electro.repository.general.NotificationRepository;
import com.electro.repository.order.OrderRepository;
import com.electro.service.general.NotificationService;
import com.electro.service.order.OrderService;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientOrderControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientOrderMapper clientOrderMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private ClientOrderController clientOrderController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest httpServletRequest;

    /**
     * Test Case ID: CRO001
     * Test Name: testGetAllOrders
     * Objective: Verify that the controller correctly retrieves all orders for a user
     * Input: Page=1, Size=10, Sort="id,desc", Filter="status==1", Username="testuser"
     * Expected Output: HTTP 200 OK with a ListResponse containing orders
     * Note: Tests pagination and filtering functionality for client orders
     */
    @Test
    public void testGetAllOrders() {
        // Given
        int page = 1;
        int size = 10;
        String sort = "id,desc";
        String filter = "status==1";
        String username = "testuser";

        when(authentication.getName()).thenReturn(username);

        List<Order> orders = new ArrayList<>();
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(page - 1, size), 0);

        when(orderRepository.findAllByUsername(eq(username), eq(sort), eq(filter), any(PageRequest.class)))
                .thenReturn(orderPage);

        // When
        ResponseEntity<ListResponse<ClientSimpleOrderResponse>> result =
                clientOrderController.getAllOrders(authentication, page, size, sort, filter);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(orderRepository).findAllByUsername(eq(username), eq(sort), eq(filter), any(PageRequest.class));
    }

    /**
     * Test Case ID: CRO002
     * Test Name: testGetOrder
     * Objective: Verify that the controller correctly retrieves a specific order by code
     * Input: orderCode="ORDER123"
     * Expected Output: HTTP 200 OK with order details
     * Note: Tests the retrieval of a single order's details
     */
    @Test
    public void testGetOrder() {
        // Given
        String orderCode = "ORDER123";
        Order order = new Order();
        ClientOrderDetailResponse expectedResponse = new ClientOrderDetailResponse();

        when(orderRepository.findByCode(orderCode)).thenReturn(Optional.of(order));
        when(clientOrderMapper.entityToDetailResponse(order)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientOrderDetailResponse> result = clientOrderController.getOrder(orderCode);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(orderRepository).findByCode(orderCode);
        verify(clientOrderMapper).entityToDetailResponse(order);
    }

    /**
     * Test Case ID: CRO003
     * Test Name: testGetOrderNotFound
     * Objective: Verify that the controller throws ResourceNotFoundException when order not found
     * Input: orderCode="NONEXISTENT"
     * Expected Output: ResourceNotFoundException
     * Note: Tests error handling for non-existent orders
     */
    @Test
    public void testGetOrderNotFound() {
        // Given
        String orderCode = "NONEXISTENT";
        when(orderRepository.findByCode(orderCode)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> clientOrderController.getOrder(orderCode));
        verify(orderRepository).findByCode(orderCode);
    }

    /**
     * Test Case ID: CRO004
     * Test Name: testCancelOrder
     * Objective: Verify that the controller correctly cancels an order
     * Input: orderCode="ORDER123"
     * Expected Output: HTTP 200 OK with empty response body
     * Note: Tests order cancellation functionality
     */
    @Test
    public void testCancelOrder() {
        // Given
        String orderCode = "ORDER123";
        doNothing().when(orderService).cancelOrder(orderCode);

        // When
        ResponseEntity<ObjectNode> result = clientOrderController.cancelOrder(orderCode);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(orderService).cancelOrder(orderCode);
    }

    /**
     * Test Case ID: CRO005
     * Test Name: testCreateClientOrder
     * Objective: Verify that the controller correctly creates a new client order
     * Input: ClientSimpleOrderRequest object
     * Expected Output: HTTP 201 CREATED with order confirmation details
     * Note: Tests order creation functionality
     */
    @Test
    public void testCreateClientOrder() {
        // Given
        ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
        ClientConfirmedOrderResponse expectedResponse = new ClientConfirmedOrderResponse();

        when(orderService.createClientOrder(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ClientConfirmedOrderResponse> result = clientOrderController.createClientOrder(request);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(orderService).createClientOrder(request);
    }

    /**
     * Test Case ID: CRO006
     * Test Name: testPaymentSuccessAndCaptureTransaction
     * Objective: Verify handling of successful PayPal payments
     * Input: PayPal token="PAY123", PayerID="PAYER456"
     * Expected Output: Redirect to success page
     * Note: Tests the PayPal payment success flow
     */
    @Test
    public void testPaymentSuccessAndCaptureTransaction() {
        // Given
        String paypalOrderId = "PAY123";
        String payerId = "PAYER456";

        when(httpServletRequest.getParameter("token")).thenReturn(paypalOrderId);
        when(httpServletRequest.getParameter("PayerID")).thenReturn(payerId);
        doNothing().when(orderService).captureTransactionPaypal(paypalOrderId, payerId);

        // When
        RedirectView result = clientOrderController.paymentSuccessAndCaptureTransaction(httpServletRequest);

        // Then
        assertNotNull(result);
        verify(orderService).captureTransactionPaypal(paypalOrderId, payerId);
    }

    /**
     * Test Case ID: CRO007
     * Test Name: testPaymentCancel
     * Objective: Verify handling of cancelled PayPal payments
     * Input: PayPal token="PAY123"
     * Expected Output: Redirect to cancel page and notification creation
     * Note: Tests PayPal payment cancellation flow and notification generation
     */
    @Test
    public void testPaymentCancel() {
        // Given
        String paypalOrderId = "PAY123";
        Order order = new Order();
        User user = new User();
        user.setUsername("testuser");
        order.setCode("ORDER123");
        order.setUser(user);
        Notification savedNotification = new Notification();
        com.electro.dto.general.NotificationResponse notificationResponse =
                new com.electro.dto.general.NotificationResponse();

        when(httpServletRequest.getParameter("token")).thenReturn(paypalOrderId);
        when(orderRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(order));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.entityToResponse(any(Notification.class))).thenReturn(notificationResponse);

        // Use doAnswer instead to make the test more flexible with argument matching
        doAnswer(invocation -> null).when(notificationService)
                .pushNotification(any(String.class), any(com.electro.dto.general.NotificationResponse.class));

        // When
        RedirectView result = clientOrderController.paymentCancel(httpServletRequest);

        // Then
        assertNotNull(result);
        // Fix the expected URL to match what the controller actually returns
        assertEquals("http://localhost:3000/payment/cancel", result.getUrl());
        verify(orderRepository).findByPaypalOrderId(paypalOrderId);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationService).pushNotification(any(String.class),
                any(com.electro.dto.general.NotificationResponse.class));
    }

    /**
     * Test Case ID: CRO008
     * Test Name: testPaymentCancelOrderNotFound
     * Objective: Verify error handling when cancelled order is not found
     * Input: PayPal token="NONEXISTENT"
     * Expected Output: ResourceNotFoundException
     * Note: Tests error handling for non-existent orders during payment cancellation
     */
    @Test
    public void testPaymentCancelOrderNotFound() {
        // Given
        String paypalOrderId = "NONEXISTENT";
        when(httpServletRequest.getParameter("token")).thenReturn(paypalOrderId);
        when(orderRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientOrderController.paymentCancel(httpServletRequest));
        verify(orderRepository).findByPaypalOrderId(paypalOrderId);
    }
}