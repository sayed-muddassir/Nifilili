package com.nifilili.account.service;

/**
 * Manages identity linking: allows users to add additional
 * authentication methods (phone, email) to their existing account.
 */
public interface IdentityLinkService {

    /**
     * Initiates phone linking by sending an OTP to the specified phone number.
     *
     * @param phone phone number to link
     * @throws com.nifilili.core.exception.PhoneAlreadyExistsException if phone is already registered
     */
    void initiateLinkPhone(String phone);

    /**
     * Completes phone linking after OTP verification.
     *
     * @param phone the phone number being linked
     * @param otp   the OTP code entered by the user
     * @throws com.nifilili.core.exception.InvalidTokenException if OTP is invalid or expired
     */
    void completeLinkPhone(String phone, String otp);

    /**
     * Initiates email linking by sending a verification OTP to the specified email.
     *
     * @param email email address to link
     * @throws com.nifilili.core.exception.EmailAlreadyExistsException if email is already registered
     */
    void initiateLinkEmail(String email);

    /**
     * Completes email linking after OTP verification.
     *
     * @param email the email address being linked
     * @param otp   the OTP code entered by the user
     * @throws com.nifilili.core.exception.InvalidTokenException if OTP is invalid or expired
     */
    void completeLinkEmail(String email, String otp);
}
