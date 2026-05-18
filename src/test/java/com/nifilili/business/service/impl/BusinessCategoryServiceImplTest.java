package com.nifilili.business.service.impl;

import com.nifilili.business.SecurityContextTestUtil;
import com.nifilili.business.domain.Business;
import com.nifilili.business.domain.CategoryDefinition;
import com.nifilili.business.repository.BusinessCategoryRepository;
import com.nifilili.business.repository.BusinessRepository;
import com.nifilili.business.repository.CategoryRepository;
import com.nifilili.core.enums.business.BusinessStatus;
import com.nifilili.core.exception.InvalidBusinessStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessCategoryServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BusinessCategoryRepository businessCategoryRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BusinessCategoryServiceImpl businessCategoryService;

    @AfterEach
    void tearDown() {
        SecurityContextTestUtil.clearContext();
    }

    @Test
    void updateCategories_WhenValidOwnedBusinessAndCategories_ShouldReplaceMappings() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 10L);
        business.setOwnerUserId(50L);
        business.setVerticalId(1L);
        business.setStatus(BusinessStatus.PUBLISHED);

        CategoryDefinition category = new CategoryDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(category, 100L);
        category.setBusinessVerticalId(1L);
        category.setActiveStatus(true);

        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        SecurityContextTestUtil.setAuthenticatedUser(50L);
        businessCategoryService.updateCategories(10L, List.of(100L));

        verify(businessCategoryRepository).deleteByBusinessId(10L);
        verify(businessCategoryRepository).save(any());
    }

    @Test
    void updateCategories_WhenDuplicateCategoriesProvided_ShouldThrowValidationError() {
        Business business = new Business();
        com.nifilili.business.TestEntityIdUtil.withId(business, 10L);
        business.setOwnerUserId(50L);
        business.setVerticalId(1L);
        business.setStatus(BusinessStatus.PUBLISHED);

        CategoryDefinition category = new CategoryDefinition();
        com.nifilili.business.TestEntityIdUtil.withId(category, 100L);
        category.setBusinessVerticalId(1L);
        category.setActiveStatus(true);

        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        SecurityContextTestUtil.setAuthenticatedUser(50L);
        assertThrows(InvalidBusinessStateException.class,
                () -> businessCategoryService.updateCategories(10L, List.of(100L, 100L)));

        verify(businessCategoryRepository, never()).save(any());
    }
}
