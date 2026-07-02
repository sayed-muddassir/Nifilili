package com.nifilili.business.repository;

import com.nifilili.business.domain.MunicipalityImageMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipalityImageMasterRepository extends JpaRepository<MunicipalityImageMaster, Long> {

    void deleteAllByMunicipalityId(Long municipalityId);
}
