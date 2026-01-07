package com.nifilili.business.repository;

import com.nifilili.business.domain.VerticalDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerticalRepository extends JpaRepository<VerticalDefinition, Long> {}
