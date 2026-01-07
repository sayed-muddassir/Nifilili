package com.nifilili.business.repository;

import com.nifilili.business.domain.SectionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionDefinition, Long> {
    List<SectionDefinition> findByVerticalId(Long verticalId);
}
