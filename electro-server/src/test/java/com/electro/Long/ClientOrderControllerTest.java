package com.electro.Long;

import com.electro.constant.AppConstants;
import com.electro.controller.client.ClientOrderController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientConfirmedOrderResponse;
import com.electro.dto.client.ClientOrderDetailResponse;
import com.electro.dto.client.ClientSimpleOrderRequest;
import com.electro.dto.client.ClientSimpleOrderResponse;
import com.electro.dto.general.NotificationResponse;
import com.electro.entity.general.Notification;
import com.electro.entity.general.NotificationType;
import com.electro.entity.order.Order;
import com.electro.entity.authentication.User;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.client.ClientOrderMapper;
import com.electro.mapper.general.NotificationMapper;
import com.electro.repository.cart.CartRepository;
import com.electro.repository.general.NotificationRepository;
import com.electro.repository.order.OrderRepository;
import com.electro.service.general.NotificationService;
import com.electro.service.order.OrderService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.electro.entity.cashbook.PaymentMethodType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.electro.entity.address.Province;
import com.electro.entity.address.District;
import com.electro.entity.address.Ward;
import com.electro.entity.address.Address;
import com.electro.entity.authentication.Role;
import com.electro.entity.product.Product;
import com.electro.entity.product.Variant;
import com.electro.entity.cart.Cart;
import com.electro.entity.cart.CartVariant;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.authentication.UserRepository;
import com.electro.entity.order.OrderResource;

/**
 * Tests for ClientOrderController
 * - Unit tests: Test controller logic without database interaction
 * - Integration tests: Test controller with actual database operations
 */
public class ClientOrderControllerTest {

    /**
     * Unit Tests for ClientOrderController
     * Test functionality without database interaction
     */
    @Nested
    @DisplayName("Unit Tests - Without database interaction")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {
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
         */
        @Test
        @DisplayName("Unit Test - Get all orders for user")
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
         */
        @Test
        @DisplayName("Unit Test - Get order by code")
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
         */
        @Test
        @DisplayName("Unit Test - Get order not found")
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
         */
        @Test
        @DisplayName("Unit Test - Cancel order")
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
         */
        @Test
        @DisplayName("Unit Test - Create client order")
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
         */
        @Test
        @DisplayName("Unit Test - Payment success and capture transaction")
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
            assertEquals(AppConstants.FRONTEND_HOST + "/payment/success", result.getUrl());
            verify(orderService).captureTransactionPaypal(paypalOrderId, payerId);
        }

        /**
         * Test Case ID: CRO007
         * Test Name: testPaymentCancel
         * Objective: Verify handling of cancelled PayPal payments
         * Input: PayPal token="PAY123"
         * Expected Output: Redirect to cancel page and notification creation
         */
        @Test
        @DisplayName("Unit Test - Payment cancel")
        public void testPaymentCancel() {
            // Given
            String paypalOrderId = "PAY123";

            // Create and set up the order properly
            Order order = new Order();
            order.setCode("ORDER123");

            // Create and set up the user properly
            User user = new User();
            user.setUsername("testuser");
            order.setUser(user);

            // Create notification objects with proper property values
            Notification notification = new Notification()
                    .setUser(user)
                    .setType(NotificationType.CHECKOUT_PAYPAL_CANCEL)
                    .setMessage(String.format("Bạn đã hủy thanh toán PayPal cho đơn hàng %s.", order.getCode()))
                    .setAnchor("/order/detail/" + order.getCode())
                    .setStatus(1);

            NotificationResponse notificationResponse = new NotificationResponse();

            // Set up mocks with proper values
            when(httpServletRequest.getParameter("token")).thenReturn(paypalOrderId);
            when(orderRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(order));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
            when(notificationMapper.entityToResponse(any(Notification.class))).thenReturn(notificationResponse);
            doNothing().when(notificationService).pushNotification(anyString(), any(NotificationResponse.class));

            // When
            RedirectView result = clientOrderController.paymentCancel(httpServletRequest);

            // Then
            assertNotNull(result);
            assertEquals(AppConstants.FRONTEND_HOST + "/payment/cancel", result.getUrl());
            verify(orderRepository).findByPaypalOrderId(paypalOrderId);
            verify(notificationRepository).save(any(Notification.class));
            verify(notificationService).pushNotification(eq(user.getUsername()), any(NotificationResponse.class));
        }

