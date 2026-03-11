package com.nifilili.auth.repository;

import com.nifilili.auth.domain.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findByUserIdOrderByLoggedInAtDesc(Long userId, Pageable pageable);
}
