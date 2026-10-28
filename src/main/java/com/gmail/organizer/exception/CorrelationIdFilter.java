package com.gmail.organizer.exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_UID_ATTRIBUTE = "requestUid";
    public static final String HEADER_NAME = "X-Request-UID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String incoming = request.getHeader(HEADER_NAME);
        // Do not trust arbitrary long or malformed IDs from callers.
        String requestUid = incoming != null && incoming.matches("[A-Za-z0-9._-]{1,64}")
                ? incoming : UUID.randomUUID().toString();

        request.setAttribute(REQUEST_UID_ATTRIBUTE, requestUid);
        response.setHeader(HEADER_NAME, requestUid);
        MDC.put("requestUid", requestUid);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestUid");
        }
    }
}
