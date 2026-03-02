package com.nifilili.offering.service;

import com.nifilili.core.enums.util.AttributeType;
import com.nifilili.offering.domain.OfferingAttributeEntity;

import java.util.List;

public interface OfferingAttributeService {

    /**
     * Creates a new attribute definition for a category.
     *
     * @param categoryId the category to attach the attribute to
     * @param name       the attribute name (e.g., "Color", "Size")
     * @param type       the data type (TEXT, NUMBER, DROPDOWN)
     * @param options    allowed values for DROPDOWN type (nullable for others)
     * @return the persisted attribute entity
     * @throws IllegalArgumentException if the category is not found
     */
    OfferingAttributeEntity create(
            Long categoryId,
            String name,
            AttributeType type,
            List<String> options
    );

    /** Returns all attribute definitions for the specified category. */

    List<OfferingAttributeEntity> getByCategory(Long categoryId);

    /**
     * Updates an existing attribute definition (name, type, and options).
     *
     * @param id      the attribute ID
     * @param name    the new attribute name
     * @param type    the new attribute type
     * @param options the new allowed values (for DROPDOWN type)
     * @return the updated attribute entity
     * @throws IllegalArgumentException if the attribute is not found
     */
    OfferingAttributeEntity update(Long id, String name, AttributeType type, List<String> options);

    /**
     * Deletes an attribute definition if it is not used by any variant.
     *
     * @param id the attribute ID to delete
     * @throws IllegalArgumentException if the attribute is not found
     * @throws IllegalStateException    if the attribute is in use by variant attributes
     */
    void delete(Long id);
}

