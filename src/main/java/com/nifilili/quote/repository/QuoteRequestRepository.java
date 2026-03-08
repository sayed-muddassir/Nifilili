package com.nifilili.quote.repository;

import com.nifilili.core.enums.quote.QuoteRequestStatus;
import com.nifilili.quote.domain.QuoteRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequestEntity, Long> {

    List<QuoteRequestEntity> findByUserIdOrderByIdDesc(Long userId);

    List<QuoteRequestEntity> findByBusinessIdOrderByIdDesc(Long businessId);

    List<QuoteRequestEntity> findByStatusAndExpiredAtBefore(QuoteRequestStatus status, LocalDateTime now);
}
