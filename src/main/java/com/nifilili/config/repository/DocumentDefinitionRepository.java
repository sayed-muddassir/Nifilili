package com.nifilili.config.repository;

import com.nifilili.config.domain.DocumentDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentDefinitionRepository
        extends JpaRepository<DocumentDefinition, Long> {

    List<DocumentDefinition> findByVerticalId(Long verticalId);
}
