package com.nifilili.business.controller.publicapi;

import com.nifilili.business.dto.response.CategoryResponse;
import com.nifilili.business.dto.response.VerticalResponse;
import com.nifilili.business.service.CategoryDefinitionService;
import com.nifilili.business.service.VerticalDefinitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BusinessLookupControllerTest {

    @Mock
    private CategoryDefinitionService categoryDefinitionService;
    @Mock
    private VerticalDefinitionService verticalDefinitionService;

    @InjectMocks
    private BusinessLookupController businessLookupController;

    @Test
    void getAllActiveVerticals_WhenServiceReturnsList_ShouldReturnSameList() {
        List<VerticalResponse> verticals = List.of(new VerticalResponse());
        when(verticalDefinitionService.getAllActive()).thenReturn(verticals);

        assertSame(verticals, businessLookupController.getAllActiveVerticals());
    }

    @Test
    void getCategoriesByVertical_WhenServiceReturnsList_ShouldReturnSameList() {
        List<CategoryResponse> categories = List.of(new CategoryResponse());
        when(categoryDefinitionService.getByVertical(11L)).thenReturn(categories);

        assertSame(categories, businessLookupController.getCategoriesByVertical(11L));
    }

    @Test
    void getAllCategories_WhenServiceReturnsList_ShouldReturnSameList() {
        List<CategoryResponse> categories = List.of(new CategoryResponse());
        when(categoryDefinitionService.getAll()).thenReturn(categories);

        assertSame(categories, businessLookupController.getAllCategories());
    }


}
