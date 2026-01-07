package com.nifilili.business.service;

import java.util.List;

public interface BusinessCategoryService {
    void updateCategories(Long businessId, List<Long> categoryIds);
}
