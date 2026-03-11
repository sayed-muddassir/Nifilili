package com.nifilili.core.email;

/**
 * Abstraction for sending emails. Swap the implementation by providing
 * a {@code @Primary} bean (e.g. SmtpEmailService, SendGridEmailService).
 */
public interface EmailService {

    /**
     * Sends an email verification link to the user.
     *
     * @param toEmail recipient email address
     * @param token   verification token (UUID)
     */
    void sendVerificationEmail(String toEmail, String token);

    /**
     * Sends a password reset link to the user.
     *
     * @param toEmail recipient email address
     * @param token   reset token (UUID)
     */
    void sendPasswordResetLink(String toEmail, String token);

    /**
     * Sends a password reset OTP to the user.
     *
     * @param toEmail recipient email address
     * @param otp     6-digit OTP code
     */
    void sendPasswordResetOtp(String toEmail, String otp);

    /**
     * Sends a generic email message.
     *
     * @param message the email message to send
     */
    void send(EmailMessage message);
}
