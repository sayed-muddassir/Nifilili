package com.nifilili.core.config;

import com.nifilili.auth.config.JwtAuthenticationEntryPoint;
import com.nifilili.auth.config.JwtAuthenticationFilter;
import com.nifilili.auth.config.JwtTokenProvider;
import com.nifilili.auth.config.RestAccessDeniedHandler;
import com.nifilili.business.controller.publicapi.BusinessQueryController;
import com.nifilili.business.service.BusinessQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = BusinessQueryController.class)
@Import({
        CorsConfig.class,
        com.nifilili.auth.config.SpringSecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String DISALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessQueryService businessQueryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(businessQueryService.getAllBusinessesByStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of()));
        given(jwtTokenProvider.validateToken(org.mockito.ArgumentMatchers.anyString())).willReturn(false);
    }

    @Test
    void preflight_WhenOriginAllowed_ShouldReturnCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/public/businesses")
                        .header(ORIGIN, ALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,X-Refresh-Token"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS, containsString("X-Refresh-Token")));
    }

    @Test
    void preflight_WhenOriginDisallowed_ShouldRejectRequest() throws Exception {
        mockMvc.perform(options("/api/v1/public/businesses")
                        .header(ORIGIN, DISALLOWED_ORIGIN)
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void get_WhenOriginAllowed_ShouldExposeAllowOriginHeader() throws Exception {
        mockMvc.perform(get("/api/v1/public/businesses")
                        .header(ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }
}
