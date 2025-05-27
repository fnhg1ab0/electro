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

/**
 * Whitebox Testing cho OrderController
 * - Unit test: Kiểm tra các hàm không tương tác với database
 * - Integration test: Kiểm tra các hàm tương tác với database
 */
public class OrderControllerTest {
    /**
     * Unit Tests sử dụng Mockito để giả lập các dependency
     * Kiểm tra logic của controller và service mà không cần tương tác với database thật
     */
    @Nested
    @DisplayName("Unit Tests - Kiểm tra logic không tương tác với database")
    @ExtendWith(MockitoExtension.class)
    class UnitTests {
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
        void setup() {
            objectMapper = new ObjectMapper();
        }

        /**
         * Test Case ID: OC-UT001
         * Test Name: testCancelOrderController
         * Objective: Kiểm tra OrderController hủy đơn hàng
         * Input: Mã đơn hàng
         * Expected Output: HTTP 200 OK và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của cancelOrder
         */
        @Test
        @DisplayName("Unit Test - Hủy đơn hàng")
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
         * Objective: Kiểm tra OrderVariantController xóa một order variant
         * Input: orderId=1L, variantId=2L
         * Expected Output: HTTP 204 NO CONTENT và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của deleteOrderVariant
         */
        @Test
        @DisplayName("Unit Test - Xóa đơn order variant")
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
         * Objective: Kiểm tra OrderVariantController xóa nhiều order variant
         * Input: Danh sách OrderVariantKeyRequest
         * Expected Output: HTTP 204 NO CONTENT và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của deleteOrderVariants
         */
        @Test
        @DisplayName("Unit Test - Xóa nhiều order variant")
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

            // Kiểm tra danh sách không null và có đúng số lượng phần tử
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
         * Objective: Kiểm tra OrderVariantController xóa với danh sách rỗng
         * Input: Danh sách OrderVariantKeyRequest rỗng
         * Expected Output: HTTP 204 NO CONTENT và service được gọi với danh sách rỗng
         * Path Coverage: Đường đi biên với danh sách trống
         */
        @Test
        @DisplayName("Unit Test - Xóa với danh sách order variant rỗng")
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

            // Kiểm tra danh sách đầu vào là rỗng
            List<OrderVariantKey> capturedKeys = keysCaptor.getValue();
            assertNotNull(capturedKeys);
            assertTrue(capturedKeys.isEmpty(), "Danh sách phải rỗng");
        }

        /**
         * Test Case ID: OC-UT005
         * Test Name: testGenericGetAllOrdersController
         * Objective: Kiểm tra GenericController lấy danh sách đơn hàng
         * Input: Các tham số phân trang, sắp xếp và lọc
         * Expected Output: HTTP 200 OK và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của getAllResources
         */
        @Test
        @DisplayName("Unit Test - GenericController lấy danh sách đơn hàng")
        void testGenericGetAllOrdersController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            int page = 0;
            int size = 10;
            String sort = "id,desc";
            String filter = "status==PENDING";
            String search = "customer";
            boolean all = false;
            
            List<OrderResponse> orders = new ArrayList<>();
            Page<Order> orderPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
            ListResponse<OrderResponse> expectedResponse = new ListResponse<>(orders, orderPage);
            
