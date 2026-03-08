package com.nifilili.kyc.repository;

import com.nifilili.kyc.domain.BusinessDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessDocumentRepository
        extends JpaRepository<BusinessDocument, Long> {

    List<BusinessDocument> findByBusinessId(Long businessId);

    java.util.Optional<BusinessDocument> findByIdAndBusinessId(Long id, Long businessId);
}
