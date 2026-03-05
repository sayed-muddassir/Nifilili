package com.nifilili.order.repository;

import com.nifilili.order.domain.BusinessConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessConfigurationRepository extends JpaRepository<BusinessConfigurationEntity, Long> {

    List<BusinessConfigurationEntity> findByBusinessId(Long businessId);

    Optional<BusinessConfigurationEntity> findByBusinessIdAndConfigKey(Long businessId, String configKey);

    List<BusinessConfigurationEntity> findByBusinessIdIn(java.util.Set<Long> businessIds);
}
