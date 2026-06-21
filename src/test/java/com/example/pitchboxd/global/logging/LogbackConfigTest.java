package com.example.pitchboxd.global.logging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import ch.qos.logback.classic.LoggerContext;

@SpringBootTest(classes = LogbackConfigTest.EmptyTestConfig.class)
@ActiveProfiles("local")
public class LogbackConfigTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
    })
    public static class EmptyTestConfig {}

    @Test
    void testLogbackAppendersConfigured() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        assertNotNull(context.getLogger("ROOT").getAppender("FILE_SYSTEM"), "System file appender should be configured");
        assertNotNull(context.getLogger("com.example.pitchboxd").getAppender("FILE_CUSTOM"), "Custom file appender should be configured");
    }
}
