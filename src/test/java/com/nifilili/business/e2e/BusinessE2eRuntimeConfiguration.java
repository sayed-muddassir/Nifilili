package com.nifilili.business.e2e;

import org.junit.jupiter.api.Assumptions;

/**
 * Runtime configuration for business happy-path E2E tests.
 *
 * Values are intentionally sourced from environment variables so the same test
 * package can run against local, QA, or staging without code changes.
 */
final class BusinessE2eRuntimeConfiguration {

    private static final String BASE_URL_ENV = "BUSINESS_E2E_BASE_URL";
    private static final String ADMIN_USERNAME_ENV = "BUSINESS_E2E_ADMIN_USERNAME";
    private static final String ADMIN_PASSWORD_ENV = "BUSINESS_E2E_ADMIN_PASSWORD";
    private static final String USER_USERNAME_ENV = "BUSINESS_E2E_USER_USERNAME";
    private static final String USER_PASSWORD_ENV = "BUSINESS_E2E_USER_PASSWORD";

    private BusinessE2eRuntimeConfiguration() {
    }

    static String baseUrl() {
        return requireEnv(BASE_URL_ENV);
    }

    static String adminUsername() {
        return requireEnv(ADMIN_USERNAME_ENV);
    }

    static String adminPassword() {
        return requireEnv(ADMIN_PASSWORD_ENV);
    }

    static String userUsername() {
        return requireEnv(USER_USERNAME_ENV);
    }

    static String userPassword() {
        return requireEnv(USER_PASSWORD_ENV);
    }

    private static String requireEnv(String variableName) {
        String value = System.getenv(variableName);
        Assumptions.assumeTrue(value != null && !value.isBlank(),
                () -> "Missing required E2E environment variable: " + variableName);
        return value;
    }
}
