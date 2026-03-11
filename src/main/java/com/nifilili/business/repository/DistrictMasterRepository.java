package com.nifilili.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nifilili.business.domain.DistrictMaster;

public interface DistrictMasterRepository extends JpaRepository<DistrictMaster, Long> {

    List<DistrictMaster> findByProvinceIdOrderByNameAsc(Long provinceId);

    boolean existsByProvinceId(Long provinceId);
}
