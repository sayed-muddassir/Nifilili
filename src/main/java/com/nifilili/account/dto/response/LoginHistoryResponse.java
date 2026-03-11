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
@Schema(name = "LoginHistoryResponse", description = "A login history entry for audit trail.")
public class LoginHistoryResponse {

    @Schema(description = "IP address.", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Browser user agent.")
    private String userAgent;

    @Schema(description = "Device name.", example = "Chrome on MacOS")
    private String deviceName;

    @Schema(description = "Login timestamp.", example = "2026-03-10T14:30:00")
    private LocalDateTime loggedInAt;
}
