package com.nifilili.business.controller.admin;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nifilili.business.dto.request.CreateDistrictRequest;
import com.nifilili.business.dto.request.CreateMunicipalityRequest;
import com.nifilili.business.dto.request.CreateProvinceRequest;
import com.nifilili.business.dto.response.DistrictResponse;
import com.nifilili.business.dto.response.MunicipalityResponse;
import com.nifilili.business.dto.response.ProvinceResponse;
import com.nifilili.business.service.DistrictAdminService;
import com.nifilili.business.service.MunicipalityAdminService;
import com.nifilili.business.service.ProvinceAdminService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessLocationAdminControllerTest {

    @Mock
    private ProvinceAdminService provinceAdminService;

    @Mock
    private DistrictAdminService districtAdminService;

    @Mock
    private MunicipalityAdminService municipalityAdminService;

    @InjectMocks
    private BusinessLocationAdminController controller;

    @Test
    void provinceEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        CreateProvinceRequest request = new CreateProvinceRequest();
        ProvinceResponse response = new ProvinceResponse();
        response.setId(1L);
        List<ProvinceResponse> listResponse = List.of(response);

        when(provinceAdminService.create(request)).thenReturn(response);
        when(provinceAdminService.getAll()).thenReturn(listResponse);
        when(provinceAdminService.getById(1L)).thenReturn(response);
        when(provinceAdminService.update(1L, request)).thenReturn(response);

        ResponseEntity<ProvinceResponse> createResult = controller.createProvince(request);
        ResponseEntity<List<ProvinceResponse>> listResult = controller.getProvinces();
        ResponseEntity<ProvinceResponse> getResult = controller.getProvince(1L);
        ResponseEntity<ProvinceResponse> updateResult = controller.updateProvince(1L, request);
        ResponseEntity<Void> deleteResult = controller.deleteProvince(1L);

        assertEquals(HttpStatus.CREATED, createResult.getStatusCode());
        assertSame(response, createResult.getBody());
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertSame(response, updateResult.getBody());
        assertEquals(HttpStatus.NO_CONTENT, deleteResult.getStatusCode());
        verify(provinceAdminService).delete(1L);
    }

    @Test
    void districtEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        CreateDistrictRequest request = new CreateDistrictRequest();
        DistrictResponse response = new DistrictResponse();
        response.setId(2L);
        List<DistrictResponse> listResponse = List.of(response);

        when(districtAdminService.create(request)).thenReturn(response);
        when(districtAdminService.getByProvince(10L)).thenReturn(listResponse);
        when(districtAdminService.getById(2L)).thenReturn(response);
        when(districtAdminService.update(2L, request)).thenReturn(response);

        ResponseEntity<DistrictResponse> createResult = controller.createDistrict(request);
        ResponseEntity<List<DistrictResponse>> listResult = controller.getDistrictsByProvince(10L);
        ResponseEntity<DistrictResponse> getResult = controller.getDistrict(2L);
        ResponseEntity<DistrictResponse> updateResult = controller.updateDistrict(2L, request);
        ResponseEntity<Void> deleteResult = controller.deleteDistrict(2L);

        assertEquals(HttpStatus.CREATED, createResult.getStatusCode());
        assertSame(response, createResult.getBody());
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertSame(response, updateResult.getBody());
        assertEquals(HttpStatus.NO_CONTENT, deleteResult.getStatusCode());
        verify(districtAdminService).delete(2L);
    }

    @Test
    void municipalityEndpoints_WhenServicesReturnResponses_ShouldDelegateAndReturnExpectedStatuses() {
        CreateMunicipalityRequest request = new CreateMunicipalityRequest();
        MunicipalityResponse response = new MunicipalityResponse();
        response.setId(3L);
        List<MunicipalityResponse> listResponse = List.of(response);

        when(municipalityAdminService.create(request)).thenReturn(response);
        when(municipalityAdminService.getByDistrict(20L)).thenReturn(listResponse);
        when(municipalityAdminService.getById(3L)).thenReturn(response);
        when(municipalityAdminService.update(3L, request)).thenReturn(response);

        ResponseEntity<MunicipalityResponse> createResult = controller.createMunicipality(request);
        ResponseEntity<List<MunicipalityResponse>> listResult = controller.getMunicipalitiesByDistrict(20L);
        ResponseEntity<MunicipalityResponse> getResult = controller.getMunicipality(3L);
        ResponseEntity<MunicipalityResponse> updateResult = controller.updateMunicipality(3L, request);
        ResponseEntity<Void> deleteResult = controller.deleteMunicipality(3L);

        assertEquals(HttpStatus.CREATED, createResult.getStatusCode());
        assertSame(response, createResult.getBody());
        assertEquals(HttpStatus.OK, listResult.getStatusCode());
        assertSame(listResponse, listResult.getBody());
        assertEquals(HttpStatus.OK, getResult.getStatusCode());
        assertSame(response, getResult.getBody());
        assertEquals(HttpStatus.OK, updateResult.getStatusCode());
        assertSame(response, updateResult.getBody());
        assertEquals(HttpStatus.NO_CONTENT, deleteResult.getStatusCode());
        verify(municipalityAdminService).delete(3L);
    }
}
