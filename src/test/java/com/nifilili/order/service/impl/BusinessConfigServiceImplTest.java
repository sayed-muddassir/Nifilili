package com.nifilili.order.service.impl;

import com.nifilili.business.TestEntityIdUtil;
import com.nifilili.order.domain.BusinessConfigurationEntity;
import com.nifilili.order.dto.request.UpdateBusinessConfigRequest;
import com.nifilili.order.dto.response.BusinessConfigResponse;
import com.nifilili.order.repository.BusinessConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessConfigServiceImplTest {

    private static final Long BUSINESS_ID = 5L;

    @Mock
    private BusinessConfigurationRepository configRepository;

    @InjectMocks
    private BusinessConfigServiceImpl configService;

    // --- getConfigs ---

    @Test
    void getConfigs_WhenConfigsExist_ShouldReturnList() {
        BusinessConfigurationEntity entity = buildConfigEntity(1L, BUSINESS_ID, "tax_rate", "15");

        when(configRepository.findByBusinessId(BUSINESS_ID)).thenReturn(List.of(entity));

        List<BusinessConfigResponse> result = configService.getConfigs(BUSINESS_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("tax_rate");
        assertThat(result.get(0).getConfigValue()).isEqualTo("15");
    }

    @Test
    void getConfigs_WhenNoConfigs_ShouldReturnEmptyList() {
        when(configRepository.findByBusinessId(BUSINESS_ID)).thenReturn(List.of());

        List<BusinessConfigResponse> result = configService.getConfigs(BUSINESS_ID);

        assertThat(result).isEmpty();
    }

    // --- upsertConfig ---

    @Test
    void upsertConfig_WhenConfigExists_ShouldUpdateValue() {
        BusinessConfigurationEntity existing = buildConfigEntity(1L, BUSINESS_ID, "tax_rate", "13");
        UpdateBusinessConfigRequest request = new UpdateBusinessConfigRequest();
        request.setConfigKey("tax_rate");
        request.setConfigValue("15");

        when(configRepository.findByBusinessIdAndConfigKey(BUSINESS_ID, "tax_rate"))
                .thenReturn(Optional.of(existing));
        when(configRepository.save(any(BusinessConfigurationEntity.class))).thenReturn(existing);

        BusinessConfigResponse result = configService.upsertConfig(BUSINESS_ID, request);

        ArgumentCaptor<BusinessConfigurationEntity> captor =
                ArgumentCaptor.forClass(BusinessConfigurationEntity.class);
        verify(configRepository).save(captor.capture());
        assertThat(captor.getValue().getConfigValue()).isEqualTo("15");
    }

    @Test
    void upsertConfig_WhenConfigNotExists_ShouldCreateNew() {
        UpdateBusinessConfigRequest request = new UpdateBusinessConfigRequest();
        request.setConfigKey("delivery_charge");
        request.setConfigValue("50");

        when(configRepository.findByBusinessIdAndConfigKey(BUSINESS_ID, "delivery_charge"))
                .thenReturn(Optional.empty());

        BusinessConfigurationEntity saved = buildConfigEntity(2L, BUSINESS_ID, "delivery_charge", "50");
        when(configRepository.save(any(BusinessConfigurationEntity.class))).thenReturn(saved);

        BusinessConfigResponse result = configService.upsertConfig(BUSINESS_ID, request);

        ArgumentCaptor<BusinessConfigurationEntity> captor =
                ArgumentCaptor.forClass(BusinessConfigurationEntity.class);
        verify(configRepository).save(captor.capture());
        assertThat(captor.getValue().getBusinessId()).isEqualTo(BUSINESS_ID);
        assertThat(captor.getValue().getConfigKey()).isEqualTo("delivery_charge");
        assertThat(captor.getValue().getConfigValue()).isEqualTo("50");
    }

    // --- getConfigMap ---

    @Test
    void getConfigMap_WhenConfigsExist_ShouldGroupByBusinessId() {
        BusinessConfigurationEntity config1 = buildConfigEntity(1L, 5L, "tax_rate", "13");
        BusinessConfigurationEntity config2 = buildConfigEntity(2L, 5L, "delivery_charge", "50");
        BusinessConfigurationEntity config3 = buildConfigEntity(3L, 6L, "tax_rate", "10");

        when(configRepository.findByBusinessIdIn(Set.of(5L, 6L)))
                .thenReturn(List.of(config1, config2, config3));

        Map<Long, Map<String, String>> result = configService.getConfigMap(Set.of(5L, 6L));

        assertThat(result).hasSize(2);
        assertThat(result.get(5L)).containsEntry("tax_rate", "13");
        assertThat(result.get(5L)).containsEntry("delivery_charge", "50");
        assertThat(result.get(6L)).containsEntry("tax_rate", "10");
    }

    @Test
    void getConfigMap_WhenBusinessHasNoConfigs_ShouldReturnEmptyMapForThatBusiness() {
        when(configRepository.findByBusinessIdIn(Set.of(5L, 7L))).thenReturn(List.of());

        Map<Long, Map<String, String>> result = configService.getConfigMap(Set.of(5L, 7L));

        assertThat(result).hasSize(2);
        assertThat(result.get(5L)).isEmpty();
        assertThat(result.get(7L)).isEmpty();
    }

    // --- helpers ---

    private BusinessConfigurationEntity buildConfigEntity(Long id, Long businessId,
                                                           String key, String value) {
        BusinessConfigurationEntity entity = BusinessConfigurationEntity.builder()
                .businessId(businessId)
                .configKey(key)
                .configValue(value)
                .build();
        return TestEntityIdUtil.withId(entity, id);
    }
}
