package com.nifilili.auth.repository;

import com.nifilili.auth.domain.OtpToken;
import com.nifilili.auth.domain.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findFirstByIdentifierAndOtpAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String identifier, String otp, OtpPurpose purpose);

    long countByIdentifierAndPurposeAndCreatedAtAfter(
            String identifier, OtpPurpose purpose, LocalDateTime after);
}
