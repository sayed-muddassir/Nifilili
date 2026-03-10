package com.nifilili.business.api.impl;

import com.nifilili.business.repository.BusinessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessValidationImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessValidationImpl businessValidationService;

    @Test
    void existsAndActive_WhenRepositoryReturnsTrue_ShouldReturnTrue() {
        when(businessRepository.existsById(1L)).thenReturn(true);
        assertTrue(businessValidationService.existsAndActive(1L));
    }

    @Test
    void existsAndActive_WhenRepositoryReturnsFalse_ShouldReturnFalse() {
        when(businessRepository.existsById(2L)).thenReturn(false);
        assertFalse(businessValidationService.existsAndActive(2L));
    }
}
