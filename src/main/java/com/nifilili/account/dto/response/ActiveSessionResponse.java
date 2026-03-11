package com.nifilili.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ActiveSessionResponse", description = "An active user session (refresh token) with device info.")
public class ActiveSessionResponse {

    @Schema(description = "Session ID (refresh token database ID).", example = "123456789")
    private Long sessionId;

    @Schema(description = "Device name or user agent summary.", example = "Chrome on MacOS")
    private String deviceName;

    @Schema(description = "IP address of the session.", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "When the session was created.", example = "2026-03-10T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Whether this is the current calling session.", example = "true")
    private boolean current;
}
