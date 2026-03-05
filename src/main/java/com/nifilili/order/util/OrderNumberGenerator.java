package com.nifilili.order.util;

import java.util.UUID;

/**
 * Generates unique identifiers for orders, invoices, and return authorizations.
 */
public final class OrderNumberGenerator {

    private OrderNumberGenerator() {
        // utility class
    }

    /**
     * Generates a unique order number with prefix ORD-.
     *
     * @return order number in format ORD-xxxxxxxx
     */
    public static String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generates a unique invoice number with prefix INV-.
     *
     * @return invoice number in format INV-{timestamp}
     */
    public static String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    /**
     * Generates a unique RMA (Return Merchandise Authorization) number.
     *
     * @return RMA number in format RMA-xxxxxxxx
     */
    public static String generateRmaNumber() {
        return "RMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
