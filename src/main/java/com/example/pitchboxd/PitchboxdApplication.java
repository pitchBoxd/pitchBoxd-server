package com.example.pitchboxd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PitchboxdApplication {

    public static void main(String[] args) {
        SpringApplication.run(PitchboxdApplication.class, args);
    }
}
