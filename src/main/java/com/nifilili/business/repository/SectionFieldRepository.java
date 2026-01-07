package com.nifilili.business.repository;

import com.nifilili.business.domain.SectionField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionFieldRepository extends JpaRepository<SectionField, Long> {
    List<SectionField> findBySectionId(Long sectionId);
}
