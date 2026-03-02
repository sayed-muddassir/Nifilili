package com.nifilili.offering.service.impl;

import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.response.VariantAttributeDetailResponse;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import com.nifilili.offering.service.VariantAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VariantAttributeServiceImpl implements VariantAttributeService {

    private final OfferingVariantAttributeRepository variantAttributeRepository;
    private final OfferingVariantRepository variantRepository;
    private final OfferingAttributeRepository attributeRepository;

    public void assignAttributes(
            Long variantId,
            AssignVariantAttributesRequest request
    ) {


        OfferingVariantEntity variantById = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with given id: " + variantId));

        request.attributes().forEach(requestAttribute -> {
            OfferingVariantAttributeEntity entity = new OfferingVariantAttributeEntity();
            entity.setVariant(variantById);
            entity.setAttributeName(requestAttribute.attributeName());
            entity.setAttributeValue(requestAttribute.attributeValue());

            if (requestAttribute.offeringAttributesId() != null) {
                // Predefined attribute — validate name match and allowed values
                OfferingAttributeEntity attributeById = attributeRepository.findById(requestAttribute.offeringAttributesId())
                        .orElseThrow(() -> new IllegalArgumentException("Attribute not found with given id: " + requestAttribute.offeringAttributesId()));

                if (!attributeById.getName().equalsIgnoreCase(requestAttribute.attributeName())) {
                    throw new IllegalArgumentException("Attribute name does not match with the attribute id provided");
                }

                if (attributeById.getOptions() != null && !attributeById.getOptions().isEmpty()
                        && !attributeById.getOptions().contains(requestAttribute.attributeValue())) {
                    throw new IllegalArgumentException("Invalid attribute value for attribute: "
                            + attributeById.getName() + " allowed values are: "
                            + String.join(", ", attributeById.getOptions()));
                }

                entity.setOfferingAttribute(attributeById);
            } else {
                // Custom attribute — no predefined definition, allow any name/value
                log.debug("Creating custom attribute: name={}, value={}", requestAttribute.attributeName(), requestAttribute.attributeValue());
                entity.setOfferingAttribute(null);
            }

            variantAttributeRepository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public List<VariantAttributeDetailResponse> listAttributes(Long variantId) {
        log.debug("Listing attributes for variantId={}", variantId);
        return variantAttributeRepository.findByVariantId(variantId).stream()
                .map(a -> new VariantAttributeDetailResponse(
                        a.getId(),
                        a.getOfferingAttribute() != null ? a.getOfferingAttribute().getId() : null,
                        a.getAttributeName(),
                        a.getAttributeValue()
                ))
                .toList();
    }

    public void updateAttribute(Long attributeId, String newValue) {
        log.info("Updating variant attribute id={} value={}", attributeId, newValue);
        OfferingVariantAttributeEntity attr = variantAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new IllegalArgumentException("Variant attribute not found"));

        // Validate against predefined options if linked to a predefined attribute
        if (attr.getOfferingAttribute() != null && attr.getOfferingAttribute().getOptions() != null
                && !attr.getOfferingAttribute().getOptions().isEmpty()) {
            if (!attr.getOfferingAttribute().getOptions().contains(newValue)) {
                throw new IllegalArgumentException("Invalid attribute value for attribute: "
                        + attr.getAttributeName() + " allowed values are: "
                        + String.join(", ", attr.getOfferingAttribute().getOptions()));
            }
        }

        attr.setAttributeValue(newValue);
        variantAttributeRepository.save(attr);
    }

    public void deleteAttribute(Long attributeId) {
        log.info("Deleting variant attribute id={}", attributeId);
        if (!variantAttributeRepository.existsById(attributeId)) {
            throw new IllegalArgumentException("Variant attribute not found");
        }
        variantAttributeRepository.deleteById(attributeId);
    }
}