package com.nifilili.offering.service.owner;

import com.nifilili.offering.domain.OfferingAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantAttributeEntity;
import com.nifilili.offering.domain.OfferingVariantEntity;
import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.repository.OfferingAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantAttributeRepository;
import com.nifilili.offering.repository.OfferingVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VariantAttributeService {

    private final OfferingVariantAttributeRepository variantAttributeRepository;
    private final OfferingVariantRepository variantRepository;
    private final OfferingAttributeRepository attributeRepository;

    public void assignAttributes(
            Long variantId,
            AssignVariantAttributesRequest request
    ) {


        request.attributes().forEach(requestAttribute ->
                {
                    if (requestAttribute.offeringAttributesId() == null) {
                        throw new IllegalArgumentException("TODO: Custom attributes are not supported yet");
                    }

                    if (!attributeRepository.existsById(requestAttribute.offeringAttributesId())) {
                        throw new IllegalArgumentException("Invalid attribute id");
                    }

                    OfferingAttributeEntity attributeById = attributeRepository.findById(requestAttribute.offeringAttributesId())
                            .orElseThrow(() -> new IllegalArgumentException("Attribute not found with given id: " + requestAttribute.offeringAttributesId()));

                    if (attributeById.getName().equalsIgnoreCase(requestAttribute.attributeName())) {
                        if (!attributeById.getOptions().contains(requestAttribute.attributeValue())) {
                            throw new IllegalArgumentException("Invalid attribute value for attribute: "
                                    + attributeById.getName() + " allowed values are: "
                                    + String.join(", ", attributeById.getOptions()));
                        }

                        OfferingVariantEntity variantById = variantRepository.findById(variantId)
                                .orElseThrow(() -> new IllegalArgumentException("Variant not found with given id: " + variantId));

                        OfferingVariantAttributeEntity offeringVariantAttributeEntity = new OfferingVariantAttributeEntity();
                        offeringVariantAttributeEntity.setVariant(variantById);
                        offeringVariantAttributeEntity.setOfferingAttribute(attributeById);
                        offeringVariantAttributeEntity.setAttributeName(requestAttribute.attributeName());
                        offeringVariantAttributeEntity.setAttributeValue(requestAttribute.attributeValue());
                        variantAttributeRepository.save(offeringVariantAttributeEntity);

                    } else {
                        throw new IllegalArgumentException("Attribute name does not match with the attribute id provided");
                    }
                }

        );
    }
}