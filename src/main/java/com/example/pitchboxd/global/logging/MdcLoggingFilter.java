package com.example.pitchboxd.global.logging;

import com.example.pitchboxd.global.security.UserAdaptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    // Precompiled regex pattern that safely handles escaped quotes inside JSON strings
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "\"(password|passwordConfirm|oldPassword|newPassword|token|accessToken|refreshToken|secret)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"?"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") 
            || path.startsWith("/swagger-ui") 
            || path.startsWith("/swagger-resources") 
            || path.startsWith("/webjars") 
            || path.equals("/favicon.ico") 
            || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        
        String contentType = request.getContentType();
        boolean shouldCache = contentType != null && contentType.toLowerCase().contains("application/json");
        // Limit cache size to 10KB to prevent OOM on large payloads
        HttpServletRequest wrappedRequest = shouldCache ? new ContentCachingRequestWrapper(request, 10240) : request;

        try {
            // Use full UUID without hyphens for high entropy trace ID
            String traceId = UUID.randomUUID().toString().replace("-", "");
            String ip = getClientIp(request);
            
            // Set MDC attributes inside try block so they are guaranteed to clear in finally block
            MDC.put("traceId", traceId);
            MDC.put("ip", ip);
            MDC.put("userId", "anonymous");

            // Expose trace ID to the client response header
            response.setHeader("X-Trace-Id", traceId);

            // Initial security context check
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserAdaptor userAdaptor) {
                MDC.put("userId", String.valueOf(userAdaptor.getUserId()));
            }

            filterChain.doFilter(wrappedRequest, response);
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                
                // Re-check authentication context in case the user authenticated during request execution
                Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();
                if (finalAuth != null && finalAuth.getPrincipal() instanceof UserAdaptor userAdaptor) {
                    MDC.put("userId", String.valueOf(userAdaptor.getUserId()));
                }

                // If @LoggingExclude is present, log request metadata but mask the request body
                boolean excludeLogging = isExcludeLogging(request);
                String body;
                if (excludeLogging) {
                    body = "[Protected Payload]";
                } else {
                    body = shouldCache ? getRequestBody((ContentCachingRequestWrapper) wrappedRequest) : "[Non-JSON Payload]";
                }

                log.info("Request API: [{}] {} | Status: {} | Body: {} | Time: {}ms", 
                        request.getMethod(), request.getRequestURI(), response.getStatus(), body, duration);
            } catch (Exception e) {
                log.error("Error occurred in MdcLoggingFilter logging block", e);
            } finally {
                MDC.clear();
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] ipHeaders = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isExcludeLogging(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.hasMethodAnnotation(LoggingExclude.class) 
                || AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), LoggingExclude.class) != null;
        }
        return false;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            String body = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
            // Mask sensitive data first before truncating to avoid partial quotes issues
            body = maskSensitiveData(body);
            if (body.length() > 1000) {
                body = body.substring(0, 1000) + "...(truncated)";
            }
            return body.replaceAll("\\s+", " ");
        }
        return "";
    }

    private String maskSensitiveData(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SENSITIVE_PATTERN.matcher(json).replaceAll("\"$1\":\"***\"");
    }
}
