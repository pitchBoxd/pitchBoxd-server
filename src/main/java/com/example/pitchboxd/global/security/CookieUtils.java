package com.example.pitchboxd.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jackson2.SecurityJackson2Modules;

@Slf4j
public class CookieUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        ClassLoader loader = CookieUtils.class.getClassLoader();
        objectMapper.registerModules(SecurityJackson2Modules.getModules(loader));
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
        log.info("Cookie saved to response: {} (value length: {})", name, value.length());
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    cookie.setSecure(request.isSecure());
                    response.addCookie(cookie);
                    log.info("Cookie deleted from response: {}", name);
                }
            }
        }
    }

    public static String serialize(Object object) {
        try {
            return Base64.getUrlEncoder()
                    .encodeToString(objectMapper.writeValueAsBytes(object));
        } catch (Exception e) {
            log.error("Failed to serialize object", e);
            throw new IllegalArgumentException("Failed to serialize object", e);
        }
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());
            return objectMapper.readValue(decoded, cls);
        } catch (Exception e) {
            log.error("Failed to deserialize cookie", e);
            throw new IllegalArgumentException("Failed to deserialize cookie", e);
        }
    }
}
