package com.nifilili.offering.service.admin;

import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingCategoryEntity;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferingAttributeService {

    private final OfferingAttributeRepository attributeRepository;
    private final OfferingCategoryRepository categoryRepository;

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
}

