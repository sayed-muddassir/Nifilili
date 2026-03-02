package com.nifilili.quote.repository;

import com.nifilili.quote.domain.QuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    List<QuoteRequest> findByBusinessId(Long businessId);

    List<QuoteRequest> findByBusinessIdOrderByIdDesc(Long businessId);

    List<QuoteRequest> findByUserIdOrderByIdDesc(Long userId);

    List<QuoteRequest> findByStatusAndExpiredAtBefore(
            com.nifilili.core.enums.quote.QuoteRequestStatus status,
            java.time.LocalDateTime expiredAt
    );
}
