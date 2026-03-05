package com.nifilili.order.controller.admin;

import com.nifilili.core.exception.ResourceNotFoundException;
import com.nifilili.order.domain.PaymentTypeEntity;
import com.nifilili.order.dto.response.PaymentTypeResponse;
import com.nifilili.order.repository.PaymentTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentTypeControllerTest {

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @InjectMocks
    private AdminPaymentTypeController adminPaymentTypeController;

    @Test
    void listPaymentTypes_WhenRepositoryReturnsEntities_ShouldReturnMappedResponses() {
        PaymentTypeEntity entity = new PaymentTypeEntity();
        entity.setName("COD");
        entity.setDescription("Cash on delivery");
        entity.setIsActive(true);
        when(paymentTypeRepository.findByIsActiveTrue()).thenReturn(List.of(entity));

        ResponseEntity<List<PaymentTypeResponse>> result = adminPaymentTypeController.listPaymentTypes();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("COD", result.getBody().get(0).getName());
        assertEquals("Cash on delivery", result.getBody().get(0).getDescription());
    }

    @Test
    void deactivate_WhenPaymentTypeExists_ShouldDeactivateAndReturnNoContent() {
        PaymentTypeEntity entity = new PaymentTypeEntity();
        entity.setIsActive(true);
        entity.setUpdatedAt(LocalDateTime.now());
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentTypeRepository.save(entity)).thenReturn(entity);

        ResponseEntity<Void> result = adminPaymentTypeController.deactivate(1L);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertFalse(entity.getIsActive());
        verify(paymentTypeRepository).save(entity);
    }

    @Test
    void deactivate_WhenPaymentTypeNotFound_ShouldThrowResourceNotFoundException() {
        when(paymentTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminPaymentTypeController.deactivate(99L));
    }
}
