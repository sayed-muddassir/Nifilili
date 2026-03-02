package com.nifilili.offering.service;

import com.nifilili.offering.dto.request.AssignVariantAttributesRequest;
import com.nifilili.offering.dto.response.VariantAttributeDetailResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VariantAttributeService {


    /**
     * Assigns attribute values to a variant. Supports both predefined attributes (validated against
     * category definitions) and custom attributes (offeringAttributesId is null, any name/value accepted).
     *
     * @param variantId the variant to assign attributes to
     * @param request   the attribute assignments (ID, name, value triples)
     * @throws IllegalArgumentException if attribute ID is invalid, name doesn't match, or value is not allowed
     */
    void assignAttributes(
            Long variantId,
            AssignVariantAttributesRequest request
    );

    /**
     * Returns all attributes assigned to a variant.
     *
     * @param variantId the variant ID
     * @return list of attribute detail responses
     */
    @Transactional(readOnly = true)
    List<VariantAttributeDetailResponse> listAttributes(Long variantId);

    /**
     * Updates the value of an existing variant attribute.
     *
     * @param attributeId the variant attribute ID to update
     * @param newValue    the new attribute value
     * @throws IllegalArgumentException if the attribute is not found or the value is not allowed
     */
    void updateAttribute(Long attributeId, String newValue);
    /**
     * Deletes a variant attribute by ID.
     *
     * @param attributeId the variant attribute ID to delete
     * @throws IllegalArgumentException if the attribute is not found
     */
    void deleteAttribute(Long attributeId);
}