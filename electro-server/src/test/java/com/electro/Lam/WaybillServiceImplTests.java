package com.electro.Lam;

import com.electro.dto.general.NotificationResponse;
import com.electro.dto.waybill.GhnUpdateOrderRequest;
import com.electro.entity.authentication.User;
import com.electro.entity.general.NotificationType;
import com.electro.mapper.general.NotificationMapper;
import com.electro.entity.general.Notification;
import com.electro.dto.ListResponse;
import com.electro.dto.waybill.GhnCallbackOrderRequest;
import com.electro.dto.waybill.WaybillRequest;
import com.electro.dto.waybill.WaybillResponse;
import com.electro.entity.cashbook.PaymentMethodType;
import com.electro.entity.order.Order;
import com.electro.entity.waybill.RequiredNote;
import com.electro.entity.waybill.Waybill;
import com.electro.entity.waybill.WaybillLog;
import com.electro.exception.ResourceNotFoundException;
import com.electro.mapper.waybill.WaybillMapper;
import com.electro.repository.general.NotificationRepository;
import com.electro.repository.order.OrderRepository;
import com.electro.repository.waybill.WaybillLogRepository;
import com.electro.repository.waybill.WaybillRepository;
import com.electro.service.general.NotificationService;
import com.electro.service.waybill.WaybillServiceImpl;
import com.electro.utils.RewardUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class WaybillServiceImplTests {
    // Inject values from application properties
    @Value("${electro.app.shipping.ghnToken}")
    private String ghnToken;
    @Value("${electro.app.shipping.ghnShopId}")
    private String ghnShopId;
    @Value("${electro.app.shipping.ghnApiPath}")
    private String ghnApiPath;

    // Mock dependencies
    @Mock
    private WaybillRepository waybillRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WaybillLogRepository waybillLogRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RewardUtils rewardUtils;

    @Mock
    private WaybillMapper waybillMapper;

    @Mock
    private NotificationMapper notificationMapper;

    // Inject the service to be tested
    @InjectMocks
    private WaybillServiceImpl waybillService;

    // Setup method to initialize mocks and set fields
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(waybillService, "ghnShopId", "123");
    }


    @Test
    public void testFindAll() {
        Waybill waybill = new Waybill();
        WaybillResponse waybillResponse = new WaybillResponse();

        // Mock repository and mapper methods
        Page<Waybill> waybillPage = new PageImpl<>(List.of(waybill));
        when(waybillRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(waybillPage);
        when(waybillMapper.entityToResponse(anyList())).thenReturn(List.of(waybillResponse));

        // Call the service method
        ListResponse<WaybillResponse> result = waybillService.findAll(1, 10, "id,asc", null, null, false);

        // Print test data and results
        System.out.println("Test Data: waybill = " + waybill);
        System.out.println("Test Result: Content Size = " + result.getContent().size());

        // Verify the results
        assertEquals(1, result.getContent().size());
        verify(waybillRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
        verify(waybillMapper, times(1)).entityToResponse(anyList());
    }


    @Test
    public void testFindById() {
        Waybill waybill = new Waybill();
        WaybillResponse waybillResponse = new WaybillResponse();

        // Mock repository and mapper methods
        when(waybillRepository.findById(1L)).thenReturn(Optional.of(waybill));
        when(waybillMapper.entityToResponse(any(Waybill.class))).thenReturn(waybillResponse);

        // Call the service method
        WaybillResponse result = waybillService.findById(1L);

        // Print test data and results
        System.out.println("Test Data: waybill = " + waybill);
        System.out.println("Test Result: waybillResponse = " + result);

        // Verify the results
        assertEquals(waybillResponse, result);
        verify(waybillRepository, times(1)).findById(1L);
        verify(waybillMapper, times(1)).entityToResponse(any(Waybill.class));
    }


    @Test
    public void testFindById_NotFound() {
        // Mock repository method
        when(waybillRepository.findById(1L)).thenReturn(Optional.empty());

        // Print test data
        System.out.println("Test Data: waybillId = 1");

        // Verify that the service method throws an exception
        assertThrows(ResourceNotFoundException.class, () -> {
            waybillService.findById(1L);
        });

        verify(waybillRepository, times(1)).findById(1L);
        verify(waybillMapper, times(0)).entityToResponse(any(Waybill.class));
    }

        @Test
    public void testSave() {
        // Set up the absolute URL for ghnApiPath
        ReflectionTestUtils.setField(waybillService, "ghnApiPath", "https://dev-online-gateway.ghn.vn/shiip/public-api/v2");

        WaybillRequest waybillRequest = new WaybillRequest();
        waybillRequest.setOrderId(1L); // Set the orderId to a valid value
        waybillRequest.setNote("Test Note");
        waybillRequest.setGhnRequiredNote(RequiredNote.CHOTHUHANG);
        waybillRequest.setWeight(1);
        waybillRequest.setLength(1);
        waybillRequest.setWidth(1);
        waybillRequest.setHeight(1);
        waybillRequest.setShippingDate(Instant.now());

        Waybill waybill = new Waybill();
        WaybillResponse waybillResponse = new WaybillResponse();
        Order order = new Order();
        order.setId(1L);
        order.setStatus(1); // Set the order status to a valid value
        order.setToName("Test Name");
        order.setToPhone("123456789");
        order.setToAddress("Test Address");
        order.setToWardName("Test Ward");
        order.setToDistrictName("Test District");
        order.setToProvinceName("Test Province");
        order.setPaymentMethodType(PaymentMethodType.CASH);
        order.setTotalPay(BigDecimal.valueOf(1000));

        // Mock repository and mapper methods
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(waybillMapper.requestToEntity(waybillRequest)).thenReturn(waybill);
        when(waybillRepository.save(waybill)).thenReturn(waybill);
        when(waybillMapper.entityToResponse(waybill)).thenReturn(waybillResponse);

        // Call the service method
        WaybillResponse result = waybillService.save(waybillRequest);

        // Print test data and results
        System.out.println("Test Data: waybillRequest = " + waybillRequest);
        System.out.println("Test Result: waybillResponse = " + result);

        // Verify the results
        assertEquals(waybillResponse, result);
        verify(orderRepository, times(1)).findById(1L);
        verify(waybillRepository, times(1)).save(waybill);
        verify(waybillMapper, times(1)).entityToResponse(waybill);
    }

       @Test
    public void testSave_Update() {
        // Set up the absolute URL for ghnApiPath
        ReflectionTestUtils.setField(waybillService, "ghnApiPath", "https://dev-online-gateway.ghn.vn/shiip/public-api/v2");

        Long id = 1L;
        WaybillRequest waybillRequest = new WaybillRequest();
        Waybill waybill = new Waybill();
        WaybillResponse waybillResponse = new WaybillResponse();

        // Mock repository and mapper methods
        when(waybillRepository.findById(id)).thenReturn(Optional.of(waybill));
        when(waybillMapper.partialUpdate(waybill, waybillRequest)).thenReturn(waybill);
        when(waybillRepository.save(waybill)).thenReturn(waybill);
        when(waybillMapper.entityToResponse(waybill)).thenReturn(waybillResponse);

        // Call the service method
        WaybillResponse result = waybillService.save(id, waybillRequest);

        // Print test data and results
        System.out.println("Test Data: waybillRequest = " + waybillRequest);
        System.out.println("Test Result: waybillResponse = " + result);

        // Verify the results
        assertEquals(waybillResponse, result);
        verify(waybillRepository, times(1)).findById(id);
        verify(waybillRepository, times(1)).save(waybill);
        verify(waybillMapper, times(1)).entityToResponse(waybill);
    }


    @Test
    public void testSave_Update_NotFound() {
        Long id = 1L;
        WaybillRequest waybillRequest = new WaybillRequest();

        // Mock repository method
        when(waybillRepository.findById(id)).thenReturn(Optional.empty());

        // Print test data
        System.out.println("Test Data: waybillId = " + id);

        // Verify that the service method throws an exception
        assertThrows(ResourceNotFoundException.class, () -> {
            waybillService.save(id, waybillRequest);
        });

        verify(waybillRepository, times(1)).findById(id);
        verify(waybillRepository, times(0)).save(any(Waybill.class));
    }


    @Test
    public void testDelete() {
        // Call the service method
        waybillService.delete(1L);

        // Print test data
        System.out.println("Test Data: waybillId = 1");

        // Verify the results
        verify(waybillRepository, times(1)).deleteById(1L);
    }


    @Test
    public void testDeleteMultiple() {
        // Call the service method
        waybillService.delete(List.of(1L, 2L));

        // Print test data
        System.out.println("Test Data: waybillIds = [1, 2]");

        // Verify the results
        verify(waybillRepository, times(1)).deleteAllById(List.of(1L, 2L));
    }


    @Test
    public void testCallbackStatusWaybillFromGHN() {
        // Set up the absolute URL for ghnApiPath
        ReflectionTestUtils.setField(waybillService, "ghnApiPath", "https://dev-online-gateway.ghn.vn/shiip/public-api/v2");

        GhnCallbackOrderRequest ghnCallbackOrderRequest = new GhnCallbackOrderRequest();
        ghnCallbackOrderRequest.setShopID(123);
        ghnCallbackOrderRequest.setOrderCode("GHN123");
        ghnCallbackOrderRequest.setStatus("ready_to_pick");

        Waybill waybill = new Waybill();
        waybill.setCode("GHN123");
        Order order = new Order();
        order.setId(1L);
        order.setStatus(1);
        order.setToName("Test Name");
        order.setToPhone("123456789");
        order.setToAddress("Test Address");
        order.setToWardName("Test Ward");
        order.setToDistrictName("Test District");
        order.setToProvinceName("Test Province");
        order.setPaymentMethodType(PaymentMethodType.CASH);
        order.setTotalPay(BigDecimal.valueOf(1000));
        waybill.setOrder(order);

        // Mock repository and mapper methods
        when(waybillRepository.findByCode("GHN123")).thenReturn(Optional.of(waybill));
        when(notificationMapper.entityToResponse(any(Notification.class))).thenReturn(new NotificationResponse());

        // Call the service method
        waybillService.callbackStatusWaybillFromGHN(ghnCallbackOrderRequest);

        // Print test data and results
        System.out.println("Test Data: ghnCallbackOrderRequest = " + ghnCallbackOrderRequest);
        System.out.println("Test Result: waybill = " + waybill);

        // Verify the results
        verify(waybillRepository, times(1)).findByCode("GHN123");
        verify(waybillLogRepository, times(1)).save(any(WaybillLog.class));
        verify(waybillRepository, times(1)).save(any(Waybill.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).pushNotification(anyString(), any());
    }

        @Test
    public void testCallbackStatusWaybillFromGHN_WaybillNotFound() {
        GhnCallbackOrderRequest ghnCallbackOrderRequest = new GhnCallbackOrderRequest();
        ghnCallbackOrderRequest.setShopID(123);
        ghnCallbackOrderRequest.setOrderCode("GHN123");
        ghnCallbackOrderRequest.setStatus("ready_to_pick");

        // Mock repository method
        when(waybillRepository.findByCode("GHN123")).thenReturn(Optional.empty());

        // Print test data
        System.out.println("Test Data: ghnCallbackOrderRequest = " + ghnCallbackOrderRequest);

        // Verify that the service method throws an exception
        assertThrows(ResourceNotFoundException.class, () -> {
            waybillService.callbackStatusWaybillFromGHN(ghnCallbackOrderRequest);
        });

        verify(waybillRepository, times(1)).findByCode("GHN123");
        verify(waybillLogRepository, times(0)).save(any(WaybillLog.class));
        verify(waybillRepository, times(0)).save(any(Waybill.class));
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(notificationService, times(0)).pushNotification(anyString(), any());
    }
}