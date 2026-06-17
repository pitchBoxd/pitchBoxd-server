package com.example.pitchboxd.global.logging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class LoggingExcludeTest {
    @LoggingExclude
    static class TestClass {
        @LoggingExclude
        public void testMethod() {}
    }

    @Test
    void testAnnotationPresent() throws NoSuchMethodException {
        assertNotNull(TestClass.class.getAnnotation(LoggingExclude.class));
        assertNotNull(TestClass.class.getMethod("testMethod").getAnnotation(LoggingExclude.class));
    }
}
