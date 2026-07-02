package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.service.BusinessQueryService;
import com.nifilili.core.enums.business.BusinessStatus;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessQueryController Unit Tests")
class BusinessQueryControllerTest {

    @Mock
    private BusinessQueryService businessQueryService;

    @InjectMocks
    private BusinessQueryController businessQueryController;

    private static final Long VERTICAL_ID = 1000L;
    private static final Long MUNICIPALITY_ID = 1000L;

    @Test
    void getAllBusinesses_WhenServiceReturnsPage_ShouldReturnSamePage() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<BusinessResponse> responsePage = new PageImpl<>(List.of(BusinessResponse.builder().build()));
        when(businessQueryService.getAllBusinessesByStatus(pageRequest, BusinessStatus.PUBLISHED))
                .thenReturn(responsePage);

        assertSame(responsePage, businessQueryController.getAllBusinesses(pageRequest));
    }

    @Test
    void getBusinessById_WhenServiceReturnsResponse_ShouldReturnSameResponse() {
        BusinessResponse response = BusinessResponse.builder().build();
        when(businessQueryService.getBusinessById(100L)).thenReturn(response);

        assertSame(response, businessQueryController.getBusinessById(100L));
    }

    @Test
    @DisplayName("Should_ReturnOkWithTopBusinesses_WhenValidParametersProvided")
    void testGetTopByVerticalAndMunicipality_HappyPath() {
        List<BusinessResponse> mockBusinesses = createMockBusinessResponses();

        when(businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5))
            .thenReturn(mockBusinesses);

        ResponseEntity<List<BusinessResponse>> response = businessQueryController.getTopByVerticalAndMunicipality(
            VERTICAL_ID, MUNICIPALITY_ID, 5
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Business 1", response.getBody().get(0).getName());
        assertEquals("Business 2", response.getBody().get(1).getName());

        verify(businessQueryService, times(1)).getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5);
    }

//    @Test
//    @DisplayName("Should_ReturnNotFound_WhenNoBusinessesFound")
//    void testGetTopByVerticalAndMunicipality_NoBusinessesFound() {
//        when(businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5))
//            .thenReturn(new ArrayList<>());
//
//        ResponseEntity<List<BusinessResponse>> response = businessQueryController.getTopByVerticalAndMunicipality(
//            VERTICAL_ID, MUNICIPALITY_ID, 5
//        );
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//        assertTrue(response.getBody().isEmpty());
//
//        verify(businessQueryService, times(1)).getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5);
//    }

    @Test
    @DisplayName("Should_ReturnBadRequest_WhenIllegalArgumentExceptionThrown")
    void testGetTopByVerticalAndMunicipality_IllegalArgumentException() {
        when(businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5))
            .thenThrow(new IllegalArgumentException("limit must be between 1 and 100"));

        ResponseEntity<List<BusinessResponse>> response = businessQueryController.getTopByVerticalAndMunicipality(
            VERTICAL_ID, MUNICIPALITY_ID, 5
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(businessQueryService, times(1)).getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5);
    }

    @Test
    @DisplayName("Should_ReturnInternalServerError_WhenUnexpectedExceptionThrown")
    void testGetTopByVerticalAndMunicipality_UnexpectedException() {
        when(businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5))
            .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<List<BusinessResponse>> response = businessQueryController.getTopByVerticalAndMunicipality(
            VERTICAL_ID, MUNICIPALITY_ID, 5
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(businessQueryService, times(1)).getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 5);
    }

    @Test
    @DisplayName("Should_ReturnOkWithCustomLimitCount_WhenTopParameterProvided")
    void testGetTopByVerticalAndMunicipality_CustomLimit() {
        List<BusinessResponse> mockBusinesses = List.of(
            createMockBusinessResponse(1L, "Business 1", new BigDecimal("4.8"))
        );

        when(businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 10))
            .thenReturn(mockBusinesses);

        ResponseEntity<List<BusinessResponse>> response = businessQueryController.getTopByVerticalAndMunicipality(
            VERTICAL_ID, MUNICIPALITY_ID, 10
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(businessQueryService, times(1)).getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 10);
    }

    private List<BusinessResponse> createMockBusinessResponses() {
        return List.of(
            createMockBusinessResponse(1L, "Business 1", new BigDecimal("4.8")),
            createMockBusinessResponse(2L, "Business 2", new BigDecimal("4.5"))
        );
    }

    private BusinessResponse createMockBusinessResponse(Long id, String name, BigDecimal rating) {
        return BusinessResponse.builder()
            .id(id)
            .name(name)
            .legalName(name)
            .verticalId(VERTICAL_ID)
            .municipalityId(MUNICIPALITY_ID)
            .averageRating(rating)
            .reviewCount(10)
            .build();
    }
}
