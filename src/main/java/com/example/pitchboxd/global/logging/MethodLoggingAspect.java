package com.example.pitchboxd.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    @Around("execution(* com.example.pitchboxd..*Controller.*(..)) || execution(* com.example.pitchboxd..*Service.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getName();
        
        // Exclude global package internally from proxy logs to avoid circular dependencies
        if (className.startsWith("com.example.pitchboxd.global")) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Check if @LoggingExclude is present on class or method
        boolean isExcluded = method.isAnnotationPresent(LoggingExclude.class) 
            || joinPoint.getTarget().getClass().isAnnotationPresent(LoggingExclude.class);

        if (isExcluded) {
            return joinPoint.proceed();
        }

        String methodName = signature.getName();
        String shortClassName = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        String formattedArgs = Arrays.stream(args)
                .map(this::safeFormat)
                .collect(Collectors.joining(", "));

        log.info("[Method-Start] {}.{}({})", shortClassName, methodName, formattedArgs);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            
            log.info("[Method-End] {}.{}(...) | Time: {}ms | Return: {}", 
                shortClassName, methodName, duration, safeFormat(result));
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            log.error("[Method-Exception] {}.{}(...) | Time: {}ms | Exception: {} ({})", 
                shortClassName, methodName, duration, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    // Helper for testing
    public String invokeSafeFormat(Object obj) {
        return safeFormat(obj);
    }

    private String safeFormat(Object obj) {
        if (obj == null) return "null";
        
        if (obj instanceof HttpServletRequest || obj instanceof HttpServletResponse 
            || obj instanceof BindingResult || obj instanceof MultipartFile) {
            return obj.getClass().getSimpleName();
        }
        
        String className = obj.getClass().getName();
        // Prevent calling toString() on Hibernate/JPA entities to avoid LazyInitializationException
        if (className.contains(".domain.")) {
            return "[" + obj.getClass().getSimpleName() + "]";
        }
        
        String str = obj.toString();
        if (str.length() > 500) {
            return str.substring(0, 500) + "...(truncated)";
        }
        return str;
    }
}
