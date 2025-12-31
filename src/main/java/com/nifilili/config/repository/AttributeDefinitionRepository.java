package com.nifilili.config.repository;


import com.nifilili.config.domain.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeDefinitionRepository
        extends JpaRepository<AttributeDefinition, Long> {

    List<AttributeDefinition> findByVerticalId(Long verticalId);
}
