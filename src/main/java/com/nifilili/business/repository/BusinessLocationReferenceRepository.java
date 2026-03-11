package com.nifilili.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nifilili.business.domain.Business;

public interface BusinessLocationReferenceRepository extends JpaRepository<Business, Long> {

    boolean existsByMunicipalityId(Long municipalityId);
}
