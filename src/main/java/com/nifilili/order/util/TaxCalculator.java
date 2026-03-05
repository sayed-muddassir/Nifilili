package com.nifilili.order.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates tax amount based on a taxable amount and tax rate percentage.
 */
public final class TaxCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private TaxCalculator() {
        // utility class
    }

    /**
     * Calculates tax for a given taxable amount.
     *
     * @param taxableAmount the amount to apply tax on
     * @param taxRate       the tax rate as a whole number (e.g. 13 for 13%)
     * @return the calculated tax, rounded to 2 decimal places (HALF_UP)
     */
    public static BigDecimal calculate(BigDecimal taxableAmount, int taxRate) {
        if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0 || taxRate <= 0) {
            return BigDecimal.ZERO;
        }
        return taxableAmount
                .multiply(BigDecimal.valueOf(taxRate))
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
