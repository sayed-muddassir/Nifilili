package com.nifilili.account.controller.owner;

import com.nifilili.account.dto.response.ActiveSessionResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.service.AccountSessionService;
import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = SwaggerConstants.ACCOUNT_3, description = "Device session management and login history.")
public class AccountSessionController {

    private final AccountSessionService accountSessionService;

    @Operation(summary = "List Active Sessions",
            description = "Lists all active sessions (devices) for the current user. "
                    + "Pass the current refresh token via X-Refresh-Token header to identify the calling session.")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping
    public ResponseEntity<List<ActiveSessionResponse>> getActiveSessions(
            @Parameter(description = "Current refresh token to mark as 'current' session")
            @RequestHeader(value = "X-Refresh-Token", required = false) String currentRefreshToken) {
        log.debug("Get active sessions request");
        return ResponseEntity.ok(accountSessionService.getActiveSessions(currentRefreshToken));
    }

    @Operation(summary = "Revoke Session",
            description = "Revokes a specific session by its ID, ending that device's access.")
    @ApiResponse(responseCode = "204", description = "Session revoked")
    @ApiResponse(responseCode = "404", description = "Session not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revokeSession(@PathVariable Long sessionId) {
        log.info("Revoke session request for sessionId={}", sessionId);
        accountSessionService.revokeSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Login History",
            description = "Returns paginated login history for the current user.")
    @ApiResponse(responseCode = "200", description = "History retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/history")
    public ResponseEntity<Page<LoginHistoryResponse>> getLoginHistory(
            @PageableDefault(size = 20, sort = "loggedInAt") Pageable pageable) {
        log.debug("Get login history request");
        return ResponseEntity.ok(accountSessionService.getLoginHistory(pageable));
    }
}
