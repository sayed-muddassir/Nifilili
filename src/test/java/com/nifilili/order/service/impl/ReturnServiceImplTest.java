package com.nifilili.order.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.core.enums.order.OrderItemStatus;
import com.nifilili.core.enums.order.ReturnStatus;
import com.nifilili.core.exception.InvalidOrderStateException;
import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.OrderItemEntity;
import com.nifilili.order.domain.ReturnRequestEntity;
import com.nifilili.order.dto.request.AddressDto;
import com.nifilili.order.dto.request.CreateReturnRequest;
import com.nifilili.order.dto.response.ReturnResponse;
import com.nifilili.order.mapper.ReturnMapper;
import com.nifilili.order.repository.OrderItemRepository;
import com.nifilili.order.repository.OrderStatusHistoryRepository;
import com.nifilili.order.repository.ReturnRequestRepository;
import com.nifilili.order.service.BusinessConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnServiceImplTest {

    private static final Long USER_ID = 42L;
    private static final Long ITEM_ID = 100L;
    private static final Long BUSINESS_ID = 5L;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @Mock
    private BusinessConfigService businessConfigService;

    @Mock
    private ReturnMapper returnMapper;

    @InjectMocks
    private ReturnServiceImpl returnService;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(USER_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    // --- createReturn ---

    @Test
    void createReturn_WhenValidRequest_ShouldCreateReturnRequest() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);
        item.setUpdatedAt(LocalDateTime.now().minusDays(2)); // Within 7-day window

        CreateReturnRequest request = buildReturnRequest();
        ReturnResponse expectedResponse = ReturnResponse.builder()
                .returnRequestId(1L).rmaNumber("RMA-12345").status("REQUESTED").build();

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(businessConfigService.getConfigMap(Set.of(BUSINESS_ID)))
                .thenReturn(Map.of(BUSINESS_ID, Map.of()));
        when(returnRequestRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.empty());
        when(returnRequestRepository.save(any(ReturnRequestEntity.class)))
                .thenAnswer(invocation -> TestEntityIdUtil.withId(invocation.getArgument(0), 1L));
        when(returnMapper.toResponse(any(ReturnRequestEntity.class))).thenReturn(expectedResponse);

        ReturnResponse response = returnService.createReturn(ITEM_ID, request);

        assertThat(response.getStatus()).isEqualTo("REQUESTED");
        assertThat(response.getRmaNumber()).isNotNull();

        ArgumentCaptor<ReturnRequestEntity> captor = ArgumentCaptor.forClass(ReturnRequestEntity.class);
        verify(returnRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ReturnStatus.REQUESTED);
        assertThat(captor.getValue().getRmaNumber()).startsWith("RMA-");
    }

    @Test
    void createReturn_WhenItemNotDelivered_ShouldThrowInvalidOrderState() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.SHIPPED);

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> returnService.createReturn(ITEM_ID, buildReturnRequest()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("delivered");
    }

    @Test
    void createReturn_WhenItemNotFound_ShouldThrowResourceNotFound() {
        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> returnService.createReturn(ITEM_ID, buildReturnRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createReturn_WhenReturnWindowExpired_ShouldThrowInvalidOrderState() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);
        item.setUpdatedAt(LocalDateTime.now().minusDays(10)); // Past 7-day window

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(businessConfigService.getConfigMap(Set.of(BUSINESS_ID)))
                .thenReturn(Map.of(BUSINESS_ID, Map.of()));

        assertThatThrownBy(() -> returnService.createReturn(ITEM_ID, buildReturnRequest()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void createReturn_WhenCustomReturnWindow_ShouldUseBusinessConfig() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);
        item.setUpdatedAt(LocalDateTime.now().minusDays(10)); // Past default 7 days but within 15

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(businessConfigService.getConfigMap(Set.of(BUSINESS_ID)))
                .thenReturn(Map.of(BUSINESS_ID, Map.of("return_window_days", "15")));
        when(returnRequestRepository.findByOrderItemId(ITEM_ID)).thenReturn(Optional.empty());
        when(returnRequestRepository.save(any(ReturnRequestEntity.class)))
                .thenAnswer(invocation -> TestEntityIdUtil.withId(invocation.getArgument(0), 1L));
        when(returnMapper.toResponse(any(ReturnRequestEntity.class)))
                .thenReturn(ReturnResponse.builder().status("REQUESTED").build());

        ReturnResponse response = returnService.createReturn(ITEM_ID, buildReturnRequest());

        assertThat(response.getStatus()).isEqualTo("REQUESTED");
    }

    @Test
    void createReturn_WhenReturnAlreadyExists_ShouldThrowInvalidOrderState() {
        OrderItemEntity item = buildItem(ITEM_ID, OrderItemStatus.DELIVERED);
        item.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(orderItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(businessConfigService.getConfigMap(Set.of(BUSINESS_ID)))
                .thenReturn(Map.of(BUSINESS_ID, Map.of()));
        when(returnRequestRepository.findByOrderItemId(ITEM_ID))
                .thenReturn(Optional.of(new ReturnRequestEntity()));

        assertThatThrownBy(() -> returnService.createReturn(ITEM_ID, buildReturnRequest()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already exists");
    }

    // --- helpers ---

    private OrderItemEntity buildItem(Long id, OrderItemStatus status) {
        OrderItemEntity item = OrderItemEntity.builder()
                .orderId(10L)
                .businessId(BUSINESS_ID)
                .status(status)
                .build();
        return TestEntityIdUtil.withId(item, id);
    }

    private CreateReturnRequest buildReturnRequest() {
        AddressDto address = new AddressDto();
        address.setMunicipalityId(1L);
        address.setWardNumber(5);
        address.setToleName("Kathmandu");
        address.setAddressField1("123 Main St");

        CreateReturnRequest request = new CreateReturnRequest();
        request.setReason("Product damaged");
        request.setDetails("Arrived with scratches");
        request.setPickupAddress(address);
        return request;
    }
}