        /**
         * Test Case ID: CRO008
         * Test Name: testPaymentCancelOrderNotFound
         * Objective: Verify error handling when cancelled order is not found
         * Input: PayPal token="NONEXISTENT"
         * Expected Output: ResourceNotFoundException
         */
        @Test
        @DisplayName("Unit Test - Payment cancel order not found")
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

    /**
     * Integration Tests for ClientOrderController
     * Test functionality with actual database operations
     */
    @Nested
    @DisplayName("Integration Tests - Database interactions")
    @SpringBootTest
    @ActiveProfiles("test")
    @Sql(scripts = { "classpath:schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Transactional
    class IntegrationTests {
        @Autowired private ClientOrderController clientOrderController;
        @Autowired private OrderService orderService;
        @Autowired private OrderRepository orderRepository;
        @Autowired private CartRepository cartRepository;
        @Autowired private com.electro.repository.address.ProvinceRepository provinceRepository;
        @Autowired private com.electro.repository.address.DistrictRepository districtRepository;
        @Autowired private com.electro.repository.address.WardRepository wardRepository;
        @Autowired private com.electro.repository.address.AddressRepository addressRepository;
        @Autowired private com.electro.repository.authentication.RoleRepository roleRepository;
        @Autowired private com.electro.repository.order.OrderResourceRepository orderResourceRepository;
        @Autowired private com.electro.repository.product.ProductRepository productRepository;
        @Autowired private com.electro.repository.product.VariantRepository variantRepository;
        @Autowired private EntityManager entityManager;
        @Autowired private CartVariantRepository cartVariantRepository;
        @Autowired private UserRepository userRepository;
        @Autowired private NotificationRepository notificationRepository;

        /**
         * Setup base test data needed for integration tests
         */
        private void setupBaseData() {
            // Create Location data
            Province province = new Province(); province.setName("TestProv"); province.setCode("TP"); province = provinceRepository.save(province);
            District district = new District(); district.setName("TestDist"); district.setCode("TD"); district.setProvince(province); district = districtRepository.save(district);
            Ward ward = new Ward(); ward.setName("TestWard"); ward.setCode("TW"); ward.setDistrict(district); ward = wardRepository.save(ward);
            
            // Create Address
            Address address = new Address(); 
            address.setLine("123 Test St"); 
            address.setProvince(province); 
            address.setDistrict(district); 
            address.setWard(ward);
            address = addressRepository.save(address);
            
            // Create Role
            Role role = roleRepository.findByCode("ROLE_USER").orElseGet(() -> {
                Role r = new Role(); 
                r.setCode("ROLE_USER"); 
                r.setName("User"); 
                r.setStatus(1); 
                return roleRepository.save(r);
            });
            
            // Create default OrderResource
            OrderResource orderResource = new OrderResource(); 
            orderResource.setCode("DEFAULT"); 
            orderResource.setName("Default"); 
            orderResource.setColor("Gray"); 
            orderResource.setStatus(1); 
            orderResource = orderResourceRepository.save(orderResource);
            
            // Create User
            User user = new User(); 
            user.setUsername("testuser"); 
            user.setPassword("pass"); 
            user.setEmail("test@test.com"); 
            user.setFullname("Test User"); 
            user.setPhone("0123456789"); 
            user.setGender("M"); 
            user.setAddress(address);
            user.setStatus(1); 
            user.setRoles(Set.of(role)); 
            user = userRepository.save(user);
            
            // Create Product and Variant
            Product product = new Product(); 
            product.setName("Prod"); 
            product.setCode("P1"); 
            product.setSlug("prod-1"); 
            product.setStatus(1); 
            product = productRepository.save(product);
            
            Variant variant = new Variant(); 
            variant.setProduct(product); 
            variant.setSku("V1"); 
            variant.setPrice(100.0); 
            variant.setCost(50.0); 
            variant.setStatus(1); 
            variant = variantRepository.save(variant);
            
            // Create Cart and persist CartVariant
            Cart cart = new Cart(); 
            cart.setUser(user); 
            cart.setStatus(1); 
            cart = cartRepository.save(cart);
            
            CartVariant cv = new CartVariant(); 
            cv.setCart(cart); 
            cv.setVariant(variant); 
            cv.setQuantity(2);
            cv = cartVariantRepository.save(cv);
            
            cart.setCartVariants(new java.util.HashSet<>(java.util.List.of(cv)));
            entityManager.flush();
        }

        /**
         * Create an order with PayPal payment for integration tests
         */
        private Order setupPaypalOrder() {
            // Create a user if not exists
            User user = userRepository.findByUsername("testuser").orElseThrow(() -> 
                new RuntimeException("Test user not found, run setupBaseData first"));
            
            // Find or create order resource
            OrderResource orderResource = orderResourceRepository.findAll().stream()
                .filter(res -> "DEFAULT".equals(res.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    OrderResource res = new OrderResource();
                    res.setCode("DEFAULT");
                    res.setName("Default");
                    res.setColor("Gray");
                    res.setStatus(1);
                    return orderResourceRepository.save(res);
                });
            
            // Create PayPal order
            Order order = new Order();
            order.setCode("TEST-" + UUID.randomUUID().toString().substring(0, 8));
            order.setStatus(1);
            order.setToName("Test User");
            order.setToPhone("0123456789");
            order.setToAddress("123 Test St");
            order.setToWardName("Test Ward");
            order.setToDistrictName("Test District");
            order.setToProvinceName("Test Province");
            order.setOrderResource(orderResource);
            order.setUser(user);
            order.setPaymentMethodType(PaymentMethodType.PAYPAL);
            order.setPaymentStatus(1);
            order.setPaypalOrderId("TEST-PAYPAL-ORDER-ID");
            order.setPaypalOrderStatus("CREATED");
            
            // Set required non-null fields
            order.setTotalAmount(java.math.BigDecimal.valueOf(100000));
            order.setTax(java.math.BigDecimal.valueOf(0.1));
            order.setShippingCost(java.math.BigDecimal.valueOf(20000));
            order.setTotalPay(java.math.BigDecimal.valueOf(130000));
            
            order.setCreatedAt(new Date().toInstant());
            order.setUpdatedAt(new Date().toInstant());
            
            return orderRepository.save(order);
        }

        /**
         * Test Case ID: CRO009
         * Test Name: testCreateClientOrderIntegration
         * Objective: Verify that the controller creates a client order with cash payment in the database
         * Input: ClientSimpleOrderRequest with PaymentMethodType.CASH
         * Expected Output: Order created in database with valid order code
         */
        @Test
        @DisplayName("Integration Test - Create client order with cash payment")
        void testCreateClientOrderIntegration() {
            setupBaseData();
            
            // Authenticate testuser
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.CASH);
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Call endpoint
            ResponseEntity<ClientConfirmedOrderResponse> response = clientOrderController.createClientOrder(request);
            entityManager.flush();
            
            // Verify response
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody().getOrderCode());
            
            // Verify order exists in DB
            assertTrue(orderRepository.findByCode(response.getBody().getOrderCode()).isPresent());
            
            // Find cart, using findByUsername which might return null
            Cart cart = cartRepository.findByUsername("testuser").orElse(null);
            if (cart != null) {
                assertEquals(2, cart.getStatus(), "Cart should be disabled after order creation");
            }
        }

        /**
         * Test Case ID: CRO010
         * Test Name: testCancelClientOrderIntegration
         * Objective: Verify that the controller correctly cancels an existing order in the database
         * Input: Valid order code from previously created order
         * Expected Output: Order status changed to 5 (cancelled) in database
         */
        @Test
        @DisplayName("Integration Test - Cancel client order")
        void testCancelClientOrderIntegration() {
            setupBaseData();
            
            // Prepare and authenticate user
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.CASH);
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Create order via controller
            ResponseEntity<ClientConfirmedOrderResponse> createResponse = clientOrderController.createClientOrder(request);
            String code = createResponse.getBody().getOrderCode();
            entityManager.flush();
            
            // Cancel via controller
            ResponseEntity<ObjectNode> result = clientOrderController.cancelOrder(code);
            entityManager.flush();
            
            // Verify response
            assertEquals(HttpStatus.OK, result.getStatusCode());
            
            // Verify cancelled status in database
            Order cancelled = orderRepository.findByCode(code).orElseThrow();
            assertEquals(5, cancelled.getStatus(), "Order status should be 5 (cancelled)");
        }
        