            when(genericOrderService.findAll(page, size, sort, filter, search, all)).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<ListResponse<OrderResponse>> response = genericController.getAllResources(page, size, sort, filter, search, all);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(genericOrderService).findAll(page, size, sort, filter, search, all);
        }
        
        /**
         * Test Case ID: OC-UT006
         * Test Name: testGenericGetOrderByIdController
         * Objective: Kiểm tra GenericController lấy đơn hàng theo ID
         * Input: ID đơn hàng
         * Expected Output: HTTP 200 OK và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của getResource
         */
        @Test
        @DisplayName("Unit Test - GenericController lấy đơn hàng theo ID")
        void testGenericGetOrderByIdController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            Long orderId = 1L;
            OrderResponse expectedResponse = new OrderResponse();
            when(genericOrderService.findById(orderId)).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<OrderResponse> response = genericController.getResource(orderId);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(genericOrderService).findById(orderId);
        }
        
        /**
         * Test Case ID: OC-UT007
         * Test Name: testGenericCreateOrderController
         * Objective: Kiểm tra GenericController tạo đơn hàng mới
         * Input: OrderRequest
         * Expected Output: HTTP 201 CREATED và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của createResource
         */
        @Test
        @DisplayName("Unit Test - GenericController tạo đơn hàng mới")
        void testGenericCreateOrderController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            JsonNode request = objectMapper.createObjectNode();
            OrderResponse expectedResponse = new OrderResponse();
            when(genericOrderService.save(any(JsonNode.class), eq(OrderRequest.class))).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<OrderResponse> response = genericController.createResource(request);
            
            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(genericOrderService).save(any(JsonNode.class), eq(OrderRequest.class));
        }
        
        /**
         * Test Case ID: OC-UT008
         * Test Name: testGenericUpdateOrderController
         * Objective: Kiểm tra GenericController cập nhật đơn hàng
         * Input: ID đơn hàng và OrderRequest
         * Expected Output: HTTP 200 OK và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của updateResource
         */
        @Test
        @DisplayName("Unit Test - GenericController cập nhật đơn hàng")
        void testGenericUpdateOrderController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            Long orderId = 1L;
            JsonNode request = objectMapper.createObjectNode();
            OrderResponse expectedResponse = new OrderResponse();
            when(genericOrderService.save(eq(orderId), any(JsonNode.class), eq(OrderRequest.class))).thenReturn(expectedResponse);
            
            // When
            ResponseEntity<OrderResponse> response = genericController.updateResource(orderId, request);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(genericOrderService).save(eq(orderId), any(JsonNode.class), eq(OrderRequest.class));
        }
        
        /**
         * Test Case ID: OC-UT009
         * Test Name: testGenericDeleteOrderController
         * Objective: Kiểm tra GenericController xóa đơn hàng
         * Input: ID đơn hàng
         * Expected Output: HTTP 204 NO CONTENT và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của deleteResource
         */
        @Test
        @DisplayName("Unit Test - GenericController xóa đơn hàng")
        void testGenericDeleteOrderController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            Long orderId = 1L;
            doNothing().when(genericOrderService).delete(orderId);
            
            // When
            ResponseEntity<Void> response = genericController.deleteResource(orderId);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(genericOrderService).delete(orderId);
        }
        
        /**
         * Test Case ID: OC-UT010
         * Test Name: testGenericDeleteMultipleOrdersController
         * Objective: Kiểm tra GenericController xóa nhiều đơn hàng
         * Input: Danh sách ID đơn hàng
         * Expected Output: HTTP 204 NO CONTENT và service được gọi đúng cách
         * Path Coverage: Đường đi thành công của deleteResources
         */
        @Test
        @DisplayName("Unit Test - GenericController xóa nhiều đơn hàng")
        void testGenericDeleteMultipleOrdersController() {
            // Given
            GenericController<OrderRequest, OrderResponse> genericController = new GenericController<>();
            genericController.setCrudService(genericOrderService);
            genericController.setRequestType(OrderRequest.class);
            
            List<Long> orderIds = Arrays.asList(1L, 2L, 3L);
            doNothing().when(genericOrderService).delete(orderIds);
            
            // When
            ResponseEntity<Void> response = genericController.deleteResources(orderIds);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(genericOrderService).delete(orderIds);
        }
    }

    /**
     * Integration Tests tương tác với database thật
     * Kiểm tra tương tác với database và xác nhận dữ liệu được lưu/đọc/cập nhật/xóa thực sự
     */
    @Nested
    @DisplayName("Integration Tests - Kiểm tra tương tác với database")
    @SpringBootTest
    @ActiveProfiles("test")
    @Sql(scripts = { "classpath:schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Transactional
    class DatabaseTests {

        @Autowired
        private OrderController orderController;

        @Autowired
        private OrderVariantController orderVariantController;

        @Autowired
        private OrderService orderService;

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
        private com.electro.repository.address.ProvinceRepository provinceRepository;
        
        @Autowired
        private com.electro.repository.address.DistrictRepository districtRepository;
        
        @Autowired
        private com.electro.repository.address.WardRepository wardRepository;

        @Autowired
        private com.electro.repository.inventory.ProductInventoryLimitRepository productInventoryLimitRepository;

        @Autowired
        private com.electro.repository.inventory.VariantInventoryLimitRepository variantInventoryLimitRepository;

        @Autowired
        private com.electro.repository.inventory.CountVariantRepository countVariantRepository;

        @Autowired
        private com.electro.repository.inventory.DocketVariantRepository docketVariantRepository;

        @Autowired
        private com.electro.repository.inventory.PurchaseOrderVariantRepository purchaseOrderVariantRepository;


        // Thêm các repository khác cần thiết cho việc tạo dữ liệu test
        @Autowired
        private com.electro.repository.order.OrderResourceRepository orderResourceRepository;

        @Autowired
        private com.electro.repository.address.AddressRepository addressRepository;

        @Autowired
        private com.electro.repository.authentication.RoleRepository roleRepository;

        @Autowired
        private com.electro.repository.product.ProductRepository productRepository;

        @Autowired
        private com.electro.repository.product.VariantRepository variantRepository;
        
        // Thêm GenericController cho Order
        @Autowired
        private com.electro.controller.GenericController<com.electro.dto.order.OrderRequest, com.electro.dto.order.OrderResponse> orderGenericController;

        /**
         * Tạo mã ngẫu nhiên cho OrderResource
         */
        private String generateRandomResourceCode() {
            return "RES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }


        /**
         * Kiểm tra và tạo OrderResource nếu chưa tồn tại
         */
        private OrderResource getOrCreateOrderResource() {
            // Thử tìm OrderResource có sẵn
            List<OrderResource> existingResources = orderResourceRepository.findAll();
            Optional<OrderResource> existingResource = existingResources.stream()
                    .filter resource -> "WEB".equals(resource.getCode())
                    .findFirst();

            if (existingResource.isPresent()) {
                return existingResource.get();
            }


            // Nếu không có, tạo mới với mã ngẫu nhiên
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
         * Tạo dữ liệu test trực tiếp trong code thay vì sử dụng file SQL
         */
        private void setupTestData() {
            // Tạo OrderResource
            OrderResource orderResource = getOrCreateOrderResource();

            // Lấy province, district, ward có sẵn trong DB
            com.electro.entity.address.Province province = provinceRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No province found"));
            com.electro.entity.address.District district = districtRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No district found"));
            com.electro.entity.address.Ward ward = wardRepository.findAll().stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No ward found"));

            // Tạo mới address1, address2 và lưu vào DB
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


            // Tạo Role chỉ khi chưa tồn tại
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


            // Tạo User
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

            // Thiết lập roles cho users
            Set<com.electro.entity.authentication.Role> user1Roles = new HashSet<>();
            user1Roles.add(roleUser);
            user1.setRoles(user1Roles);

            Set<com.electro.entity.authentication.Role> user2Roles = new HashSet<>();
            user2Roles.add(roleUser);
            user2Roles.add(roleAdmin);
            user2.setRoles(user2Roles);

            userRepository.saveAll(Arrays.asList(user1, user2));


            // Tạo Products và Variants
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


            // Tạo Orders với mã ngẫu nhiên
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
            // Đảm bảo dữ liệu đã được ghi vào database
            entityManager.flush();
        }


        /**
         * Tạo đơn hàng đã giao cho test
         */
        private void setupDeliveredOrderTestData() {
            // Sử dụng lại OrderResource đã tạo
            OrderResource orderResource = getOrCreateOrderResource();
            User user = userRepository.findById(1L).orElse(null);

            if (user == null) {
                setupTestData(); // Đảm bảo dữ liệu cơ bản đã được tạo
                user = userRepository.findById(1L).orElse(null);
            }

            // Tạo đơn hàng đã giao với mã ngẫu nhiên
            Order order = new Order();
            order.setId(5L);
            order.setCode(generateRandomResourceCode());
            order.setStatus(4); // 4 = Đã giao
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
            order.setPaymentStatus(2); // 2 = Đã thanh toán
            order.setCreatedAt(new java.util.Date().toInstant());
            order.setUpdatedAt(new java.util.Date().toInstant());

            orderRepository.save(order);
        }


        /**
         * Tạo đơn hàng với PayPal cho test
         */
        private void setupPaypalOrderTestData() {
            // Sử dụng lại OrderResource đã tạo
            OrderResource orderResource = getOrCreateOrderResource();
            User user = userRepository.findById(1L).orElse(null);

            if (user == null) {
                setupTestData(); // Đảm bảo dữ liệu cơ bản đã được tạo
                user = userRepository.findById(1L).orElse(null);
            }

            // Tạo đơn hàng với PayPal và mã ngẫu nhiên
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
         * Tạo các OrderVariant cho test
         */
        private void setupOrderVariantTestData() {
            // Đảm bảo Order và Variant đã tồn tại
            Order order1 = orderRepository.findById(1L).orElse(null);
            Order order2 = orderRepository.findById(2L).orElse(null);
            Variant variant1 = variantRepository.findById(1L).orElse(null);
            Variant variant2 = variantRepository.findById(2L).orElse(null);

            if (order1 == null || order2 == null || variant1 == null || variant2 == null) {
                setupTestData(); // Đảm bảo dữ liệu cơ bản đã được tạo
                order1 = orderRepository.findById(1L).orElse(null);
                order2 = orderRepository.findById(2L).orElse(null);
                variant1 = variantRepository.findById(1L).orElse(null);
                variant2 = variantRepository.findById(2L).orElse(null);
            }

            // Tạo OrderVariant
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

            // Cập nhật OrderVariants trong Order
            Set<OrderVariant> orderVariants1 = new HashSet<>();
            orderVariants1.add(orderVariant1);
            order1.setOrderVariants(orderVariants1);

            Set<OrderVariant> orderVariants2 = new HashSet<>();
            orderVariants2.add(orderVariant2);
            order2.setOrderVariants(orderVariants2);

            orderRepository.saveAll(Arrays.asList(order1, order2));
        }


        /**
         * Tạo Waybill cho test
         */
        private void setupWaybillTestData() {
            // Đảm bảo Order đã tồn tại
            Order order = orderRepository.findById(2L).orElse(null);

            if (order == null) {
                setupTestData(); // Đảm bảo dữ liệu cơ bản đã được tạo
                order = orderRepository.findById(2L).orElse(null);
            }

            // Tạo Waybill
            Waybill waybill = new Waybill();
            waybill.setId(1L);
            waybill.setCode("WB123456");
            waybill.setStatus(1);
            waybill.setOrder(order);
            waybill.setShippingDate(new Date().toInstant());
            waybill.setExpectedDeliveryTime(java.time.LocalDate.now().plusDays(3).atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
            waybill.setCodAmount(0);
            waybill.setShippingFee(20000); // Changed from BigDecimal to Integer
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
         * Tạo Cart và CartVariant cho test
         */
        private void setupCartTestData() {
            // Sử dụng lại User đã tạo ở trên
            User user = userRepository.findById(1L).orElse(null);
            Variant variant1 = variantRepository.findById(1L).orElse(null);
            Variant variant2 = variantRepository.findById(2L).orElse(null);

            if (user == null || variant1 == null || variant2 == null) {
                setupTestData(); // Đảm bảo dữ liệu cơ bản đã được tạo
                user = userRepository.findById(1L).orElse(null);
                variant1 = variantRepository.findById(1L).orElse(null);
                variant2 = variantRepository.findById(2L).orElse(null);
            }

            // Tạo Cart
            Cart cart = new Cart();
            cart.setId(1L);
            cart.setUser(user);
            cart.setStatus(1);
            cart.setCreatedAt(new Date().toInstant());
            cart.setUpdatedAt(new Date().toInstant());

            cartRepository.save(cart);

            // Tạo CartVariant
            CartVariant cartVariant1 = new CartVariant();
            cartVariant1.setCartVariantKey(new com.electro.entity.cart.CartVariantKey(1L, 1L));
            cartVariant1.setCart(cart);
            cartVariant1.setVariant(variant1);
            cartVariant1.setQuantity(2);
            cartVariant1.setCreatedAt(new Date().toInstant());

            CartVariant cartVariant2 = new CartVariant();
            cartVariant2.setCartVariantKey(new com.electro.entity.cart.CartVariantKey(1L, 2L));
            cartVariant2.setCart(cart);
            cartVariant2.setVariant(variant2);
            cartVariant2.setQuantity(1);
            cartVariant2.setCreatedAt(new Date().toInstant());

            // Cập nhật CartVariants trong Cart
            Set<CartVariant> cartVariants = new HashSet<>();
            cartVariants.add(cartVariant1);
            cartVariants.add(cartVariant2);
            cart.setCartVariants(cartVariants);

            cartRepository.save(cart);
        }

        /**
         * Test Case ID: OC-IT001
         * Test Name: testCancelOrderWithDatabase
         * Objective: Kiểm tra OrderController hủy đơn hàng trong database
         * Input: Mã đơn hàng từ dữ liệu test
         * Expected Output: Trạng thái đơn hàng được cập nhật thành hủy (5) trong database
         * Path Coverage: Đường đi thành công và xác minh thay đổi database
         */
        @Test
        @DisplayName("Integration Test - Hủy đơn hàng với database")
        void testCancelOrderWithDatabase() {
            // Given
            setupTestData();
            Order testOrder = orderRepository.findAll().get(0); // Lấy đơn hàng đầu tiên
            String orderCode = testOrder.getCode();

            // When
            ResponseEntity<ObjectNode> response = orderController.cancelOrder(orderCode);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());

            // Kiểm tra thay đổi trong database
            Order cancelledOrder = orderRepository.findByCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Trạng thái 5 = Đã hủy
            assertEquals(5, cancelledOrder.getStatus(), "Trạng thái đơn hàng phải được cập nhật thành 5 (Đã hủy)");
        }

        /**
         * Test Case ID: OC-IT002
         * Test Name: testCancelOrderWithWaybillDatabase
         * Objective: Kiểm tra OrderController hủy đơn hàng có vận đơn
         * Input: Mã đơn hàng có vận đơn từ dữ liệu test
         * Expected Output: Trạng thái đơn hàng và vận đơn được cập nhật trong database
         * Path Coverage: Đường đi có vận đơn và xác minh thay đổi database
         */
        @Test
        @DisplayName("Integration Test - Hủy đơn hàng có vận đơn với database")
        void testCancelOrderWithWaybillUpdate() {
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

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertTrue(response.getStatusCode().is2xxSuccessful());

            // Kiểm tra đơn hàng đã bị hủy
            Order cancelledOrder = orderRepository.findByCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            assertEquals(5, cancelledOrder.getStatus(), "Trạng thái đơn hàng phải được cập nhật thành 5 (Đã hủy)");

            // Kiểm tra vận đơn đã bị hủy
            Waybill waybill = waybillRepository.findByOrderId(cancelledOrder.getId())
                    .orElseThrow(() -> new RuntimeException("Waybill not found"));
            assertEquals(4, waybill.getStatus(), "Trạng thái vận đơn phải được cập nhật thành 4 (Đã hủy)");
        }

        /**
         * Test Case ID: OC-IT003
         * Test Name: testCancelOrderAlreadyDelivered
         * Objective: Kiểm tra xử lý ngoại lệ khi hủy đơn hàng đã giao
         * Input: Mã đơn hàng đã giao từ dữ liệu test
         * Expected Output: RuntimeException được ném ra
         * Path Coverage: Đường đi ngoại lệ khi đơn hàng không thể hủy
         */
        @Test
        @DisplayName("Integration Test - Hủy đơn hàng đã giao")
        void testCancelOrderAlreadyDelivered() {
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
            }, "Phải ném ngoại lệ khi hủy đơn hàng đã giao");
        }

        /**
         * Test Case ID: OC-IT004
         * Test Name: testCreateClientOrderCashPayment
         * Objective: Kiểm tra tạo đơn hàng mới với thanh toán tiền mặt
         * Input: ClientSimpleOrderRequest với PaymentMethodType.CASH
         * Expected Output: Đơn hàng mới được tạo trong database
         * Path Coverage: Đường đi tạo đơn hàng với thanh toán tiền mặt
         */
        @Test
        @DisplayName("Integration Test - Tạo đơn hàng với thanh toán tiền mặt")
        void testCreateClientOrderCashPayment() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            setupCartTestData(); // Tạo giỏ hàng cho user

            // Thiết lập authentication
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Tạo request với phương thức thanh toán tiền mặt
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.CASH);

            // Đếm số đơn hàng trước khi tạo
            long orderCountBefore = orderRepository.count();

            // When
            ClientConfirmedOrderResponse response = orderService.createClientOrder(request);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertNotNull(response, "Phải trả về response không null");
            assertNotNull(response.getOrderCode(), "Phải có mã đơn hàng");

            // Kiểm tra đơn hàng mới được tạo
            long orderCountAfter = orderRepository.count();
            assertEquals(orderCountBefore + 1, orderCountAfter, "Số lượng đơn hàng phải tăng thêm 1");

            // Kiểm tra chi tiết đơn hàng
            Order createdOrder = orderRepository.findByCode(response.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("Created order not found"));

            assertEquals(PaymentMethodType.CASH, createdOrder.getPaymentMethodType(), "Phương thức thanh toán phải là CASH");
            assertEquals(1, createdOrder.getStatus(), "Trạng thái phải là 1 (Đơn hàng mới)");
            assertEquals(1, createdOrder.getPaymentStatus(), "Trạng thái thanh toán phải là 1 (Chưa thanh toán)");

            // Kiểm tra giỏ hàng đã bị vô hiệu hóa
            Cart cart = cartRepository.findByUsername("testuser")
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            assertEquals(2, cart.getStatus(), "Trạng thái giỏ hàng phải là 2 (Đã vô hiệu hóa)");
        }

        /**
         * Test Case ID: OC-IT005
         * Test Name: testCreateClientOrderPayPalPayment
         * Objective: Kiểm tra tạo đơn hàng mới với thanh toán PayPal
         * Input: ClientSimpleOrderRequest với PaymentMethodType.PAYPAL
         * Expected Output: Đơn hàng mới được tạo với thông tin PayPal
         * Path Coverage: Đường đi tạo đơn hàng với thanh toán PayPal
         */
        @Test
        @DisplayName("Integration Test - Tạo đơn hàng với thanh toán PayPal")
        void testCreateClientOrderPayPalPayment() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            setupCartTestData(); // Tạo giỏ hàng cho user

            // Thiết lập authentication
            Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Tạo request với phương thức thanh toán PayPal
            ClientSimpleOrderRequest request = new ClientSimpleOrderRequest();
            request.setPaymentMethodType(PaymentMethodType.PAYPAL);

            // When
            ClientConfirmedOrderResponse response = orderService.createClientOrder(request);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertNotNull(response, "Phải trả về response không null");
            assertNotNull(response.getOrderCode(), "Phải có mã đơn hàng");
            assertNotNull(response.getOrderPaypalCheckoutLink(), "Phải có link thanh toán PayPal");

            // Kiểm tra đơn hàng có paypalOrderId
            Order createdOrder = orderRepository.findByCode(response.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("Created order not found"));

            assertEquals(PaymentMethodType.PAYPAL, createdOrder.getPaymentMethodType(), "Phương thức thanh toán phải là PAYPAL");
            assertNotNull(createdOrder.getPaypalOrderId(), "Phải có PayPal Order ID");
            assertEquals("CREATED", createdOrder.getPaypalOrderStatus(), "Trạng thái PayPal phải là CREATED");
        }

        /**
         * Test Case ID: OC-IT006
         * Test Name: testCaptureTransactionPaypal
         * Objective: Kiểm tra xử lý giao dịch PayPal thành công
         * Input: PayPal order ID và payer ID
         * Expected Output: Đơn hàng được cập nhật và thông báo được tạo
         * Path Coverage: Đường đi xử lý thanh toán PayPal thành công
         */
        @Test
        @DisplayName("Integration Test - Xử lý thanh toán PayPal thành công")
        void testCaptureTransactionPaypal() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            setupPaypalOrderTestData(); // Tạo đơn hàng PayPal

            String paypalOrderId = "TEST-PAYPAL-ORDER-ID";
            String payerId = "TEST-PAYER-ID";

            // Đếm số thông báo trước khi xử lý
            long notificationCountBefore = notificationRepository.count();

            // When
            orderService.captureTransactionPaypal(paypalOrderId, payerId);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            // Kiểm tra đơn hàng đã được cập nhật
            Order order = orderRepository.findByPaypalOrderId(paypalOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            assertEquals("COMPLETED", order.getPaypalOrderStatus(), "Trạng thái PayPal phải là COMPLETED");
            assertEquals(2, order.getPaymentStatus(), "Trạng thái thanh toán phải là 2 (Đã thanh toán)");

            // Kiểm tra thông báo đã được tạo
            long notificationCountAfter = notificationRepository.count();
            assertEquals(notificationCountBefore + 1, notificationCountAfter, "Số lượng thông báo phải tăng thêm 1");

            // Kiểm tra chi tiết thông báo
            Notification notification = notificationRepository.findAll().stream()
                    .reduce((first, second) -> second) // Lấy thông báo cuối cùng
                    .orElse(null);

            assertNotNull(notification, "Phải có thông báo được tạo");
            assertEquals(order.getUser().getId(), notification.getUser().getId(), "User ID phải khớp");
        }

        /**
         * Test Case ID: OC-IT007
         * Test Name: testDeleteOrderVariant
         * Objective: Kiểm tra xóa một order variant
         * Input: Order ID và variant ID
         * Expected Output: Order variant bị xóa khỏi database
         * Path Coverage: Đường đi xóa một order variant
         */
        @Test
        @DisplayName("Integration Test - Xóa một order variant")
        void testDeleteOrderVariant() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            setupOrderVariantTestData(); // Tạo order variant

            Long orderId = 1L;
            Long variantId = 1L;

            // Kiểm tra order variant tồn tại trước khi xóa
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            boolean existsBefore = order.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(variantId));

            assertTrue(existsBefore, "Order variant phải tồn tại trước khi xóa");

            // When
            ResponseEntity<Void> response = orderVariantController.deleteOrderVariant(orderId, variantId);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

            // Kiểm tra order variant đã bị xóa
            Order updatedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            boolean existsAfter = updatedOrder.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(variantId));

            assertFalse(existsAfter, "Order variant không được tồn tại sau khi xóa");
        }

        /**
         * Test Case ID: OC-IT008
         * Test Name: testDeleteMultipleOrderVariants
         * Objective: Kiểm tra xóa nhiều order variants cùng lúc
         * Input: Danh sách các OrderVariantKeyRequest
         * Expected Output: Tất cả order variants trong danh sách bị xóa khỏi database
         * Path Coverage: Đường đi xóa nhiều order variants
         */
        @Test
        @DisplayName("Integration Test - Xóa nhiều order variants")
        void testDeleteMultipleOrderVariants() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            setupOrderVariantTestData(); // Tạo order variant
            
            // Tạo các key request để xóa
            List<OrderVariantKeyRequest> keyRequests = Arrays.asList(
                    new OrderVariantKeyRequest(1L, 1L),
                    new OrderVariantKeyRequest(2L, 2L)
            );
            
            // Kiểm tra các order variant tồn tại trước khi xóa
            Order order1 = orderRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Order 1 not found"));
            Order order2 = orderRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Order 2 not found"));
                    
            boolean exists1Before = order1.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(1L));
            boolean exists2Before = order2.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(2L));
                    
            assertTrue(exists1Before, "Order variant 1 phải tồn tại trước khi xóa");
            assertTrue(exists2Before, "Order variant 2 phải tồn tại trước khi xóa");

            // When
            ResponseEntity<Void> response = orderVariantController.deleteOrderVariants(keyRequests);

            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();

            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

            // Kiểm tra tất cả order variant đã bị xóa
            Order updatedOrder1 = orderRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Order 1 not found"));
            Order updatedOrder2 = orderRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("Order 2 not found"));
                    
            boolean exists1After = updatedOrder1.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(1L));
            boolean exists2After = updatedOrder2.getOrderVariants().stream()
                    .anyMatch(ov -> ov.getVariant().getId().equals(2L));
                    
            assertFalse(exists1After, "Order variant 1 không được tồn tại sau khi xóa");
            assertFalse(exists2After, "Order variant 2 không được tồn tại sau khi xóa");
        }

        /**
         * Test Case ID: OGSC-IT001
         * Test Name: testGenericGetAllOrders
         * Objective: Kiểm tra GenericController lấy danh sách đơn hàng từ database
         * Input: Các tham số phân trang, sắp xếp, và lọc
         * Expected Output: Danh sách đơn hàng được trả về từ database
         * Path Coverage: Đường đi thành công của getAllResources với database
         */
        @Test
        @DisplayName("Integration Test - GenericController lấy danh sách đơn hàng từ database")
        void testGenericGetAllOrders() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản

            int page = 0;
            int size = 10;
            String sort = "id,asc";
            String filter = null;
            String search = null;
            boolean all = false;
            
            // When
            ResponseEntity<ListResponse<com.electro.dto.order.OrderResponse>> response = 
                orderGenericController.getAllResources(page, size, sort, filter, search, all);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getData());
            assertFalse(response.getBody().getData().isEmpty(), "Danh sách đơn hàng không được rỗng");
            
            // Kiểm tra số lượng đơn hàng
            long orderCount = orderRepository.count();
            assertEquals(orderCount, response.getBody().getPagination().getTotalElements(), 
                "Tổng số đơn hàng phải bằng số lượng trong database");
        }

        /**
         * Test Case ID: OGSC-IT002
         * Test Name: testGenericGetOrderById
         * Objective: Kiểm tra GenericController lấy đơn hàng theo ID từ database
         * Input: ID đơn hàng
         * Expected Output: Đơn hàng được trả về từ database
         * Path Coverage: Đường đi thành công của getResource với database
         */
        @Test
        @DisplayName("Integration Test - GenericController lấy đơn hàng theo ID từ database")
        void testGenericGetOrderById() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            // Lấy một Order ID có sẵn
            Order existingOrder = orderRepository.findAll().get(0);
            Long orderId = existingOrder.getId();
            
            // When
            ResponseEntity<com.electro.dto.order.OrderResponse> response = 
                orderGenericController.getResource(orderId);
                
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(orderId, response.getBody().getId(), "ID đơn hàng phải khớp");
        }

        /**
         * Test Case ID: OGSC-IT003
         * Test Name: testGenericGetOrderByIdNotFound
         * Objective: Kiểm tra GenericController xử lý khi không tìm thấy đơn hàng
         * Input: ID đơn hàng không tồn tại
         * Expected Output: ResourceNotFoundException được ném ra
         * Path Coverage: Đường đi ngoại lệ của getResource với database
         */
        @Test
        @DisplayName("Integration Test - GenericController không tìm thấy đơn hàng")
        void testGenericGetOrderByIdNotFound() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            Long nonExistentOrderId = 9999L;
            
            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                orderGenericController.getResource(nonExistentOrderId);
            }, "Phải ném ngoại lệ ResourceNotFoundException khi không tìm thấy đơn hàng");
        }

        /**
         * Test Case ID: OGSC-IT004
         * Test Name: testGenericCreateOrder
         * Objective: Kiểm tra GenericController tạo đơn hàng mới trong database
         * Input: OrderRequest với thông tin đơn hàng mới
         * Expected Output: Đơn hàng mới được tạo trong database
         * Path Coverage: Đường đi thành công của createResource với database
         */
        @Test
        @DisplayName("Integration Test - GenericController tạo đơn hàng mới trong database")
        void testGenericCreateOrder() throws Exception {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            // Tạo OrderRequest dưới dạng JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode requestNode = objectMapper.createObjectNode();
            
            // Thông tin cơ bản cho đơn hàng mới
            requestNode.put("status", 1);
            requestNode.put("toName", "Test User Create");
            requestNode.put("toPhone", "0123456789");
            requestNode.put("toAddress", "123 Test Street");
            requestNode.put("toWardName", "Test Ward");
            requestNode.put("toDistrictName", "Test District");
            requestNode.put("toProvinceName", "Test Province");
            
            // ID của OrderResource hiện có
            OrderResource existingResource = orderResourceRepository.findAll().get(0);
            requestNode.put("orderResourceId", existingResource.getId());
            
            // ID của User hiện có
            User existingUser = userRepository.findAll().get(0);
            requestNode.put("userId", existingUser.getId());
            
            // Thông tin tài chính
            requestNode.put("totalAmount", 100000);
            requestNode.put("tax", 0.1);
            requestNode.put("shippingCost", 20000);
            requestNode.put("totalPay", 130000);
            requestNode.put("paymentMethodType", "CASH");
            requestNode.put("paymentStatus", 1);
            
            // Đếm số đơn hàng trước khi tạo
            long orderCountBefore = orderRepository.count();
            
            // When
            ResponseEntity<com.electro.dto.order.OrderResponse> response = 
                orderGenericController.createResource(requestNode);
                
            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();
                
            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId(), "ID đơn hàng mới phải không null");
            
            // Đếm số đơn hàng sau khi tạo
            long orderCountAfter = orderRepository.count();
            assertEquals(orderCountBefore + 1, orderCountAfter, "Số lượng đơn hàng phải tăng thêm 1");
            
            // Kiểm tra đơn hàng đã được lưu vào database
            Order createdOrder = orderRepository.findById(response.getBody().getId())
                .orElseThrow(() -> new RuntimeException("Created order not found"));
                
            assertEquals("Test User Create", createdOrder.getToName(), "Tên người nhận phải khớp");
            assertEquals("CASH", createdOrder.getPaymentMethodType().toString(), "Phương thức thanh toán phải khớp");
        }

        /**
         * Test Case ID: OGSC-IT005
         * Test Name: testGenericUpdateOrder
         * Objective: Kiểm tra GenericController cập nhật đơn hàng trong database
         * Input: ID đơn hàng và OrderRequest với thông tin cập nhật
         * Expected Output: Đơn hàng được cập nhật trong database
         * Path Coverage: Đường đi thành công của updateResource với database
         */
        @Test
        @DisplayName("Integration Test - GenericController cập nhật đơn hàng trong database")
        void testGenericUpdateOrder() throws Exception {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            // Lấy một đơn hàng để cập nhật
            Order existingOrder = orderRepository.findAll().get(0);
            Long orderId = existingOrder.getId();
            
            // Tạo OrderRequest dưới dạng JsonNode với thông tin cập nhật
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode requestNode = objectMapper.createObjectNode();
            
            // Chỉ cập nhật một số trường
            requestNode.put("toName", "Updated User Name");
            requestNode.put("toPhone", "0987654321");
            requestNode.put("status", 2); // Cập nhật trạng thái
            
            // When
            ResponseEntity<com.electro.dto.order.OrderResponse> response = 
                orderGenericController.updateResource(orderId, requestNode);
                
            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();
                
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            // Kiểm tra đơn hàng đã được cập nhật trong database
            Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Updated order not found"));
                
            assertEquals("Updated User Name", updatedOrder.getToName(), "Tên người nhận phải được cập nhật");
            assertEquals("0987654321", updatedOrder.getToPhone(), "Số điện thoại người nhận phải được cập nhật");
            assertEquals(2, updatedOrder.getStatus(), "Trạng thái phải được cập nhật");
        }

        /**
         * Test Case ID: OGSC-IT006
         * Test Name: testGenericDeleteOrder
         * Objective: Kiểm tra GenericController xóa đơn hàng khỏi database
         * Input: ID đơn hàng
         * Expected Output: Đơn hàng bị xóa khỏi database
         * Path Coverage: Đường đi thành công của deleteResource với database
         */
        @Test
        @DisplayName("Integration Test - GenericController xóa đơn hàng khỏi database")
        void testGenericDeleteOrder() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            // Lấy một đơn hàng để xóa (không có liên kết với bảng khác)
            Order orderToDelete = orderRepository.findAll().get(0);
            Long orderId = orderToDelete.getId();
            
            // Đếm số đơn hàng trước khi xóa
            long orderCountBefore = orderRepository.count();
            
            // When
            ResponseEntity<Void> response = orderGenericController.deleteResource(orderId);
            
            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            
            // Đếm số đơn hàng sau khi xóa
            long orderCountAfter = orderRepository.count();
            assertEquals(orderCountBefore - 1, orderCountAfter, "Số lượng đơn hàng phải giảm đi 1");
            
            // Kiểm tra đơn hàng không còn tồn tại
            assertFalse(orderRepository.existsById(orderId), "Đơn hàng không được tồn tại sau khi xóa");
        }

        /**
         * Test Case ID: OGSC-IT007
         * Test Name: testGenericDeleteMultipleOrders
         * Objective: Kiểm tra GenericController xóa nhiều đơn hàng khỏi database
         * Input: Danh sách ID đơn hàng
         * Expected Output: Tất cả đơn hàng trong danh sách bị xóa khỏi database
         * Path Coverage: Đường đi thành công của deleteResources với database
         */
        @Test
        @DisplayName("Integration Test - GenericController xóa nhiều đơn hàng khỏi database")
        void testGenericDeleteMultipleOrders() {
            // Given
            setupTestData(); // Tạo dữ liệu cơ bản
            
            // Lấy một số đơn hàng để xóa (không có liên kết với bảng khác)
            List<Order> ordersToDelete = orderRepository.findAll().subList(0, 2);
            List<Long> orderIds = ordersToDelete.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
            
            // Đếm số đơn hàng trước khi xóa
            long orderCountBefore = orderRepository.count();
            
            // When
            ResponseEntity<Void> response = orderGenericController.deleteResources(orderIds);
            
            // Đảm bảo thay đổi được ghi vào database
            entityManager.flush();
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            
            // Đếm số đơn hàng sau khi xóa
            long orderCountAfter = orderRepository.count();
            assertEquals(orderCountBefore - orderIds.size(), orderCountAfter, 
                "Số lượng đơn hàng phải giảm đi bằng số lượng ID đã xóa");
            
            // Kiểm tra tất cả đơn hàng không còn tồn tại
            for (Long id : orderIds) {
                assertFalse(orderRepository.existsById(id), 
                    "Đơn hàng với ID " + id + " không được tồn tại sau khi xóa");
            }
        }
    }
}