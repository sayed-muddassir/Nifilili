package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessQueryControllerTest {

    @Mock
    private BusinessQueryService businessQueryService;

    @InjectMocks
    private BusinessQueryController businessQueryController;

    @Test
    void getAllBusinesses_WhenServiceReturnsPage_ShouldReturnSamePage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<BusinessResponse> responsePage = new PageImpl<>(List.of(BusinessResponse.builder().build()));
        when(businessQueryService.getAllBusinesses(pageRequest)).thenReturn(responsePage);

        assertSame(responsePage, businessQueryController.getAllBusinesses(pageRequest));
    }

    @Test
    void getBusinessById_WhenServiceReturnsResponse_ShouldReturnSameResponse() {
        BusinessResponse response = BusinessResponse.builder().build();
        when(businessQueryService.getBusinessById(100L)).thenReturn(response);

        assertSame(response, businessQueryController.getBusinessById(100L));
    }
}
