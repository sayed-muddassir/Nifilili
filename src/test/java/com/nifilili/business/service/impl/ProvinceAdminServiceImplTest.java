package com.nifilili.business.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.business.domain.ProvinceMaster;
import com.nifilili.business.dto.request.CreateProvinceRequest;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.ProvinceMasterRepository;
import com.nifilili.core.exception.InvalidBusinessStateException;
import com.nifilili.core.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvinceAdminServiceImplTest {

    @Mock
    private ProvinceMasterRepository provinceMasterRepository;

    @Mock
    private DistrictMasterRepository districtMasterRepository;

    @InjectMocks
    private ProvinceAdminServiceImpl service;

    @Test
    void create_WhenRequestIsValid_ShouldPersistProvince() {
        CreateProvinceRequest request = new CreateProvinceRequest();
        request.setName("Bagmati");

        when(provinceMasterRepository.save(any(ProvinceMaster.class))).thenAnswer(invocation -> {
            ProvinceMaster province = invocation.getArgument(0);
            return TestEntityIdUtil.withId(province, 1L);
        });

        var response = service.create(request);

        assertEquals(1L, response.getId());
        assertEquals("Bagmati", response.getName());

        ArgumentCaptor<ProvinceMaster> captor = ArgumentCaptor.forClass(ProvinceMaster.class);
        verify(provinceMasterRepository).save(captor.capture());
        assertEquals("Bagmati", captor.getValue().getName());
    }

    @Test
    void create_WhenRepositoryReturnsEntity_ShouldReturnMappedProvince() {
        CreateProvinceRequest request = new CreateProvinceRequest();
        request.setName("Gandaki");

        ProvinceMaster saved = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Gandaki").build(), 2L);
        when(provinceMasterRepository.save(any(ProvinceMaster.class))).thenReturn(saved);

        var response = service.create(request);

        assertEquals(2L, response.getId());
        assertEquals("Gandaki", response.getName());
    }

    @Test
    void update_WhenProvinceExists_ShouldUpdateProvince() {
        CreateProvinceRequest request = new CreateProvinceRequest();
        request.setName("Updated Province");
        ProvinceMaster existing = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Old").build(), 10L);

        when(provinceMasterRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(provinceMasterRepository.save(existing)).thenReturn(existing);

        var response = service.update(10L, request);

        assertEquals(10L, response.getId());
        assertEquals("Updated Province", response.getName());
    }

    @Test
    void update_WhenProvinceMissing_ShouldThrowNotFound() {
        CreateProvinceRequest request = new CreateProvinceRequest();
        request.setName("Updated Province");

        when(provinceMasterRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(10L, request));
    }

    @Test
    void getById_WhenProvinceExists_ShouldReturnProvince() {
        ProvinceMaster province = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Bagmati").build(), 20L);
        when(provinceMasterRepository.findById(20L)).thenReturn(Optional.of(province));

        var response = service.getById(20L);

        assertEquals(20L, response.getId());
        assertEquals("Bagmati", response.getName());
    }

    @Test
    void getById_WhenProvinceMissing_ShouldThrowNotFound() {
        when(provinceMasterRepository.findById(21L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(21L));
    }

    @Test
    void getAll_WhenProvincesExist_ShouldReturnOrderedResponses() {
        ProvinceMaster one = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Bagmati").build(), 1L);
        ProvinceMaster two = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Gandaki").build(), 2L);

        when(provinceMasterRepository.findAllByOrderByNameAsc()).thenReturn(List.of(one, two));

        var response = service.getAll();

        assertEquals(2, response.size());
        assertEquals("Bagmati", response.get(0).getName());
        assertEquals("Gandaki", response.get(1).getName());
    }

    @Test
    void getAll_WhenNoProvincesExist_ShouldReturnEmptyList() {
        when(provinceMasterRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        var response = service.getAll();

        assertEquals(0, response.size());
    }

    @Test
    void delete_WhenProvinceHasNoDistricts_ShouldDeleteProvince() {
        ProvinceMaster province = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Bagmati").build(), 30L);
        when(provinceMasterRepository.findById(30L)).thenReturn(Optional.of(province));
        when(districtMasterRepository.existsByProvinceId(30L)).thenReturn(false);
        doNothing().when(provinceMasterRepository).delete(province);

        service.delete(30L);

        verify(provinceMasterRepository).delete(province);
    }

    @Test
    void delete_WhenProvinceHasDistricts_ShouldThrowInvalidBusinessState() {
        ProvinceMaster province = TestEntityIdUtil.withId(ProvinceMaster.builder().name("Bagmati").build(), 31L);
        when(provinceMasterRepository.findById(31L)).thenReturn(Optional.of(province));
        when(districtMasterRepository.existsByProvinceId(31L)).thenReturn(true);

        assertThrows(InvalidBusinessStateException.class, () -> service.delete(31L));
        verify(provinceMasterRepository, never()).delete(any());
    }
}
