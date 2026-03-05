package com.nifilili.order.controller.owner;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.order.dto.request.UpdateBusinessConfigRequest;
import com.nifilili.order.dto.response.BusinessConfigResponse;
import com.nifilili.order.service.BusinessConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessConfigControllerTest {

    @Mock
    private BusinessConfigService businessConfigService;

    @InjectMocks
    private BusinessConfigController businessConfigController;

    @BeforeEach
    void setUp() {
        SecurityContextTestUtil.setAuthenticatedUser(10L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void allConfigEndpoints_WhenServiceReturnsResponses_ShouldDelegateAndReturnOk() {
        List<BusinessConfigResponse> configs = List.of(BusinessConfigResponse.builder().build());
        when(businessConfigService.getConfigs(10L)).thenReturn(configs);

        UpdateBusinessConfigRequest request = new UpdateBusinessConfigRequest();
        BusinessConfigResponse upsertResp = BusinessConfigResponse.builder().build();
        when(businessConfigService.upsertConfig(10L, request)).thenReturn(upsertResp);

        ResponseEntity<List<BusinessConfigResponse>> getResult = businessConfigController.getConfigs();
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(configs, getResult.getBody());

        ResponseEntity<BusinessConfigResponse> upsertResult = businessConfigController.upsertConfig(request);
        assertEquals(HttpStatus.OK, upsertResult.getStatusCode());
        assertSame(upsertResp, upsertResult.getBody());
    }
}
