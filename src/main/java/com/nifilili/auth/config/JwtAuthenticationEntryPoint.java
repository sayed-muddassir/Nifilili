package com.nifilili.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifilili.core.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                       ObjectMapper objectMapper) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        if (!hasHandler(request)) {
            log.warn("No handler found for '{}'", request.getRequestURI());
            writeError(response, request, HttpServletResponse.SC_NOT_FOUND, "Not Found", "URL not found");
            return;
        }

        log.warn("Unauthorized access to '{}': {}", request.getRequestURI(), authException.getMessage());
        writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication is required to access this resource");
    }

    private boolean hasHandler(HttpServletRequest request) {
        try {
            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
            return handler != null;
        } catch (Exception ex) {
            log.debug("Could not resolve handler for '{}': {}", request.getRequestURI(), ex.getMessage());
            return true;
        }
    }

    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            int status,
                            String error,
                            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorDto(
                OffsetDateTime.now().toString(),
                status,
                error,
                message,
                request.getRequestURI()
        ));
    }
}
