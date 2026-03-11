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
import com.nifilili.business.domain.MunicipalityMaster;
import com.nifilili.business.dto.request.CreateMunicipalityRequest;
import com.nifilili.business.repository.BusinessLocationReferenceRepository;
import com.nifilili.business.repository.DistrictMasterRepository;
import com.nifilili.business.repository.MunicipalityMasterRepository;
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
class MunicipalityAdminServiceImplTest {

    @Mock
    private MunicipalityMasterRepository municipalityMasterRepository;

    @Mock
    private DistrictMasterRepository districtMasterRepository;

    @Mock
    private BusinessLocationReferenceRepository businessLocationReferenceRepository;

    @InjectMocks
    private MunicipalityAdminServiceImpl service;

    @Test
    void create_WhenDistrictExists_ShouldPersistMunicipality() {
        CreateMunicipalityRequest request = new CreateMunicipalityRequest();
        request.setDistrictId(1L);
        request.setName("Kathmandu Metropolitan City");
        request.setType("metropolitan");

        when(districtMasterRepository.existsById(1L)).thenReturn(true);
        when(municipalityMasterRepository.save(any(MunicipalityMaster.class))).thenAnswer(invocation -> {
            MunicipalityMaster municipality = invocation.getArgument(0);
            return TestEntityIdUtil.withId(municipality, 21L);
        });

        var response = service.create(request);

        assertEquals(21L, response.getId());
        assertEquals(1L, response.getDistrictId());
        assertEquals("metropolitan", response.getType());

        ArgumentCaptor<MunicipalityMaster> captor = ArgumentCaptor.forClass(MunicipalityMaster.class);
        verify(municipalityMasterRepository).save(captor.capture());
        assertEquals("Kathmandu Metropolitan City", captor.getValue().getName());
    }

    @Test
    void create_WhenDistrictMissing_ShouldThrowNotFound() {
        CreateMunicipalityRequest request = new CreateMunicipalityRequest();
        request.setDistrictId(1L);
        request.setName("Kathmandu Metropolitan City");
        request.setType("metropolitan");

        when(districtMasterRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }

    @Test
    void update_WhenMunicipalityAndDistrictExist_ShouldUpdateMunicipality() {
        CreateMunicipalityRequest request = new CreateMunicipalityRequest();
        request.setDistrictId(2L);
        request.setName("Lalitpur Metropolitan City");
        request.setType("metropolitan");
        MunicipalityMaster existing = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(1L).name("Old").type("municipality").build(), 22L
        );

        when(districtMasterRepository.existsById(2L)).thenReturn(true);
        when(municipalityMasterRepository.findById(22L)).thenReturn(Optional.of(existing));
        when(municipalityMasterRepository.save(existing)).thenReturn(existing);

        var response = service.update(22L, request);

        assertEquals(22L, response.getId());
        assertEquals(2L, response.getDistrictId());
        assertEquals("Lalitpur Metropolitan City", response.getName());
    }

    @Test
    void update_WhenDistrictMissing_ShouldThrowNotFound() {
        CreateMunicipalityRequest request = new CreateMunicipalityRequest();
        request.setDistrictId(2L);
        request.setName("Lalitpur Metropolitan City");
        request.setType("metropolitan");

        when(districtMasterRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.update(22L, request));
    }

    @Test
    void getById_WhenMunicipalityExists_ShouldReturnMunicipality() {
        MunicipalityMaster municipality = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(3L).name("Pokhara").type("metropolitan").build(), 23L
        );
        when(municipalityMasterRepository.findById(23L)).thenReturn(Optional.of(municipality));

        var response = service.getById(23L);

        assertEquals(23L, response.getId());
        assertEquals("Pokhara", response.getName());
    }

    @Test
    void getById_WhenMunicipalityMissing_ShouldThrowNotFound() {
        when(municipalityMasterRepository.findById(24L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(24L));
    }

    @Test
    void getByDistrict_WhenDistrictExists_ShouldReturnMunicipalities() {
        MunicipalityMaster one = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(4L).name("Kathmandu").type("metropolitan").build(), 25L
        );
        MunicipalityMaster two = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(4L).name("Kirtipur").type("municipality").build(), 26L
        );

        when(districtMasterRepository.existsById(4L)).thenReturn(true);
        when(municipalityMasterRepository.findByDistrictIdOrderByNameAsc(4L)).thenReturn(List.of(one, two));

        var response = service.getByDistrict(4L);

        assertEquals(2, response.size());
        assertEquals("Kathmandu", response.get(0).getName());
    }

    @Test
    void getByDistrict_WhenDistrictMissing_ShouldThrowNotFound() {
        when(districtMasterRepository.existsById(4L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getByDistrict(4L));
    }

    @Test
    void delete_WhenMunicipalityHasNoBusinessReferences_ShouldDeleteMunicipality() {
        MunicipalityMaster municipality = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(5L).name("Kathmandu").type("metropolitan").build(), 27L
        );
        when(municipalityMasterRepository.findById(27L)).thenReturn(Optional.of(municipality));
        when(businessLocationReferenceRepository.existsByMunicipalityId(27L)).thenReturn(false);
        doNothing().when(municipalityMasterRepository).delete(municipality);

        service.delete(27L);

        verify(municipalityMasterRepository).delete(municipality);
    }

    @Test
    void delete_WhenMunicipalityHasBusinessReferences_ShouldThrowInvalidBusinessState() {
        MunicipalityMaster municipality = TestEntityIdUtil.withId(
                MunicipalityMaster.builder().districtId(5L).name("Kathmandu").type("metropolitan").build(), 28L
        );
        when(municipalityMasterRepository.findById(28L)).thenReturn(Optional.of(municipality));
        when(businessLocationReferenceRepository.existsByMunicipalityId(28L)).thenReturn(true);

        assertThrows(InvalidBusinessStateException.class, () -> service.delete(28L));
        verify(municipalityMasterRepository, never()).delete(any());
    }
}
