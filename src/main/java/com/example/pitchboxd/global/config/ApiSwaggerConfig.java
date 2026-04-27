package com.example.pitchboxd.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "http:localhost:8080", description = "로컬 서버"),
        }
)
public class ApiSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PitchBoxd API 문서")
                        .version("v1")
                        .description("PitchBoxd API 명세입니다.")
                )
                .tags(List.of(
                        new Tag().name("User API").description("사용자 관련 API 명세")
                ));
    }
}
