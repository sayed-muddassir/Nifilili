package com.nifilili.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class RestAccessDeniedHandlerTest {

    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(new ObjectMapper());

    @Test
    void handle_WhenAccessDenied_ShouldReturn403WithJsonPayload() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access denied"));

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains("\"error\":\"Forbidden\""));
        assertTrue(body.contains("You do not have permission to access this resource"));
        assertTrue(body.contains("/api/v1/admin/users"));
    }
}
