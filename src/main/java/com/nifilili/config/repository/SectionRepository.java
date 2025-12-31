package com.nifilili.config.repository;

import com.nifilili.config.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByVerticalId(Long verticalId);
}
