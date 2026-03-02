package com.nifilili.offering.service;

import com.nifilili.offering.domain.OfferingCategoryEntity;

import java.util.List;


public interface OfferingCategoryService {

    /**
     * Creates a new offering category, optionally as a child of an existing parent.
     *
     * @param name             the category name
     * @param parentCategoryId the parent category ID (nullable for root categories)
     * @return the persisted category entity
     * @throws IllegalArgumentException if the parent category is not found
     */
    OfferingCategoryEntity create(String name, Long parentCategoryId);

    /** Returns all root-level categories (those without a parent). */

    List<OfferingCategoryEntity> getTree();

    /**
     * Validates that the given category is a leaf (has no children) and returns it.
     *
     * @param categoryId the category ID to validate
     * @return the leaf category entity
     * @throws IllegalStateException    if the category has children
     * @throws IllegalArgumentException if the category is not found
     */
    OfferingCategoryEntity validateLeafCategory(Long categoryId);

    /**
     * Updates the name of an existing category.
     *
     * @param id   the category ID
     * @param name the new category name
     * @return the updated category entity
     * @throws IllegalArgumentException if the category is not found
     */
    OfferingCategoryEntity update(Long id, String name);

    /**
     * Deletes a category if it has no children and no offerings are using it.
     *
     * @param id the category ID to delete
     * @throws IllegalArgumentException if the category is not found
     * @throws IllegalStateException    if the category has children or offerings assigned
     */
    void delete(Long id);
}

