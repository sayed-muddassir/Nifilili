package com.nifilili.offering.service.impl;

import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
import com.nifilili.offering.service.OfferingAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingAttributeServiceImpl implements OfferingAttributeService {

    private final OfferingAttributeRepository attributeRepository;
    private final OfferingCategoryRepository categoryRepository;
    private final OfferingVariantAttributeRepository variantAttributeRepository;

    public OfferingAttributeEntity create(
            Long categoryId,
            String name,
            AttributeType type,
            List<String> options
    ) {
        OfferingCategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        OfferingAttributeEntity attribute = new OfferingAttributeEntity();
        attribute.setCategory(category);
        attribute.setName(name);
        attribute.setAttributeType(type);
        attribute.setOptions(options);

        return attributeRepository.save(attribute);
    }

    @Transactional(readOnly = true)
    public List<OfferingAttributeEntity> getByCategory(Long categoryId) {
        return attributeRepository.findByCategoryId(categoryId);
    }

    public OfferingAttributeEntity update(Long id, String name, AttributeType type, List<String> options) {
        log.info("Updating attribute id={} name={}", id, name);
        OfferingAttributeEntity attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found"));
        attribute.setName(name);
        attribute.setAttributeType(type);
        attribute.setOptions(options);
        return attributeRepository.save(attribute);
    }

    public void delete(Long id) {
        log.info("Deleting attribute id={}", id);
        if (!attributeRepository.existsById(id)) {
            throw new IllegalArgumentException("Attribute not found");
        }
        if (variantAttributeRepository.existsByOfferingAttributeId(id)) {
            throw new IllegalStateException("Cannot delete attribute in use by variant attributes");
        }
        attributeRepository.deleteById(id);
    }
}

