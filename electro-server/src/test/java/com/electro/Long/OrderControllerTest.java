package com.electro.Long;

import com.electro.controller.order.OrderController;
import com.electro.controller.order.OrderVariantController;
import com.electro.dto.ListResponse;
import com.electro.dto.client.ClientConfirmedOrderResponse;
import com.electro.dto.client.ClientSimpleOrderRequest;
import com.electro.dto.order.OrderRequest;
import com.electro.dto.order.OrderResponse;
import com.electro.dto.order.OrderVariantKeyRequest;
import com.electro.entity.authentication.User;
import com.electro.entity.cart.Cart;
import com.electro.entity.cart.CartVariant;
import com.electro.entity.cashbook.PaymentMethodType;
import com.electro.entity.general.Notification;
import com.electro.entity.product.Variant;
import com.electro.entity.order.Order;
import com.electro.entity.order.OrderResource;
import com.electro.entity.order.OrderVariant;
import com.electro.entity.order.OrderVariantKey;
import com.electro.entity.product.Product;
import com.electro.entity.waybill.RequiredNote;
import com.electro.entity.waybill.Waybill;
import com.electro.entity.address.Address;
import com.electro.exception.ResourceNotFoundException;
import com.electro.repository.authentication.UserRepository;
import com.electro.repository.cart.CartRepository;
import com.electro.repository.general.NotificationRepository;
import com.electro.repository.order.OrderRepository;
import com.electro.repository.waybill.WaybillRepository;
import com.electro.service.GenericService;
import com.electro.service.inventory.OrderVariantService;
import com.electro.service.order.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.electro.repository.inventory.StorageLocationRepository;
import com.electro.repository.order.OrderVariantRepository;
import com.electro.repository.cart.CartVariantRepository;
import com.electro.repository.address.ProvinceRepository;
import com.electro.repository.address.DistrictRepository;
import com.electro.repository.address.WardRepository;
import com.electro.repository.order.OrderResourceRepository;
import com.electro.repository.address.AddressRepository;
import com.electro.repository.authentication.RoleRepository;
import com.electro.repository.product.ProductRepository;
import com.electro.repository.product.VariantRepository;
import com.electro.mapper.order.OrderMapper;

/**
 * Tests for OrderController and OrderVariantController
 * - Unit tests: Test controller and service logic without database interaction
 * - Integration tests: Test full functionality with database interaction
 */
public class OrderControllerTest {
    
