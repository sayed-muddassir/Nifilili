/**
 * Account module: user profile management, email verification, password reset,
 * device session management, and account security operations.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"auth", "core", "business :: events"}
)
package com.nifilili.account;
