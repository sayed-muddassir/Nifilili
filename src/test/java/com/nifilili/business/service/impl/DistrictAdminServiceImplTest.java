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
import com.nifilili.business.domain.DistrictMaster;
import com.nifilili.business.dto.request.CreateDistrictRequest;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.MunicipalityMasterRepository;
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
class DistrictAdminServiceImplTest {

    @Mock
    private DistrictMasterRepository districtMasterRepository;

    @Mock
    private ProvinceMasterRepository provinceMasterRepository;

    @Mock
    private MunicipalityMasterRepository municipalityMasterRepository;

    @InjectMocks
    private DistrictAdminServiceImpl service;

    @Test
    void create_WhenProvinceExists_ShouldPersistDistrict() {
        CreateDistrictRequest request = new CreateDistrictRequest();
        request.setProvinceId(1L);
        request.setName("Kathmandu");

        when(provinceMasterRepository.existsById(1L)).thenReturn(true);
        when(districtMasterRepository.save(any(DistrictMaster.class))).thenAnswer(invocation -> {
            DistrictMaster district = invocation.getArgument(0);
            return TestEntityIdUtil.withId(district, 11L);
        });

        var response = service.create(request);

        assertEquals(11L, response.getId());
        assertEquals(1L, response.getProvinceId());
        assertEquals("Kathmandu", response.getName());

        ArgumentCaptor<DistrictMaster> captor = ArgumentCaptor.forClass(DistrictMaster.class);
        verify(districtMasterRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getProvinceId());
    }

    @Test
    void create_WhenProvinceMissing_ShouldThrowNotFound() {
        CreateDistrictRequest request = new CreateDistrictRequest();
        request.setProvinceId(1L);
        request.setName("Kathmandu");

        when(provinceMasterRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void update_WhenDistrictAndProvinceExist_ShouldUpdateDistrict() {
        CreateDistrictRequest request = new CreateDistrictRequest();
        request.setProvinceId(2L);
        request.setName("Lalitpur");
        DistrictMaster existing = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(1L).name("Old").build(), 12L);

        when(provinceMasterRepository.existsById(2L)).thenReturn(true);
        when(districtMasterRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(districtMasterRepository.save(existing)).thenReturn(existing);

        var response = service.update(12L, request);

        assertEquals(12L, response.getId());
        assertEquals(2L, response.getProvinceId());
        assertEquals("Lalitpur", response.getName());
    }

    @Test
    void update_WhenProvinceMissing_ShouldThrowNotFound() {
        CreateDistrictRequest request = new CreateDistrictRequest();
        request.setProvinceId(2L);
        request.setName("Lalitpur");

        when(provinceMasterRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.update(12L, request));
    }

    @Test
    void getById_WhenDistrictExists_ShouldReturnDistrict() {
        DistrictMaster district = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(3L).name("Bhaktapur").build(), 13L);
        when(districtMasterRepository.findById(13L)).thenReturn(Optional.of(district));

        var response = service.getById(13L);

        assertEquals(13L, response.getId());
        assertEquals("Bhaktapur", response.getName());
    }

    @Test
    void getById_WhenDistrictMissing_ShouldThrowNotFound() {
        when(districtMasterRepository.findById(14L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(14L));
    }

    @Test
    void getByProvince_WhenProvinceExists_ShouldReturnDistricts() {
        DistrictMaster one = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(4L).name("Kathmandu").build(), 15L);
        DistrictMaster two = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(4L).name("Lalitpur").build(), 16L);

        when(provinceMasterRepository.existsById(4L)).thenReturn(true);
        when(districtMasterRepository.findByProvinceIdOrderByNameAsc(4L)).thenReturn(List.of(one, two));

        var response = service.getByProvince(4L);

        assertEquals(2, response.size());
        assertEquals("Kathmandu", response.get(0).getName());
    }

    @Test
    void getByProvince_WhenProvinceMissing_ShouldThrowNotFound() {
        when(provinceMasterRepository.existsById(4L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getByProvince(4L));
    }

    @Test
    void delete_WhenDistrictHasNoMunicipalities_ShouldDeleteDistrict() {
        DistrictMaster district = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(5L).name("Kathmandu").build(), 17L);
        when(districtMasterRepository.findById(17L)).thenReturn(Optional.of(district));
        when(municipalityMasterRepository.existsByDistrictId(17L)).thenReturn(false);
        doNothing().when(districtMasterRepository).delete(district);

        service.delete(17L);

        verify(districtMasterRepository).delete(district);
    }

    @Test
    void delete_WhenDistrictHasMunicipalities_ShouldThrowInvalidBusinessState() {
        DistrictMaster district = TestEntityIdUtil.withId(DistrictMaster.builder().provinceId(5L).name("Kathmandu").build(), 18L);
        when(districtMasterRepository.findById(18L)).thenReturn(Optional.of(district));
        when(municipalityMasterRepository.existsByDistrictId(18L)).thenReturn(true);

        assertThrows(InvalidBusinessStateException.class, () -> service.delete(18L));
        verify(districtMasterRepository, never()).delete(any());
    }
}
