package com.nifilili.auth.repository;

import com.nifilili.auth.domain.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    /**
     * Fetches all permission names associated with the given role IDs.
     *
     * @param roleIds set of role primary keys
     * @return distinct permission names (e.g. "BUSINESS_CREATE", "ORDER_PLACE")
     */
    @Query("SELECT p.name FROM Role r JOIN r.permissions p WHERE r.id IN :roleIds")
    Set<String> findPermissionNamesByRoleIds(@Param("roleIds") Set<Long> roleIds);
}
