package com.nifilili.order.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculatorTest {

    @Test
    void calculate_WhenValidInputs_ShouldReturnCorrectTax() {
        BigDecimal result = TaxCalculator.calculate(new BigDecimal("1000.00"), 13);
        assertEquals(new BigDecimal("130.00"), result);
    }

    @Test
    void calculate_WhenTaxRateIsZero_ShouldReturnZero() {
        BigDecimal result = TaxCalculator.calculate(new BigDecimal("1000.00"), 0);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculate_WhenAmountIsNull_ShouldReturnZero() {
        BigDecimal result = TaxCalculator.calculate(null, 13);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculate_WhenAmountIsNegative_ShouldReturnZero() {
        BigDecimal result = TaxCalculator.calculate(new BigDecimal("-100.00"), 13);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculate_WhenFractionalResult_ShouldRoundHalfUp() {
        BigDecimal result = TaxCalculator.calculate(new BigDecimal("33.33"), 13);
        assertEquals(new BigDecimal("4.33"), result);
    }
}
