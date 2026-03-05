package com.nifilili.order.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderNumberGeneratorTest {

    @Test
    void generateOrderNumber_ShouldReturnStringWithOrdPrefix() {
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        assertNotNull(orderNumber);
        assertTrue(orderNumber.startsWith("ORD-"));
        assertEquals(12, orderNumber.length()); // "ORD-" + 8 uppercase hex chars
    }

    @Test
    void generateOrderNumber_ShouldReturnUniqueValues() {
        String first = OrderNumberGenerator.generateOrderNumber();
        String second = OrderNumberGenerator.generateOrderNumber();
        assertNotEquals(first, second);
    }

    @Test
    void generateInvoiceNumber_ShouldReturnStringWithInvPrefix() {
        String invoiceNumber = OrderNumberGenerator.generateInvoiceNumber();
        assertNotNull(invoiceNumber);
        assertTrue(invoiceNumber.startsWith("INV-"));
    }

    @Test
    void generateInvoiceNumber_ShouldContainTimestamp() {
        long before = System.currentTimeMillis();
        String invoiceNumber = OrderNumberGenerator.generateInvoiceNumber();
        long after = System.currentTimeMillis();

        String timestampStr = invoiceNumber.substring(4);
        long timestamp = Long.parseLong(timestampStr);
        assertTrue(timestamp >= before && timestamp <= after);
    }

    @Test
    void generateRmaNumber_ShouldReturnStringWithRmaPrefix() {
        String rmaNumber = OrderNumberGenerator.generateRmaNumber();
        assertNotNull(rmaNumber);
        assertTrue(rmaNumber.startsWith("RMA-"));
        assertEquals(12, rmaNumber.length()); // "RMA-" + 8 uppercase hex chars
    }

    @Test
    void generateRmaNumber_ShouldReturnUniqueValues() {
        String first = OrderNumberGenerator.generateRmaNumber();
        String second = OrderNumberGenerator.generateRmaNumber();
        assertNotEquals(first, second);
    }
}
