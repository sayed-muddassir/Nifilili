package com.nifilili.business.controller.owner;

import com.nifilili.business.dto.response.DistrictResponse;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.dto.response.ProvinceResponse;
import com.nifilili.business.service.DistrictAdminService;
import com.nifilili.business.service.MunicipalityAdminService;
import com.nifilili.business.service.ProvinceAdminService;
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
class BusinessLocationOwnerControllerTest {

    @Mock
    private ProvinceAdminService provinceAdminService;

    @Mock
    private DistrictAdminService districtAdminService;

    @Mock
    private MunicipalityAdminService municipalityAdminService;

    @InjectMocks
    private BusinessLocationOwnerController controller;

    @Test
    void provinceEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        ProvinceResponse response = new ProvinceResponse();
        response.setId(1L);
        List<ProvinceResponse> listResponse = List.of(response);

        when(provinceAdminService.getAll()).thenReturn(listResponse);
        when(provinceAdminService.getById(1L)).thenReturn(response);

        ResponseEntity<List<ProvinceResponse>> listResult = controller.getProvinces();
        ResponseEntity<ProvinceResponse> getResult = controller.getProvince(1L);

        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
    }

    @Test
    void districtEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        DistrictResponse response = new DistrictResponse();
        response.setId(2L);
        List<DistrictResponse> listResponse = List.of(response);

        when(districtAdminService.getByProvince(10L)).thenReturn(listResponse);
        when(districtAdminService.getById(2L)).thenReturn(response);

        ResponseEntity<List<DistrictResponse>> listResult = controller.getDistrictsByProvince(10L);
        ResponseEntity<DistrictResponse> getResult = controller.getDistrict(2L);

        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
    }

    @Test
    void municipalityEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        MunicipalityResponse response = new MunicipalityResponse();
        response.setId(3L);
        List<MunicipalityResponse> listResponse = List.of(response);

        when(municipalityAdminService.getByDistrict(20L)).thenReturn(listResponse);
        when(municipalityAdminService.getById(3L)).thenReturn(response);
        ResponseEntity<MunicipalityResponse> getResult = controller.getMunicipality(3L);

        ResponseEntity<List<MunicipalityResponse>> listResult = controller.getMunicipalitiesByDistrict(20L);

        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
    }
}
