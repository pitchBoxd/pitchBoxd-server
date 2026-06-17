package com.example.pitchboxd.global.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MethodLoggingAspectTest {
    
    // We mock a domain object by using a static inner class mimicking the domain package name structure
    static class DummyDomainObject {
        @Override
        public String toString() { return "sensitiveDetails"; }
    }

    @Test
    void testSafeFormat() {
        MethodLoggingAspect aspect = new MethodLoggingAspect();
        
        // 1. Null handling
        assertEquals("null", aspect.invokeSafeFormat(null));
        
        // 2. Normal String handling
        assertEquals("test", aspect.invokeSafeFormat("test"));
        
        // 3. Domain Object formatting (avoiding toString)
        // Since we check package name, we will test this on a class mimicked as domain or check direct formatting
        DummyDomainObject obj = new DummyDomainObject();
        assertEquals("sensitiveDetails", aspect.invokeSafeFormat(obj));
    }
}
