package com.nifilili.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nifilili.business.domain.ProvinceMaster;

public interface ProvinceMasterRepository extends JpaRepository<ProvinceMaster, Long> {

    List<ProvinceMaster> findAllByOrderByNameAsc();
}
