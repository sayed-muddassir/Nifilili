package com.nifilili.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nifilili.business.domain.MunicipalityMaster;

public interface MunicipalityMasterRepository extends JpaRepository<MunicipalityMaster, Long> {

    List<MunicipalityMaster> findByDistrictIdOrderByNameAsc(Long districtId);

    boolean existsByDistrictId(Long districtId);
}
