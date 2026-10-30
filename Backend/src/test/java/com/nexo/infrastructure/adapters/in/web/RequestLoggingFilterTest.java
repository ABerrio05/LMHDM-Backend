package com.nexo.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.nexo.application.ports.in.AuditLogUseCase;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {
    private final AuditLogUseCase auditLogs = mock(AuditLogUseCase.class);
    private final RequestLoggingFilter filter = new RequestLoggingFilter(auditLogs);

    @Test
    void propagatesProvidedRequestIdToTheResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader("X-Request-Id", "review-request-42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("review-request-42", response.getHeader("X-Request-Id"));
    }

    @Test
    void createsRequestIdWhenClientDoesNotProvideOne() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertFalse(response.getHeader("X-Request-Id").isBlank());
    }

    @Test
    void persistsOnlyTheAuthenticatedUsersApiActivity() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/tasks");
        request.setAttribute(JwtAuthenticationFilter.AUTHENTICATED_USER_ID, 9L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(auditLogs).record(eq(9L), eq("POST /api/tasks"), eq("POST"), eq("/api/tasks"),
                eq(200), anyLong(), org.mockito.ArgumentMatchers.anyString());
    }
}
