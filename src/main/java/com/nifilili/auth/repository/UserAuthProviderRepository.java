package com.nifilili.auth.repository;

import com.nifilili.auth.domain.UserAuthProvider;
import com.nifilili.auth.domain.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {

    Optional<UserAuthProvider> findByUserIdAndProviderType(Long userId, ProviderType providerType);

    boolean existsByUserIdAndProviderType(Long userId, ProviderType providerType);
}
