package com.nifilili.quote.repository;

import com.nifilili.core.enums.quote.QuoteStatus;
import com.nifilili.quote.domain.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {

    List<QuoteEntity> findByRequestIdOrderByIdDesc(Long requestId);

    Optional<QuoteEntity> findFirstByRequestIdOrderByIdDesc(Long requestId);

    boolean existsByRequestIdAndStatusIn(Long requestId, List<QuoteStatus> statuses);

    List<QuoteEntity> findByStatusAndValidUntilBefore(QuoteStatus status, LocalDateTime now);
}
