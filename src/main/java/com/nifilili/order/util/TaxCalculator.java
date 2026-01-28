package com.nifilili.order.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * TaxCalculator calculates tax based on taxable amount.
 *
 * Assumes:
 * - tax is percentage-based
 * - tax applies AFTER discount
 */
public final class TaxCalculator {

    private TaxCalculator() {
        // Utility class
    }

    /**
     * Calculate tax amount.
     *
     * @param taxableAmount amount after discount
     * @param taxRate       tax rate in percentage (e.g. 13)
     */
    public static BigDecimal calculate(
            BigDecimal taxableAmount,
            int taxRate
    ) {

        if (taxRate <= 0) {
            return BigDecimal.ZERO;
        }

        if (taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return taxableAmount
                .multiply(BigDecimal.valueOf(taxRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}