        /**
         * Test Case ID: CRO011
         * Test Name: testGetAllOrdersIntegration
         * Objective: Verify that the controller correctly retrieves all orders for a user from the database
         * Input: Authentication with username "testuser"
         * Expected Output: HTTP 200 OK with a ListResponse containing orders from database
         */
        @Test
        @DisplayName("Integration Test - Get all orders for user from database")
        void testGetAllOrdersIntegration() {
            setupBaseData();
            
            // Create order first
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.CASH);
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
            SecurityContextHolder.getContext().setAuthentication(auth);
            clientOrderController.createClientOrder(request);
            entityManager.flush();
            
            // Test getAllOrders endpoint
            ResponseEntity<ListResponse<ClientSimpleOrderResponse>> response = 
                clientOrderController.getAllOrders(auth, 1, 10, "id,desc", null);
            
            // Verify response
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().getContent().isEmpty(), "Should return at least one order");
        }
        
        /**
         * Test Case ID: CRO012
         * Test Name: testGetOrderIntegration
         * Objective: Verify that the controller correctly retrieves a specific order from the database
         * Input: Valid order code
         * Expected Output: HTTP 200 OK with order details from database
         */
        @Test
        @DisplayName("Integration Test - Get order by code from database")
        void testGetOrderIntegration() {
            setupBaseData();
            
            // Create order first
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.CASH);
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
            SecurityContextHolder.getContext().setAuthentication(auth);
            ResponseEntity<ClientConfirmedOrderResponse> createResponse = clientOrderController.createClientOrder(request);
            String orderCode = createResponse.getBody().getOrderCode();
            entityManager.flush();
            
            // Test getOrder endpoint
            ResponseEntity<ClientOrderDetailResponse> response = clientOrderController.getOrder(orderCode);
            
            // Verify response
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(orderCode, response.getBody().getOrderCode());
        }
        
        /**
         * Test Case ID: CRO013
         * Test Name: testPaymentSuccessAndCaptureTransactionIntegration
         * Objective: Verify handling of successful PayPal payments with database
         * Input: HttpServletRequest with PayPal token and PayerID
         * Expected Output: Redirect to success page and order updated in database
         */
        @Test
        @DisplayName("Integration Test - Handle PayPal payment success")
        void testPaymentSuccessAndCaptureTransactionIntegration() {
            setupBaseData();
            Order paypalOrder = setupPaypalOrder();
            
            // Setup HttpServletRequest mock
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter("token")).thenReturn(paypalOrder.getPaypalOrderId());
            when(request.getParameter("PayerID")).thenReturn("TEST-PAYER-ID");
            
            // Call endpoint
            RedirectView result = clientOrderController.paymentSuccessAndCaptureTransaction(request);
            entityManager.flush();
            
            // Verify redirect
            assertNotNull(result);
            assertEquals(AppConstants.FRONTEND_HOST + "/payment/success", result.getUrl());
            
            // Verify order updated in database
            Order updatedOrder = orderRepository.findById(paypalOrder.getId()).orElseThrow();
            assertEquals("COMPLETED", updatedOrder.getPaypalOrderStatus());
            assertEquals(2, updatedOrder.getPaymentStatus(), "Payment status should be 2 (paid)");
        }
        
        /**
         * Test Case ID: CRO014
         * Test Name: testPaymentCancelIntegration
         * Objective: Verify handling of cancelled PayPal payments with database
         * Input: HttpServletRequest with PayPal token
         * Expected Output: Redirect to cancel page and notification created in database
         */
        @Test
        @DisplayName("Integration Test - Handle PayPal payment cancel")
        void testPaymentCancelIntegration() {
            setupBaseData();
            Order paypalOrder = setupPaypalOrder();
            
            // Count notifications before test
            long notificationCountBefore = notificationRepository.count();
            
            // Setup HttpServletRequest mock
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter("token")).thenReturn(paypalOrder.getPaypalOrderId());
            
            // Call endpoint
            RedirectView result = clientOrderController.paymentCancel(request);
            entityManager.flush();
            
            // Verify redirect
            assertNotNull(result);
            assertEquals(AppConstants.FRONTEND_HOST + "/payment/cancel", result.getUrl());
            
            // Verify notification created in database
            long notificationCountAfter = notificationRepository.count();
            assertEquals(notificationCountBefore + 1, notificationCountAfter, "Should create one notification");
            
            // Verify notification details
            List<Notification> notifications = notificationRepository.findAll();
            Optional<Notification> paypalCancelNotification = notifications.stream()
                .filter(n -> n.getType() == NotificationType.CHECKOUT_PAYPAL_CANCEL && 
                       n.getMessage().contains(paypalOrder.getCode()))
                .findFirst();
            
            assertTrue(paypalCancelNotification.isPresent(), "PayPal cancel notification should exist");
            assertEquals(paypalOrder.getUser().getId(), paypalCancelNotification.get().getUser().getId());
        }
    }
}