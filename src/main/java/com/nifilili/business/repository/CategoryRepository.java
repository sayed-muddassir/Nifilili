package com.nifilili.business.repository;

import com.nifilili.business.domain.CategoryDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryDefinition, Long> {
    List<CategoryDefinition> findByBusinessVerticalId(Long verticalId);
}
