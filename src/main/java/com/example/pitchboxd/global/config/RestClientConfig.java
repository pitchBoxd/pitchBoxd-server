package com.example.pitchboxd.global.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // 1. 타임아웃 설정을 위한 RequestFactory 생성
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        // 2. RestClient 빌더에 팩토리 주입 및 생성
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
