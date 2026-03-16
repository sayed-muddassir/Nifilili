package com.nifilili.account.controller.owner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.account.dto.request.LinkEmailRequest;
import com.nifilili.account.dto.request.LinkPhoneRequest;
import com.nifilili.account.dto.request.VerifyLinkRequest;
import com.nifilili.account.service.IdentityLinkService;
import com.nifilili.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountIdentityControllerTest {

    @Mock
    private IdentityLinkService identityLinkService;

    @InjectMocks
    private AccountIdentityController accountIdentityController;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(accountIdentityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // linkPhone
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void linkPhone_WhenValidPhone_ShouldReturn200() throws Exception {
        LinkPhoneRequest request = new LinkPhoneRequest("+977-9800000000");

        mvc.perform(post("/api/v1/account/identity/link/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to phone number"));

        verify(identityLinkService).initiateLinkPhone("+977-9800000000");
    }

    @Test
    void linkPhone_WhenPhoneBlank_ShouldReturn400() throws Exception {
        LinkPhoneRequest request = new LinkPhoneRequest("");

        mvc.perform(post("/api/v1/account/identity/link/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).initiateLinkPhone(anyString());
    }

    @Test
    void linkPhone_WhenPhoneInvalidFormat_ShouldReturn400() throws Exception {
        LinkPhoneRequest request = new LinkPhoneRequest("invalid-phone!!!");

        mvc.perform(post("/api/v1/account/identity/link/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).initiateLinkPhone(anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyLinkPhone
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void verifyLinkPhone_WhenValidRequest_ShouldReturn200() throws Exception {
        VerifyLinkRequest request = new VerifyLinkRequest("+977-9800000000", "123456");

        mvc.perform(post("/api/v1/account/identity/link/phone/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Phone number linked successfully"));

        verify(identityLinkService).completeLinkPhone("+977-9800000000", "123456");
    }

    @Test
    void verifyLinkPhone_WhenOtpBlank_ShouldReturn400() throws Exception {
        VerifyLinkRequest request = new VerifyLinkRequest("+977-9800000000", "");

        mvc.perform(post("/api/v1/account/identity/link/phone/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).completeLinkPhone(anyString(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // linkEmail
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void linkEmail_WhenValidEmail_ShouldReturn200() throws Exception {
        LinkEmailRequest request = new LinkEmailRequest("new@nifilili.com");

        mvc.perform(post("/api/v1/account/identity/link/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to email address"));

        verify(identityLinkService).initiateLinkEmail("new@nifilili.com");
    }

    @Test
    void linkEmail_WhenEmailBlank_ShouldReturn400() throws Exception {
        LinkEmailRequest request = new LinkEmailRequest("");

        mvc.perform(post("/api/v1/account/identity/link/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).initiateLinkEmail(anyString());
    }

    @Test
    void linkEmail_WhenEmailInvalid_ShouldReturn400() throws Exception {
        LinkEmailRequest request = new LinkEmailRequest("not-an-email");

        mvc.perform(post("/api/v1/account/identity/link/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).initiateLinkEmail(anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyLinkEmail
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void verifyLinkEmail_WhenValidRequest_ShouldReturn200() throws Exception {
        VerifyLinkRequest request = new VerifyLinkRequest("new@nifilili.com", "654321");

        mvc.perform(post("/api/v1/account/identity/link/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email address linked successfully"));

        verify(identityLinkService).completeLinkEmail("new@nifilili.com", "654321");
    }

    @Test
    void verifyLinkEmail_WhenIdentifierBlank_ShouldReturn400() throws Exception {
        VerifyLinkRequest request = new VerifyLinkRequest("", "654321");

        mvc.perform(post("/api/v1/account/identity/link/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(identityLinkService, never()).completeLinkEmail(anyString(), anyString());
    }
}
