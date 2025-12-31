package com.nifilili.config.repository;

import com.nifilili.config.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByBusinessVerticalId(Long verticalId);
}
