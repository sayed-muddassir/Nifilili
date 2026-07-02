package com.nifilili.business.service;

import com.nifilili.business.domain.Business;
import com.nifilili.business.dto.response.BusinessAttributeResponse;
import com.nifilili.business.dto.response.BusinessResponse;
import com.nifilili.business.dto.response.SectionResponse;
import com.nifilili.business.repository.*;
import com.nifilili.business.service.impl.BusinessQueryServiceImpl;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessQueryService Tests")
class BusinessQueryServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BusinessCategoryRepository businessCategoryRepository;

    @Mock
    private BusinessSectionDataRepository businessDataRepository;

    @Mock
    private BusinessAttributeRepository businessAttributeRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @InjectMocks
    private BusinessQueryServiceImpl businessQueryService;

    private static final Long VERTICAL_ID = 1000L;
    private static final Long MUNICIPALITY_ID = 1000L;
    private static final int LIMIT = 5;

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenVerticalIdIsNull")
    void testGetTopNByVerticalAndMunicipality_NullVerticalId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(null, MUNICIPALITY_ID, LIMIT)
        );

        assertEquals("verticalId must be a positive number", exception.getMessage());
        verify(businessRepository, never()).findTopNByVerticalAndMunicipalityAndStatus(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenVerticalIdIsNegative")
    void testGetTopNByVerticalAndMunicipality_NegativeVerticalId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(-1L, MUNICIPALITY_ID, LIMIT)
        );

        assertEquals("verticalId must be a positive number", exception.getMessage());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenMunicipalityIdIsNull")
    void testGetTopNByVerticalAndMunicipality_NullMunicipalityId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, null, LIMIT)
        );

        assertEquals("municipalityId must be a positive number", exception.getMessage());
        verify(businessRepository, never()).findTopNByVerticalAndMunicipalityAndStatus(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenMunicipalityIdIsNegative")
    void testGetTopNByVerticalAndMunicipality_NegativeMunicipalityId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, -1L, LIMIT)
        );

        assertEquals("municipalityId must be a positive number", exception.getMessage());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenLimitIsZero")
    void testGetTopNByVerticalAndMunicipality_LimitZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 0)
        );

        assertEquals("limit must be between 1 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenLimitIsNegative")
    void testGetTopNByVerticalAndMunicipality_NegativeLimit() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, -5)
        );

        assertEquals("limit must be between 1 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should_ThrowIllegalArgumentException_WhenLimitExceeds100")
    void testGetTopNByVerticalAndMunicipality_LimitExceeds100() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 101)
        );

        assertEquals("limit must be between 1 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should_ReturnEmptyList_WhenNoBusinessesFound")
    void testGetTopNByVerticalAndMunicipality_NoBusinessesFound() {
        when(businessRepository.findTopNByVerticalAndMunicipalityAndStatus(
            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
        )).thenReturn(new ArrayList<>());

        List<BusinessResponse> result = businessQueryService.getTopNByVerticalAndMunicipality(
            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
        );

        assertTrue(result.isEmpty());
        verify(businessRepository, times(1)).findTopNByVerticalAndMunicipalityAndStatus(
            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
        );
    }

//    @Test
//    @DisplayName("Should_ReturnTopBusinesses_WhenFound")
//    void testGetTopNByVerticalAndMunicipality_HappyPath() {
//        Business business1 = createMockBusiness(1L, "Business 1", new BigDecimal("4.8"));
//        Business business2 = createMockBusiness(2L, "Business 2", new BigDecimal("4.5"));
//
//        when(businessRepository.findTopNByVerticalAndMunicipalityAndStatus(
//            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
//        )).thenReturn(List.of(business1, business2));
//
//        when(businessCategoryRepository.findByBusinessId(anyLong())).thenReturn(new ArrayList<>());
//        when(businessDataRepository.findByBusinessId(anyLong())).thenReturn(new ArrayList<>());
//        when(businessAttributeRepository.findByBusinessId(anyLong())).thenReturn(new ArrayList<>());
//
//        List<BusinessResponse> result = businessQueryService.getTopNByVerticalAndMunicipality(
//            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
//        );
//
//        assertEquals(2, result.size());
//        assertEquals("Business 1", result.get(0).getName());
//        assertEquals("Business 2", result.get(1).getName());
//        assertEquals(new BigDecimal("4.8"), result.get(0).getAverageRating());
//        assertEquals(new BigDecimal("4.5"), result.get(1).getAverageRating());
//
//        verify(businessRepository, times(1)).findTopNByVerticalAndMunicipalityAndStatus(
//            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
//        );
//    }

    @Test
    @DisplayName("Should_CallRepositoryWithCorrectStatus_PUBLISHED")
    void testGetTopNByVerticalAndMunicipality_CallsRepositoryWithPublishedStatus() {
        when(businessRepository.findTopNByVerticalAndMunicipalityAndStatus(
            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
        )).thenReturn(new ArrayList<>());

        businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, LIMIT);

        verify(businessRepository, times(1)).findTopNByVerticalAndMunicipalityAndStatus(
            VERTICAL_ID, MUNICIPALITY_ID, LIMIT
        );
    }

//    @Test
//    @DisplayName("Should_HandleValidLimitRange_1to100")
//    void testGetTopNByVerticalAndMunicipality_ValidLimitRanges() {
//        when(businessRepository.findTopNByVerticalAndMunicipalityAndStatus(
//            anyLong(), anyLong(), anyInt()
//        )).thenReturn(new ArrayList<>());
//
//        // Test boundary cases
//        assertDoesNotThrow(() -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 1));
//        assertDoesNotThrow(() -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 50));
//        assertDoesNotThrow(() -> businessQueryService.getTopNByVerticalAndMunicipality(VERTICAL_ID, MUNICIPALITY_ID, 100));
//
//        verify(businessRepository, times(3)).findTopNByVerticalAndMunicipalityAndStatus(
//            VERTICAL_ID, MUNICIPALITY_ID, anyInt()
//        );
//    }

    private Business createMockBusiness(Long id, String name, BigDecimal rating) {
        return Business.builder()
            .name(name)
            .legalName(name)
            .verticalId(VERTICAL_ID)
            .municipalityId(MUNICIPALITY_ID)
            .status(BusinessStatus.PUBLISHED)
            .averageRating(rating)
            .reviewCount(10)
            .build();
    }
}
