package com.example.pitchboxd.global.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.pitchboxd.global.security.UserAdaptor;
import com.example.pitchboxd.user.domain.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

public class MdcLoggingFilterTest {

    @Test
    void testFilterPopulatesAndClearsMdc() throws Exception {
        MdcLoggingFilter filter = new MdcLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = (req, res) -> {
            assertNotNull(MDC.get("traceId"));
            assertEquals("127.0.0.1", MDC.get("ip"));
            assertEquals("anonymous", MDC.get("userId"));
        };
        
        filter.doFilter(request, response, filterChain);
        
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("ip"));
        assertNull(MDC.get("userId"));
    }

    @Test
    void testFilterPopulatesMdcForAuthenticatedUser() throws Exception {
        MdcLoggingFilter filter = new MdcLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        UserAdaptor userAdaptor = new UserAdaptor(user);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(userAdaptor, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        try {
            FilterChain filterChain = (req, res) -> {
                assertEquals("42", MDC.get("userId"));
            };
            filter.doFilter(request, response, filterChain);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testFilterResolvesProxyIp() throws Exception {
        MdcLoggingFilter filter = new MdcLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = (req, res) -> {
            assertEquals("203.0.113.195", MDC.get("ip"));
        };
        
        filter.doFilter(request, response, filterChain);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFilterExclusionLogging() throws Exception {
        MdcLoggingFilter filter = new MdcLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        when(handlerMethod.hasMethodAnnotation(LoggingExclude.class)).thenReturn(true);
        
        request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, handlerMethod);
        
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilter(request, response, filterChain);
        
        verify(filterChain).doFilter(any(), any());
    }
}
