package com.example.pitchboxd.global.security;

import com.example.pitchboxd.auth.application.TokenManager;
import com.example.pitchboxd.global.logging.MdcLoggingFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenManager tokenManager;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final Environment env;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/favicon.ico", "/error", "/static/**", "/css/**", "/js/**", "/images/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
                        auth.requestMatchers("/api/v1/**", "/**").permitAll();
                    } else {
                        auth.requestMatchers(
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/logout",
                                        "/api/v1/users",
                                        "/api/v1/users/email/exists",
                                        "/api/v1/users/nickname/exist",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/h2-console/**",
                                        "/api/v1/auth/oauth/signup",
                                        "/api/v1/matches/*/detail/static",
                                        "/api/v1/matches/*/detail/personal",
                                        "/api/v1/matches/*/match-reviews/hot",
                                        "/api/v1/matches/*/match-reviews",
                                        "/login/oauth2/code/google",
                                        "/favicon.ico",
                                        "/error"
                                ).permitAll()
                                .anyRequest().authenticated();
                    }
                })
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 Authentication Failure: ", exception);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter()
                                    .write("{\"code\":\"A-010\",\"message\":\"인증 실패: " + exception.getMessage()
                                            + "\"}");
                        })
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .addFilterBefore(new JwtAuthenticationFilter(tokenManager, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new MdcLoggingFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }
}
