package com.talentFlow.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        long startNanos = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            int status = response.getStatus();
            String method = request.getMethod();
            String path = request.getRequestURI();

            if (path.startsWith("/actuator/health")) {
                log.debug("HTTP {} {} -> {} ({} ms)", method, path, status, durationMs);
            } else if (status >= 500) {
                log.error("HTTP {} {} -> {} ({} ms)", method, path, status, durationMs);
            } else if (status >= 400) {
                log.warn("HTTP {} {} -> {} ({} ms)", method, path, status, durationMs);
            } else {
                log.info("HTTP {} {} -> {} ({} ms)", method, path, status, durationMs);
            }

            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String headerValue = request.getHeader(REQUEST_ID_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString();
    }
}
