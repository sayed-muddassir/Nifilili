package com.nifilili.business.repository;

import com.nifilili.business.domain.DocumentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentDefinitionRepository
        extends JpaRepository<DocumentDefinition, Long> {

    List<DocumentDefinition> findByVerticalId(Long verticalId);
}
