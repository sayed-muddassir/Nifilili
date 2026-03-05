package com.nifilili.order.service.impl;

import com.nifilili.order.domain.BusinessConfigurationEntity;
import com.nifilili.order.dto.request.UpdateBusinessConfigRequest;
import com.nifilili.order.dto.response.BusinessConfigResponse;
import com.nifilili.order.repository.BusinessConfigurationRepository;
import com.nifilili.order.service.BusinessConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BusinessConfigServiceImpl implements BusinessConfigService {

    private final BusinessConfigurationRepository configRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BusinessConfigResponse> getConfigs(Long businessId) {
        log.debug("Fetching configs for businessId={}", businessId);
        return configRepository.findByBusinessId(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BusinessConfigResponse upsertConfig(Long businessId, UpdateBusinessConfigRequest request) {
        log.info("Upserting config for businessId={}, key={}", businessId, request.getConfigKey());

        BusinessConfigurationEntity entity = configRepository
                .findByBusinessIdAndConfigKey(businessId, request.getConfigKey())
                .orElse(BusinessConfigurationEntity.builder()
                        .businessId(businessId)
                        .configKey(request.getConfigKey())
                        .createdAt(LocalDateTime.now())
                        .build());

        entity.setConfigValue(request.getConfigValue());
        entity.setUpdatedAt(LocalDateTime.now());

        return toResponse(configRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Map<String, String>> getConfigMap(Set<Long> businessIds) {
        log.debug("Fetching config map for {} businesses", businessIds.size());

        List<BusinessConfigurationEntity> configs = configRepository.findByBusinessIdIn(businessIds);

        Map<Long, Map<String, String>> result = new HashMap<>();
        for (BusinessConfigurationEntity config : configs) {
            result.computeIfAbsent(config.getBusinessId(), k -> new HashMap<>())
                    .put(config.getConfigKey(), config.getConfigValue());
        }

        // Ensure all business IDs are present in the map, even if they have no configs
        for (Long businessId : businessIds) {
            result.putIfAbsent(businessId, new HashMap<>());
        }

        return result;
    }

    private BusinessConfigResponse toResponse(BusinessConfigurationEntity entity) {
        return BusinessConfigResponse.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .configValue(entity.getConfigValue())
                .build();
    }
}