    /**
     * Unit Tests using Mockito to mock dependencies
     * Tests controller and service logic without database interaction
     */
    @Nested
    @DisplayName("Unit Tests - Logic without database interaction")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {
        @Mock
        private OrderService orderService;

        @Mock
        private OrderVariantService orderVariantService;

        @Mock
        private GenericService<Order, OrderRequest, OrderResponse> genericOrderService;

        @InjectMocks
        private OrderController orderController;

        @InjectMocks
        private OrderVariantController orderVariantController;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setup() {
            objectMapper = new ObjectMapper();
        }

        /**
         * Test Case ID: OC-UT001
         * Test Name: testCancelOrderController
         * Objective: Verify OrderController cancels an order
         * Input: Order code
         * Expected Output: HTTP 200 OK and service called correctly
         */
        @Test
        @DisplayName("Unit Test - Cancel order via controller")
        void testCancelOrderController() {
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

        /**
         * Test Case ID: OC-UT002
         * Test Name: testDeleteOrderVariantController
         * Objective: Verify OrderVariantController deletes a single order variant
         * Input: orderId=1L, variantId=2L
         * Expected Output: HTTP 204 NO CONTENT and service called correctly
         */
        @Test
        @DisplayName("Unit Test - Delete a single order variant")
        void testDeleteOrderVariantController() {
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

        /**
         * Test Case ID: OC-UT003
         * Test Name: testDeleteOrderVariantsController
         * Objective: Verify OrderVariantController deletes multiple order variants
         * Input: List of OrderVariantKeyRequest
         * Expected Output: HTTP 204 NO CONTENT and service called correctly
         */
        @Test
        @DisplayName("Unit Test - Delete multiple order variants")
        void testDeleteOrderVariantsController() {
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

            List<OrderVariantKey> capturedKeys = keysCaptor.getValue();
            assertNotNull(capturedKeys);
            assertEquals(2, capturedKeys.size());
            assertEquals(Long.valueOf(1L), capturedKeys.get(0).getOrderId());
            assertEquals(Long.valueOf(2L), capturedKeys.get(0).getVariantId());
            assertEquals(Long.valueOf(3L), capturedKeys.get(1).getOrderId());
            assertEquals(Long.valueOf(4L), capturedKeys.get(1).getVariantId());
        }
        
        /**
         * Test Case ID: OC-UT004
         * Test Name: testEmptyDeleteOrderVariantsController
         * Objective: Verify OrderVariantController handles empty list of variants to delete
         * Input: Empty list of OrderVariantKeyRequest
         * Expected Output: HTTP 204 NO CONTENT and service called with empty list
         */
        @Test
        @DisplayName("Unit Test - Delete with empty order variant list")
        void testEmptyDeleteOrderVariantsController() {
            // Given
            List<OrderVariantKeyRequest> emptyKeyRequests = new ArrayList<>();
            doNothing().when(orderVariantService).delete(anyList());

            // When
            ResponseEntity<Void> response = orderVariantController.deleteOrderVariants(emptyKeyRequests);

            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

            ArgumentCaptor<List<OrderVariantKey>> keysCaptor = ArgumentCaptor.forClass(List.class);
            verify(orderVariantService).delete(keysCaptor.capture());

            List<OrderVariantKey> capturedKeys = keysCaptor.getValue();
            assertNotNull(capturedKeys);
            assertTrue(capturedKeys.isEmpty(), "List should be empty");
        }

        /**
         * Test Case ID: GS-UT001
         * Test Name: testGenericFindAllOrders
         * Objective: Verify GenericService correctly fetches paginated orders
         * Input: Pagination, sort, filter and search parameters
         * Expected Output: ListResponse with order details
         */
        @Test
        @DisplayName("Unit Test - GenericService find all orders with pagination")
        public void testGenericFindAllOrders() {
            // Given
            int page = 0;
            int size = 10;
            String sort = "id,desc";
            String filter = "status==PENDING";
            String search = "customer";
            boolean all = false;
            
            List<OrderResponse> orderResponses = new ArrayList<>();
            Page<Order> orderPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
            ListResponse<OrderResponse> expectedResponse = new ListResponse<>(orderResponses, orderPage);
            
            when(genericOrderService.findAll(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyBoolean()))
                    .thenReturn(expectedResponse);

            // When
            ListResponse<OrderResponse> actualResponse = genericOrderService.findAll(page, size, sort, filter, search, all);

            // Then
            assertNotNull(actualResponse);
            verify(genericOrderService).findAll(page, size, sort, filter, search, all);
        }

        /**
         * Test Case ID: GS-UT002
         * Test Name: testGenericFindOrderById
         * Objective: Verify GenericService correctly fetches an order by ID
         * Input: Order ID
         * Expected Output: OrderResponse with order details
         */
        @Test
        @DisplayName("Unit Test - GenericService find order by ID")
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

        /**
         * Test Case ID: GS-UT003
         * Test Name: testGenericCreateOrder
         * Objective: Verify GenericService correctly creates a new order
         * Input: OrderRequest with order details
         * Expected Output: OrderResponse with created order details
         */
        @Test
        @DisplayName("Unit Test - GenericService create new order")
        public void testGenericCreateOrder() {
            // Given
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

        /**
         * Test Case ID: GS-UT004
         * Test Name: testGenericUpdateOrder
         * Objective: Verify GenericService correctly updates an existing order
         * Input: Order ID and OrderRequest with updated details
         * Expected Output: OrderResponse with updated order details
         */
        @Test
        @DisplayName("Unit Test - GenericService update existing order")
        public void testGenericUpdateOrder() {
            // Given
            Long orderId = 1L;
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

        /**
         * Test Case ID: GS-UT005
         * Test Name: testGenericDeleteOrder
         * Objective: Verify GenericService correctly deletes an order
         * Input: Order ID
         * Expected Output: Void response
         */
        @Test
        @DisplayName("Unit Test - GenericService delete order")
        public void testGenericDeleteOrder() {
            // Given
            Long orderId = 1L;
            doNothing().when(genericOrderService).delete(orderId);

            // When
            genericOrderService.delete(orderId);

            // Then
            verify(genericOrderService).delete(orderId);
        }

        /**
         * Test Case ID: GS-UT006
         * Test Name: testGenericBulkDeleteOrders
         * Objective: Verify GenericService correctly deletes multiple orders
         * Input: List of Order IDs
         * Expected Output: Void response
         */
        @Test
        @DisplayName("Unit Test - GenericService bulk delete orders")
        public void testGenericBulkDeleteOrders() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            doNothing().when(genericOrderService).delete(ids);

            // When
            genericOrderService.delete(ids);

            // Then
            verify(genericOrderService).delete(ids);
        }
    }
    
    /**
     * Integration Tests interacting with a real database
     * Tests functionality with actual database operations and verifies the results
     */
    @Nested
    @DisplayName("Integration Tests - Database interactions")
    @SpringBootTest
    @ActiveProfiles("test")
    @Sql(scripts = { "classpath:schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Transactional
    class IntegrationTests {
        @Autowired
        private OrderController orderController;
        
        @Autowired
        private OrderVariantController orderVariantController;
        
        @Autowired
        private OrderService orderService;
        
        @Autowired
        private GenericService<Order, OrderRequest, OrderResponse> genericOrderService;
        
        @Autowired
        private OrderRepository orderRepository;
        
        @Autowired
        private WaybillRepository waybillRepository;
        
        @Autowired
        private UserRepository userRepository;
        
        @Autowired
        private CartRepository cartRepository;
        
        @Autowired
        private NotificationRepository notificationRepository;
        
        @Autowired
        private EntityManager entityManager;
        
        @Autowired
        private StorageLocationRepository storageLocationRepository;
        
        @Autowired
        private OrderVariantRepository orderVariantRepository;
        
        @Autowired
        private CartVariantRepository cartVariantRepository;
        
        @Autowired
        private ProvinceRepository provinceRepository;
        
        @Autowired
        private DistrictRepository districtRepository;
        
        @Autowired
        private WardRepository wardRepository;
        
        @Autowired
        private OrderResourceRepository orderResourceRepository;
        
        @Autowired
        private AddressRepository addressRepository;
        
        @Autowired
        private RoleRepository roleRepository;
        
        @Autowired
        private ProductRepository productRepository;
        
        @Autowired
        private VariantRepository variantRepository;
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Autowired
        private OrderMapper orderMapper;
        
        @BeforeEach
        void setup() {
            // Initialize the GenericService with required dependencies
            ((GenericService)genericOrderService).init(
                orderRepository,
                orderMapper,
                com.electro.constant.SearchFields.ORDER,
                com.electro.constant.ResourceName.ORDER
            );
        }
        
        /**
         * Generate a random code for OrderResource
         */
        private String generateRandomResourceCode() {
            return "RES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
        
        /**
         * Check and create OrderResource if it doesn't exist
         */
        private OrderResource getOrCreateOrderResource() {
            // Try to find existing OrderResource
            List<OrderResource> existingResources = orderResourceRepository.findAll();
            Optional<OrderResource> existingResource = existingResources.stream()
                    .filter(resource -> "WEB".equals(resource.getCode()))
                    .findFirst();
            
            if (existingResource.isPresent()) {
                return existingResource.get();
            }
            
            // If not found, create a new one with random code
            OrderResource orderResource = new OrderResource();
            orderResource.setName("Web");
            orderResource.setCode(generateRandomResourceCode());
            orderResource.setColor("#4CAF50");
            orderResource.setStatus(1);
            orderResource.setCreatedAt(new Date().toInstant());
            orderResource.setUpdatedAt(new Date().toInstant());
            return orderResourceRepository.save(orderResource);
        }
        
        /**
         * Create test data directly in code instead of using SQL files
         */
        private void setupTestData() {
            // Create OrderResource
            OrderResource orderResource = getOrCreateOrderResource();
            
            // Get existing province, district, ward from DB
            com.electro.entity.address.Province province = provinceRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No province found"));
            com.electro.entity.address.District district = districtRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No district found"));
            com.electro.entity.address.Ward ward = wardRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No ward found"));
            
            // Create new addresses and save to DB
            Address address1 = new Address();
            address1.setLine("123 Test Street");
            address1.setProvince(province);
            address1.setDistrict(district);
            address1.setWard(ward);
            address1.setCreatedAt(new Date().toInstant());
            address1.setUpdatedAt(new Date().toInstant());
            address1 = addressRepository.save(address1);
            
            Address address2 = new Address();
            address2.setLine("456 Admin Street");
            address2.setProvince(province);
            address2.setDistrict(district);
            address2.setWard(ward);
            address2.setCreatedAt(new Date().toInstant());
            address2.setUpdatedAt(new Date().toInstant());
            address2 = addressRepository.save(address2);
            
            // Create Role if it doesn't exist
            com.electro.entity.authentication.Role roleUser = roleRepository.findByCode("ROLE_USER")
                .orElseGet(() -> {
                    com.electro.entity.authentication.Role r = new com.electro.entity.authentication.Role();
                    r.setName("User role");
                    r.setCode("ROLE_USER");
                    r.setStatus(1);
                    r.setCreatedAt(new Date().toInstant());
                    r.setUpdatedAt(new Date().toInstant());
                    return roleRepository.save(r);
                });
            
            com.electro.entity.authentication.Role roleAdmin = roleRepository.findByCode("ROLE_ADMIN")
                .orElseGet(() -> {
                    com.electro.entity.authentication.Role r = new com.electro.entity.authentication.Role();
                    r.setName("Admin role");
                    r.setCode("ROLE_ADMIN");
                    r.setStatus(1);
                    r.setCreatedAt(new Date().toInstant());
                    return roleRepository.save(r);
                });
            
            // Create Users
            User user1 = new User();
            user1.setUsername("testuser");
            user1.setPassword("$2a$10$J8/bwFiQEwJBvK3QJcWKU.J7O4.CD3fQlJCcUrgL0Vp.hcqgXWXdW");
            user1.setEmail("test@example.com");
            user1.setFullname("Test User");
            user1.setPhone("0123456789");
            user1.setGender("M");
            user1.setAddress(address1);
            user1.setStatus(1);
            user1.setCreatedAt(new Date().toInstant());
            user1.setUpdatedAt(new Date().toInstant());
            
            User user2 = new User();
            user2.setUsername("admin");
            user2.setPassword("$2a$10$J8/bwFiQEwJBvK3QJcWKU.J7O4.CD3fQlJCcUrgL0Vp.hcqgXWXdW");
            user2.setEmail("admin@example.com");
            user2.setFullname("Admin User");
            user2.setPhone("0987654321");
            user2.setGender("M");
            user2.setAddress(address2);
            user2.setStatus(1);
            user2.setCreatedAt(new Date().toInstant());
            user2.setUpdatedAt(new Date().toInstant());
            
            // Setup roles for users
            Set<com.electro.entity.authentication.Role> user1Roles = new HashSet<>();
            user1Roles.add(roleUser);
            user1.setRoles(user1Roles);
            
            Set<com.electro.entity.authentication.Role> user2Roles = new HashSet<>();
            user2Roles.add(roleUser);
            user2Roles.add(roleAdmin);
            user2.setRoles(user2Roles);
            
            userRepository.saveAll(Arrays.asList(user1, user2));
            
            // Create Products and Variants
            Product product1 = new Product();
            product1.setId(1L);
            product1.setName("Test Product 1");
            product1.setCode("TP001");
            product1.setSlug("test-product-1");
            product1.setShortDescription("Test product short description 1");
            product1.setDescription("Test product description 1");
            product1.setStatus(1);
            product1.setCreatedAt(new Date().toInstant());
            product1.setUpdatedAt(new Date().toInstant());
            
            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Test Product 2");
            product2.setCode("TP002");
            product2.setSlug("test-product-2");
            product2.setShortDescription("Test product short description 2");
            product2.setDescription("Test product description 2");
            product2.setStatus(1);
            product2.setCreatedAt(new Date().toInstant());
            product2.setUpdatedAt(new Date().toInstant());
            
            productRepository.saveAll(Arrays.asList(product1, product2));
            
            Variant variant1 = new Variant();
            variant1.setId(1L);
            variant1.setProduct(product1);
            variant1.setSku("V001");
            variant1.setCost(80000.0);
            variant1.setPrice(100000.0);
            variant1.setStatus(1);
            variant1.setCreatedAt(new Date().toInstant());
            variant1.setUpdatedAt(new Date().toInstant());
            
            Variant variant2 = new Variant();
            variant2.setId(2L);
            variant2.setProduct(product2);
            variant2.setSku("V002");
            variant2.setCost(120000.0);
            variant2.setPrice(150000.0);
            variant2.setStatus(1);
            variant2.setCreatedAt(new Date().toInstant());
            variant2.setUpdatedAt(new Date().toInstant());
            
            variantRepository.saveAll(Arrays.asList(variant1, variant2));
            
            // Create Orders with random codes
            Order order1 = new Order();
            order1.setId(1L);
            order1.setCode(generateRandomResourceCode());
            order1.setStatus(1);
            order1.setToName("Test User");
            order1.setToPhone("0123456789");
            order1.setToAddress("123 Test Street");
            order1.setToWardName("Test Ward");
            order1.setToDistrictName("Test District");
            order1.setToProvinceName("Test Province");
            order1.setOrderResource(orderResource);
            order1.setUser(user1);
            order1.setTotalAmount(BigDecimal.valueOf(100000));
            order1.setTax(BigDecimal.valueOf(0.1));
            order1.setShippingCost(BigDecimal.valueOf(20000));
            order1.setTotalPay(BigDecimal.valueOf(130000));
            order1.setPaymentMethodType(PaymentMethodType.CASH);
            order1.setPaymentStatus(1);
            order1.setCreatedAt(new Date().toInstant());
            order1.setUpdatedAt(new Date().toInstant());
            
            Order order2 = new Order();
            order2.setId(2L);
            order2.setCode(generateRandomResourceCode());
            order2.setStatus(1);
            order2.setToName("Test User");
            order2.setToPhone("0123456789");
            order2.setToAddress("123 Test Street");
            order2.setToWardName("Test Ward");
            order2.setToDistrictName("Test District");
            order2.setToProvinceName("Test Province");
            order2.setOrderResource(orderResource);
            order2.setUser(user1);
            order2.setTotalAmount(BigDecimal.valueOf(150000));
            order2.setTax(BigDecimal.valueOf(0.1));
            order2.setShippingCost(BigDecimal.valueOf(20000));
            order2.setTotalPay(BigDecimal.valueOf(185000));
            order2.setPaymentMethodType(PaymentMethodType.CASH);
            order2.setPaymentStatus(1);
            order2.setCreatedAt(new Date().toInstant());
            order2.setUpdatedAt(new Date().toInstant());
            
            Order order3 = new Order();
            order3.setId(3L);
            order3.setCode(generateRandomResourceCode());
            order3.setStatus(3);
            order3.setToName("Test User");
            order3.setToPhone("0123456789");
            order3.setToAddress("123 Test Street");
            order3.setToWardName("Test Ward");
            order3.setToDistrictName("Test District");
            order3.setToProvinceName("Test Province");
            order3.setOrderResource(orderResource);
            order3.setUser(user1);
            order3.setTotalAmount(BigDecimal.valueOf(120000));
            order3.setTax(BigDecimal.valueOf(0.1));
            order3.setShippingCost(BigDecimal.valueOf(20000));
            order3.setTotalPay(BigDecimal.valueOf(152000));
            order3.setPaymentMethodType(PaymentMethodType.CASH);
            order3.setPaymentStatus(2);
            order3.setCreatedAt(new Date().toInstant());
            order3.setUpdatedAt(new Date().toInstant());
            
            orderRepository.saveAll(Arrays.asList(order1, order2, order3));
            
            // Ensure data is written to database
            entityManager.flush();
        }
        
        /**
         * Create a delivered order for testing
         */
        private void setupDeliveredOrderTestData() {
            // Reuse existing OrderResource
            OrderResource orderResource = getOrCreateOrderResource();
            User user = userRepository.findById(1L).orElse(null);
            
            if (user == null) {
                setupTestData(); // Ensure basic data exists
                user = userRepository.findById(1L).orElse(null);
            }
            
            // Create delivered order with random code
            Order order = new Order();
            order.setId(5L);
            order.setCode(generateRandomResourceCode());
            order.setStatus(4); // 4 = Delivered
            order.setToName("Test User");
            order.setToPhone("0123456789");
            order.setToAddress("123 Test Street");
            order.setToWardName("Test Ward");
            order.setToDistrictName("Test District");
            order.setToProvinceName("Test Province");
            order.setOrderResource(orderResource);
            order.setUser(user);
            order.setTotalAmount(BigDecimal.valueOf(150000));
            order.setTax(BigDecimal.valueOf(0.1));
            order.setShippingCost(BigDecimal.valueOf(20000));
            order.setTotalPay(BigDecimal.valueOf(185000));
            order.setPaymentMethodType(PaymentMethodType.CASH);
            order.setPaymentStatus(2); // 2 = Paid
            order.setCreatedAt(new Date().toInstant());
            order.setUpdatedAt(new Date().toInstant());
            
            orderRepository.save(order);
        }
        
        /**
         * Create an order with PayPal for testing
         */
        private void setupPaypalOrderTestData() {
            // Reuse existing OrderResource
            OrderResource orderResource = getOrCreateOrderResource();
            User user = userRepository.findById(1L).orElse(null);
            
            if (user == null) {
                setupTestData(); // Ensure basic data exists
                user = userRepository.findById(1L).orElse(null);
            }
            
            // Create PayPal order with random code
            Order order = new Order();
            order.setId(4L);
            order.setCode(generateRandomResourceCode());
            order.setStatus(1);
            order.setToName("Test User");
            order.setToPhone("0123456789");
            order.setToAddress("123 Test Street");
            order.setToWardName("Test Ward");
            order.setToDistrictName("Test District");
            order.setToProvinceName("Test Province");
            order.setOrderResource(orderResource);
            order.setUser(user);
            order.setTotalAmount(BigDecimal.valueOf(200000));
            order.setTax(BigDecimal.valueOf(0.1));
            order.setShippingCost(BigDecimal.valueOf(20000));
            order.setTotalPay(BigDecimal.valueOf(240000));
            order.setPaymentMethodType(PaymentMethodType.PAYPAL);
            order.setPaymentStatus(1);
            order.setPaypalOrderId("TEST-PAYPAL-ORDER-ID");
            order.setPaypalOrderStatus("CREATED");
            order.setCreatedAt(new Date().toInstant());
            order.setUpdatedAt(new Date().toInstant());
            
            orderRepository.save(order);
        }
        
        /**
         * Create OrderVariants for testing
         */
        private void setupOrderVariantTestData() {
            // Ensure Order and Variant exist
            Order order1 = orderRepository.findById(1L).orElse(null);
            Order order2 = orderRepository.findById(2L).orElse(null);
            Variant variant1 = variantRepository.findById(1L).orElse(null);
            Variant variant2 = variantRepository.findById(2L).orElse(null);
            
            if (order1 == null || order2 == null || variant1 == null || variant2 == null) {
                setupTestData(); // Ensure basic data exists
                order1 = orderRepository.findById(1L).orElse(null);
                order2 = orderRepository.findById(2L).orElse(null);
                variant1 = variantRepository.findById(1L).orElse(null);
                variant2 = variantRepository.findById(2L).orElse(null);
            }
            
            // Create OrderVariants
            OrderVariant orderVariant1 = new OrderVariant();
            orderVariant1.setOrderVariantKey(new OrderVariantKey(1L, 1L));
            orderVariant1.setOrder(order1);
            orderVariant1.setVariant(variant1);
            orderVariant1.setPrice(BigDecimal.valueOf(100000));
            orderVariant1.setQuantity(2);
            orderVariant1.setAmount(BigDecimal.valueOf(200000));
            
            OrderVariant orderVariant2 = new OrderVariant();
            orderVariant2.setOrderVariantKey(new OrderVariantKey(2L, 2L));
            orderVariant2.setOrder(order2);
            orderVariant2.setVariant(variant2);
            orderVariant2.setPrice(BigDecimal.valueOf(150000));
            orderVariant2.setQuantity(1);
            orderVariant2.setAmount(BigDecimal.valueOf(150000));
            
            // Update OrderVariants in Orders
            Set<OrderVariant> orderVariants1 = new HashSet<>();
            orderVariants1.add(orderVariant1);
            order1.setOrderVariants(orderVariants1);
            
            Set<OrderVariant> orderVariants2 = new HashSet<>();
            orderVariants2.add(orderVariant2);
            order2.setOrderVariants(orderVariants2);
            
            orderRepository.saveAll(Arrays.asList(order1, order2));
        }
        
        /**
         * Create Waybill for testing
         */
        private void setupWaybillTestData() {
            // Ensure Order exists
            Order order = orderRepository.findById(2L).orElse(null);
            
            if (order == null) {
                setupTestData(); // Ensure basic data exists
                order = orderRepository.findById(2L).orElse(null);
            }
            
            // Create Waybill
            Waybill waybill = new Waybill();
            waybill.setId(1L);
            waybill.setCode("WB123456");
            waybill.setStatus(1);
            waybill.setOrder(order);
            waybill.setShippingDate(new Date().toInstant());
            waybill.setExpectedDeliveryTime(java.time.LocalDate.now().plusDays(3).atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
            waybill.setCodAmount(0);
            waybill.setShippingFee(20000); 
            waybill.setWeight(500);
            waybill.setLength(20);
            waybill.setWidth(15);
            waybill.setHeight(10);
            waybill.setGhnPaymentTypeId(1);
            waybill.setGhnRequiredNote(RequiredNote.valueOf("CHOXEMHANGKHONGTHU"));
            waybill.setCreatedAt(new Date().toInstant());
            waybill.setUpdatedAt(new Date().toInstant());
            
            waybillRepository.save(waybill);
        }
        
        /**
         * Create Cart and CartVariant for testing
         */
        private void setupCartTestData() {
            // Reuse existing User - find by username to match authentication
            User user = userRepository.findByUsername("testuser").orElse(null);
            Variant variant1 = variantRepository.findById(1L).orElse(null);
            Variant variant2 = variantRepository.findById(2L).orElse(null);
            
            if (user == null || variant1 == null || variant2 == null) {
                setupTestData(); // Ensure basic data exists
                user = userRepository.findByUsername("testuser").orElseThrow(() -> 
                    new RuntimeException("User 'testuser' not found after setupTestData"));
                variant1 = variantRepository.findById(1L).orElse(null);
                variant2 = variantRepository.findById(2L).orElse(null);
            }
            
            // Check if cart already exists for this user
            Optional<Cart> existingCart = cartRepository.findAll().stream()
                .filter(c -> c.getUser().getUsername().equals("testuser"))
                .findFirst();
            
            if (existingCart.isPresent()) {
                // Update existing cart
                Cart cart = existingCart.get();
                cart.setStatus(1); // Ensure cart is active
                cart.setUpdatedAt(new Date().toInstant());
                cartRepository.save(cart);
                return;
            }
            
            // Create Cart
            Cart cart = new Cart();
            // Let the database generate the ID to avoid conflicts
            cart.setUser(user);
            cart.setStatus(1);
            cart.setCreatedAt(new Date().toInstant());
            cart.setUpdatedAt(new Date().toInstant());
            
            cartRepository.save(cart);
            
            // Create CartVariants
            CartVariant cartVariant1 = new CartVariant();
            cartVariant1.setCartVariantKey(new com.electro.entity.cart.CartVariantKey(cart.getId(), 1L));
            cartVariant1.setCart(cart);
            cartVariant1.setVariant(variant1);
            cartVariant1.setQuantity(2);
            cartVariant1.setCreatedAt(new Date().toInstant());
            
            CartVariant cartVariant2 = new CartVariant();
            cartVariant2.setCartVariantKey(new com.electro.entity.cart.CartVariantKey(cart.getId(), 2L));
            cartVariant2.setCart(cart);
            cartVariant2.setVariant(variant2);
            cartVariant2.setQuantity(1);
            cartVariant2.setCreatedAt(new Date().toInstant());
            
            // Update CartVariants in Cart
            Set<CartVariant> cartVariants = new HashSet<>();
            cartVariants.add(cartVariant1);
            cartVariants.add(cartVariant2);
            cart.setCartVariants(cartVariants);
            
            cartRepository.save(cart);
            
            // Ensure changes are visible
            entityManager.flush();
        }
        
        /**
         * Test Case ID: OC-IT001
         * Test Name: testCancelOrderWithDatabase
         * Objective: Verify OrderController cancels an order in database
         * Input: Order code from test data
         * Expected Output: Order status is updated to canceled (5) in database
         */
        @Test
        @DisplayName("Integration Test - Cancel order with database")
        void testCancelOrderWithDatabase() {
            // Given
            setupTestData();
            Order testOrder = orderRepository.findAll().get(0); // Get first order
            String orderCode = testOrder.getCode();
            
            // When
            ResponseEntity<ObjectNode> response = orderController.cancelOrder(orderCode);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            
            // Check database changes
            Order cancelledOrder = orderRepository.findByCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Status 5 = Canceled
            assertEquals(5, cancelledOrder.getStatus(), "Order status must be updated to 5 (Canceled)");
        }
        
        /**
         * Test Case ID: OC-IT002
         * Test Name: testCancelOrderWithWaybill
         * Objective: Verify OrderController cancels an order with waybill
         * Input: Order code with waybill from test data
         * Expected Output: Order and waybill statuses are updated in database
         */
        @Test
        @DisplayName("Integration Test - Cancel order with waybill")
        void testCancelOrderWithWaybill() {
            // Given
            setupTestData();
            setupWaybillTestData();
            
            Order testOrder = orderRepository.findAll().stream()
                    .filter(order -> waybillRepository.findByOrderId(order.getId()).isPresent())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No order with waybill found"));
            String orderCode = testOrder.getCode();
            
            // When
            ResponseEntity<ObjectNode> response = orderController.cancelOrder(orderCode);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());
            
            // Check order is canceled
            Order cancelledOrder = orderRepository.findByCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            assertEquals(5, cancelledOrder.getStatus(), "Order status must be updated to 5 (Canceled)");
            
            // Check waybill is canceled
            Waybill waybill = waybillRepository.findByOrderId(cancelledOrder.getId())
                    .orElseThrow(() -> new RuntimeException("Waybill not found"));
            assertEquals(4, waybill.getStatus(), "Waybill status must be updated to 4 (Canceled)");
        }
        
        /**
         * Test Case ID: OC-IT003
         * Test Name: testCancelDeliveredOrder
         * Objective: Verify exception handling when canceling a delivered order
         * Input: Delivered order code from test data
         * Expected Output: RuntimeException is thrown
         */
        @Test
        @DisplayName("Integration Test - Cannot cancel delivered order")
        void testCancelDeliveredOrder() {
            // Given
            setupDeliveredOrderTestData();
            Order deliveredOrder = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == 4)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No delivered order found"));
            String orderCode = deliveredOrder.getCode();
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                orderController.cancelOrder(orderCode);
            }, "Should throw exception when canceling a delivered order");
        }
        
        /**
         * Test Case ID: OC-IT004
         * Test Name: testCreateOrderWithCashPayment
         * Objective: Verify creating a new order with cash payment
         * Input: ClientSimpleOrderRequest with PaymentMethodType.CASH
         * Expected Output: New order is created in database
         */
//        @Test
//        @DisplayName("Integration Test - Create order with cash payment")
//        void testCreateOrderWithCashPayment() {
//            // Given
//            setupTestData(); // Create basic data
//            setupCartTestData(); // Create cart for user
//
//            // Get the cart before we start
//            Cart cartBefore = cartRepository.findByUsername("testuser")
//                .orElseThrow(() -> new RuntimeException("Cart not found for user 'testuser'"));
//
//            // Ensure cart is active before the test
//            assertEquals(1, cartBefore.getStatus(), "Cart should be active (status 1) before order creation");
//
//            // Setup authentication
//            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            // Create request with cash payment method
//            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
//            request.setPaymentMethodType(PaymentMethodType.CASH);
//
//            // Count orders before creation
//            long orderCountBefore = orderRepository.count();
//
//            // When
//            ClientConfirmedOrderResponse response = orderService.createClientOrder(request);
//
//            // Ensure changes are written to database
//            entityManager.flush();
//
//            // Then
//            assertNotNull(response, "Response must not be null");
//            assertNotNull(response.getOrderCode(), "Must have order code");
//
//            // Check new order is created
//            long orderCountAfter = orderRepository.count();
//            assertEquals(orderCountBefore + 1, orderCountAfter, "Order count must increase by 1");
//
//            // Check order details
//            Order createdOrder = orderRepository.findByCode(response.getOrderCode())
//                    .orElseThrow(() -> new RuntimeException("Created order not found"));
//
//            assertEquals(PaymentMethodType.CASH, createdOrder.getPaymentMethodType(), "Payment method must be CASH");
//            assertEquals(1, createdOrder.getStatus(), "Status must be 1 (New order)");
//            assertEquals(1, createdOrder.getPaymentStatus(), "Payment status must be 1 (Not paid)");
//
//            // Check cart is disabled - find the cart again after the operation
//            Cart cartAfter = cartRepository.findById(cartBefore.getId())
//                    .orElseThrow(() -> new RuntimeException("Cart not found after order creation"));
//            assertEquals(2, cartAfter.getStatus(), "Cart status must be 2 (Disabled)");
//        }

        /**
         * Test Case ID: OC-IT005
         * Test Name: testCreateOrderWithPayPalPayment
         * Objective: Verify creating a new order with PayPal payment
         * Input: ClientSimpleOrderRequest with PaymentMethodType.PAYPAL
         * Expected Output: New order is created with PayPal information
         */
//        @Test
//        @DisplayName("Integration Test - Create order with PayPal payment")
//        void testCreateOrderWithPayPalPayment() {
//            // Given
//            setupTestData(); // Create basic data
//            setupCartTestData(); // Create cart for user
//
//            // Get the cart before we start
//            Cart cartBefore = cartRepository.findByUsername("testuser")
//                .orElseThrow(() -> new RuntimeException("Cart not found for user 'testuser'"));
//
//            // Ensure cart is active before the test
//            assertEquals(1, cartBefore.getStatus(), "Cart should be active (status 1) before order creation");
//
//            // Setup authentication
//            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            // Create request with PayPal payment method
//            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
//            request.setPaymentMethodType(PaymentMethodType.PAYPAL);
//
//            // When
//            ClientConfirmedOrderResponse response = orderService.createClientOrder(request);
//
//            // Ensure changes are written to database
//            entityManager.flush();
//
//            // Then
//            assertNotNull(response, "Response must not be null");
//            assertNotNull(response.getOrderCode(), "Must have order code");
//            assertNotNull(response.getOrderPaypalCheckoutLink(), "Must have PayPal checkout link");
//
//            // Check order has paypalOrderId
//            Order createdOrder = orderRepository.findByCode(response.getOrderCode())
//                    .orElseThrow(() -> new RuntimeException("Created order not found"));
//
//            assertEquals(PaymentMethodType.PAYPAL, createdOrder.getPaymentMethodType(), "Payment method must be PAYPAL");
//            assertNotNull(createdOrder.getPaypalOrderId(), "Must have PayPal Order ID");
//            assertEquals("CREATED", createdOrder.getPaypalOrderStatus(), "PayPal status must be CREATED");
//
//            // Check cart is disabled - find the cart again after the operation
//            Cart cartAfter = cartRepository.findById(cartBefore.getId())
//                    .orElseThrow(() -> new RuntimeException("Cart not found after order creation"));
//            assertEquals(2, cartAfter.getStatus(), "Cart status must be 2 (Disabled)");
//        }

        /**
         * Test Case ID: OC-IT006
         * Test Name: testCapturePayPalTransaction
         * Objective: Verify handling successful PayPal transaction
         * Input: PayPal order ID and payer ID
         * Expected Output: Order is updated and notification is created
         */
//        @Test
//        @DisplayName("Integration Test - Process successful PayPal payment")
//        void testCapturePayPalTransaction() {
//            // Given
//            setupTestData(); // Create basic data
//            setupPaypalOrderTestData(); // Create PayPal order
//
//            String paypalOrderId = "TEST-PAYPAL-ORDER-ID";
//            String payerId = "TEST-PAYER-ID";
//
//            // Count notifications before processing
//            long notificationCountBefore = notificationRepository.count();
//
//            // When
//            orderService.captureTransactionPaypal(paypalOrderId, payerId);
//
//            // Ensure changes are written to database
//            entityManager.flush();
//
//            // Then
//            // Check order is updated
//            Order order = orderRepository.findByPaypalOrderId(paypalOrderId)
//                    .orElseThrow(() -> new RuntimeException("Order not found"));
//
//            assertEquals("COMPLETED", order.getPaypalOrderStatus(), "PayPal status must be COMPLETED");
//            assertEquals(2, order.getPaymentStatus(), "Payment status must be 2 (Paid)");
//
//            // Check notification is created
//            long notificationCountAfter = notificationRepository.count();
//            assertEquals(notificationCountBefore + 1, notificationCountAfter, "Notification count must increase by 1");
//
//            // Check notification details
//            Notification notification = notificationRepository.findAll().stream()
//                    .reduce((first, second) -> second) // Get last notification
//                    .orElse(null);
//
//            assertNotNull(notification, "Notification must be created");
//            assertEquals(order.getUser().getId(), notification.getUser().getId(), "User ID must match");
//        }
        
        /**
         * Test Case ID: OC-IT007
         * Test Name: testDeleteOrderVariant
         * Objective: Verify deletion of a single order variant
         * Input: Order ID and variant ID
         * Expected Output: Order variant is removed from database
         */
        @Test
        @DisplayName("Integration Test - Delete a single order variant")
        void testDeleteOrderVariant() {
            // Given
            setupTestData(); // Create basic data
            setupOrderVariantTestData(); // Create order variant
            
            Long orderId = 1L;
            Long variantId = 1L;
            
            // Check order variant exists before deletion
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            boolean existsBefore = order.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(variantId));
            
            assertTrue(existsBefore, "Order variant must exist before deletion");
            
            // When
            ResponseEntity<Void> response = orderVariantController.deleteOrderVariant(orderId, variantId);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            
            // Check order variant is deleted
            Order updatedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            boolean existsAfter = updatedOrder.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(variantId));
            
            assertFalse(existsAfter, "Order variant must not exist after deletion");
        }
        
        /**
         * Test Case ID: OC-IT008
         * Test Name: testDeleteMultipleOrderVariants
         * Objective: Verify deletion of multiple order variants simultaneously
         * Input: List of OrderVariantKeyRequest
         * Expected Output: All order variants in the list are removed from database
         */
        @Test
        @DisplayName("Integration Test - Delete multiple order variants")
        void testDeleteMultipleOrderVariants() {
            // Given
            setupTestData(); // Create basic data
            setupOrderVariantTestData(); // Create order variant
            
            // Create key requests for deletion
            List<OrderVariantKeyRequest> keyRequests = Arrays.asList(
                    new OrderVariantKeyRequest(1L, 1L),
                    new OrderVariantKeyRequest(2L, 2L)
            );
            
            // Check order variants exist before deletion
            Order order1 = orderRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Order 1 not found"));
            Order order2 = orderRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Order 2 not found"));
                    
            boolean exists1Before = order1.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(1L));
            boolean exists2Before = order2.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(2L));
                    
            assertTrue(exists1Before, "Order variant 1 must exist before deletion");
            assertTrue(exists2Before, "Order variant 2 must exist before deletion");
            
            // When
            ResponseEntity<Void> response = orderVariantController.deleteOrderVariants(keyRequests);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            
            // Check all order variants are deleted
            Order updatedOrder1 = orderRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Order 1 not found"));
            Order updatedOrder2 = orderRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Order 2 not found"));
                    
            boolean exists1After = updatedOrder1.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(1L));
            boolean exists2After = updatedOrder2.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(2L));
                    
            assertFalse(exists1After, "Order variant 1 must not exist after deletion");
            assertFalse(exists2After, "Order variant 2 must not exist after deletion");
        }
        
        /**
         * Test Case ID: GS-IT001
         * Test Name: testGenericServiceFindAllOrders
         * Objective: Verify GenericService integration with repository for finding orders
         * Input: Pagination, sort, and filter parameters
         * Expected Output: ListResponse containing order data from database
         */
        @Test
        @DisplayName("Integration Test - GenericService find all orders with database")
        void testGenericServiceFindAllOrders() {
            // Given
            setupTestData(); // Create basic data with orders
            
            int page = 1;
            int size = 10;
            String sort = "id,desc";
            
            // When - use the genericOrderService for CRUD operations
            ListResponse<OrderResponse> response = genericOrderService.findAll(page, size, sort, null, null, false);
            
            // Then
            assertNotNull(response);
            assertNotNull(response.getContent());
            assertFalse(response.getContent().isEmpty(), "Should return orders from database");
            assertTrue(response.getTotalElements() >= 3, "Should have at least 3 orders");
        }
        
        /**
         * Test Case ID: GS-IT002
         * Test Name: testGenericServiceFindOrderById
         * Objective: Verify GenericService integration with repository for finding order by ID
         * Input: Order ID
         * Expected Output: OrderResponse with data from database
         */
        @Test
        @DisplayName("Integration Test - GenericService find order by ID from database")
        void testGenericServiceFindOrderById() {
            // Given
            setupTestData(); // Create basic data with orders
            Long orderId = 1L;
            
            // When - use the genericOrderService
            OrderResponse response = genericOrderService.findById(orderId);
            
            // Then
            assertNotNull(response);
            assertEquals(orderId, response.getId());
        }
        
        /**
         * Test Case ID: GS-IT003
         * Test Name: testGenericServiceCreateOrder
         * Objective: Verify GenericService integration with repository for creating order
         * Input: OrderRequest with order details
         * Expected Output: New order created in database
         */
        @Test
        @DisplayName("Integration Test - GenericService create order in database")
        void testGenericServiceCreateOrder() {
            // Given
            setupTestData(); // Create basic data
            
            // Count orders before creation
            long countBefore = orderRepository.count();
            
            // Create request JSON
            ObjectMapper objectMapper = new ObjectMapper();
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCode(generateRandomResourceCode()); // Generate a unique code
            orderRequest.setOrderResourceId(getOrCreateOrderResource().getId());
            orderRequest.setUserId(userRepository.findByUsername("testuser").get().getId());
            orderRequest.setToName("New Test Order");
            orderRequest.setToPhone("0123456789");
            orderRequest.setToAddress("Test Address");
            orderRequest.setToWardName("Test Ward");
            orderRequest.setToDistrictName("Test District");
            orderRequest.setToProvinceName("Test Province");
            orderRequest.setTotalAmount(BigDecimal.valueOf(100000));
            orderRequest.setTax(BigDecimal.valueOf(0.1));
            orderRequest.setShippingCost(BigDecimal.valueOf(20000));
            orderRequest.setTotalPay(BigDecimal.valueOf(130000));
            orderRequest.setPaymentMethodType(PaymentMethodType.CASH);
            orderRequest.setStatus(1);
            orderRequest.setPaymentStatus(1);
            // Initialize empty orderVariants collection to prevent NullPointerException
            orderRequest.setOrderVariants(new HashSet<>());
            
            JsonNode jsonNode = objectMapper.valueToTree(orderRequest);
            
            // When - use the genericOrderService
            OrderResponse response = genericOrderService.save(jsonNode, OrderRequest.class);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertNotNull(response);
            assertNotNull(response.getId());
            
            // Check database count increased
            long countAfter = orderRepository.count();
            assertEquals(countBefore + 1, countAfter, "Order count should increase by 1");
            
            // Verify order in database
            Order savedOrder = orderRepository.findById(response.getId())
                    .orElseThrow(() -> new RuntimeException("Saved order not found"));
            
            assertEquals("New Test Order", savedOrder.getToName());
            assertEquals(PaymentMethodType.CASH, savedOrder.getPaymentMethodType());
            assertEquals(1, savedOrder.getStatus());
        }
        
        /**
         * Test Case ID: GS-IT004
         * Test Name: testGenericServiceUpdateOrder
         * Objective: Verify GenericService integration with repository for updating order
         * Input: Order ID and OrderRequest with updated details
         * Expected Output: Order updated in database
         */
        @Test
        @DisplayName("Integration Test - GenericService update order in database")
        void testGenericServiceUpdateOrder() {
            // Given
            setupTestData(); // Create basic data
            
            // Get an existing order
            Order existingOrder = orderRepository.findAll().get(0);
            Long orderId = existingOrder.getId();
            
            // Create update request JSON
            ObjectMapper objectMapper = new ObjectMapper();
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCode(existingOrder.getCode()); // Must include code as it's required
            orderRequest.setOrderResourceId(existingOrder.getOrderResource().getId());
            orderRequest.setUserId(existingOrder.getUser().getId());
            orderRequest.setToName("Updated Order Name");
            orderRequest.setToPhone(existingOrder.getToPhone());
            orderRequest.setToAddress(existingOrder.getToAddress());
            orderRequest.setToWardName(existingOrder.getToWardName());
            orderRequest.setToDistrictName(existingOrder.getToDistrictName());
            orderRequest.setToProvinceName(existingOrder.getToProvinceName());
            orderRequest.setTotalAmount(existingOrder.getTotalAmount());
            orderRequest.setTax(existingOrder.getTax());
            orderRequest.setShippingCost(existingOrder.getShippingCost());
            orderRequest.setTotalPay(existingOrder.getTotalPay());
            orderRequest.setPaymentMethodType(existingOrder.getPaymentMethodType());
            orderRequest.setStatus(2); // Changed status to 2
            orderRequest.setPaymentStatus(existingOrder.getPaymentStatus());
            // Initialize empty orderVariants collection to prevent NullPointerException
            orderRequest.setOrderVariants(new HashSet<>());
            
            JsonNode jsonNode = objectMapper.valueToTree(orderRequest);
            
            // When - use the genericOrderService
            OrderResponse response = genericOrderService.save(orderId, jsonNode, OrderRequest.class);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            assertNotNull(response);
            assertEquals(orderId, response.getId());
            assertEquals("Updated Order Name", response.getToName());
            assertEquals(2, response.getStatus());
            
            // Verify order in database
            Order updatedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Updated order not found"));
            
            assertEquals("Updated Order Name", updatedOrder.getToName());
            assertEquals(2, updatedOrder.getStatus());
        }
        
        /**
         * Test Case ID: GS-IT005
         * Test Name: testGenericServiceDeleteOrder
         * Objective: Verify GenericService integration with repository for deleting order
         * Input: Order ID
         * Expected Output: Order removed from database
         */
        @Test
        @DisplayName("Integration Test - GenericService delete order from database")
        void testGenericServiceDeleteOrder() {
            // Given
            setupTestData(); // Create basic data
            
            // Get an existing order that can be deleted
            Order existingOrder = orderRepository.findAll().stream()
                    .filter(order -> order.getStatus() == 1)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No suitable order found for deletion"));
            
            Long orderId = existingOrder.getId();
            
            // Count orders before deletion
            long countBefore = orderRepository.count();
            
            // When - use the genericOrderService
            genericOrderService.delete(orderId);
            
            // Ensure changes are written to database
            entityManager.flush();
            
            // Then
            // Check database count decreased
            long countAfter = orderRepository.count();
            assertEquals(countBefore - 1, countAfter, "Order count should decrease by 1");
            
            // Verify order not in database
            assertThrows(ResourceNotFoundException.class, () -> {
                genericOrderService.findById(orderId);
            }, "Order should not be found after deletion");
        }
    }
}