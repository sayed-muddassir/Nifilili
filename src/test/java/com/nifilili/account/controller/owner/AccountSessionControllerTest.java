package com.nifilili.account.controller.owner;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.account.dto.response.ActiveSessionResponse;
import com.nifilili.account.dto.response.LoginHistoryResponse;
import com.nifilili.account.service.AccountSessionService;
import com.nifilili.core.exception.GlobalExceptionHandler;
import com.nifilili.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountSessionControllerTest {

    @Mock
    private AccountSessionService accountSessionService;

    @InjectMocks
    private AccountSessionController accountSessionController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(accountSessionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getActiveSessions
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getActiveSessions_WhenCalled_ShouldReturn200WithSessions() throws Exception {
        ActiveSessionResponse session = ActiveSessionResponse.builder()
                .sessionId(1L)
                .deviceName("Chrome on MacOS")
                .ipAddress("192.168.1.1")
                .createdAt(LocalDateTime.of(2026, 3, 10, 14, 30))
                .current(true)
                .build();
        when(accountSessionService.getActiveSessions("my-refresh-token")).thenReturn(List.of(session));

        mvc.perform(get("/api/v1/account/sessions")
                        .header("X-Refresh-Token", "my-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(1))
                .andExpect(jsonPath("$[0].deviceName").value("Chrome on MacOS"))
                .andExpect(jsonPath("$[0].current").value(true));

        verify(accountSessionService).getActiveSessions("my-refresh-token");
    }

    @Test
    void getActiveSessions_WhenNoRefreshTokenHeader_ShouldReturn200() throws Exception {
        when(accountSessionService.getActiveSessions(isNull())).thenReturn(List.of());

        mvc.perform(get("/api/v1/account/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(accountSessionService).getActiveSessions(isNull());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // revokeSession
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void revokeSession_WhenSessionExists_ShouldReturn204() throws Exception {
        doNothing().when(accountSessionService).revokeSession(1L);

        mvc.perform(delete("/api/v1/account/sessions/1"))
                .andExpect(status().isNoContent());

        verify(accountSessionService).revokeSession(1L);
    }

    @Test
    void revokeSession_WhenSessionNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Session not found")).when(accountSessionService).revokeSession(999L);

        mvc.perform(delete("/api/v1/account/sessions/999"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLoginHistory
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getLoginHistory_WhenCalled_ShouldReturn200WithPage() throws Exception {
        LoginHistoryResponse historyEntry = LoginHistoryResponse.builder()
                .ipAddress("192.168.1.1")
                .userAgent("Chrome/120")
                .deviceName("Chrome on MacOS")
                .loggedInAt(LocalDateTime.of(2026, 3, 10, 14, 30))
                .build();
        Page<LoginHistoryResponse> page = new PageImpl<>(List.of(historyEntry), PageRequest.of(0, 20), 1);
        when(accountSessionService.getLoginHistory(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/api/v1/account/sessions/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$.content[0].deviceName").value("Chrome on MacOS"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
