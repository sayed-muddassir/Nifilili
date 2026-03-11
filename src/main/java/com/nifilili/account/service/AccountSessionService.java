package com.nifilili.account.service;

import com.nifilili.account.dto.response.ActiveSessionResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Manages user session viewing, revocation, and login history.
 */
public interface AccountSessionService {

    /**
     * Lists all active sessions (non-revoked, non-expired refresh tokens) for the current user.
     *
     * @param currentRefreshToken the refresh token of the calling session (to mark as "current")
     * @return list of active sessions
     */
    List<ActiveSessionResponse> getActiveSessions(String currentRefreshToken);

    /**
     * Revokes a specific session by its database ID.
     *
     * @param sessionId the refresh token database ID
     */
    void revokeSession(Long sessionId);

    /**
     * Returns paginated login history for the current user.
     *
     * @param pageable pagination parameters
     * @return page of login history entries
     */
    Page<LoginHistoryResponse> getLoginHistory(Pageable pageable);
}
