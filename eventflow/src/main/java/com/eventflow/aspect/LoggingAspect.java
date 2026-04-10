package com.eventflow.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.eventflow.service..*)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logServiceCalls(ProceedingJoinPoint jp) throws Throwable {
        String method = jp.getSignature().getDeclaringType().getSimpleName()
                + "." + jp.getSignature().getName();
        long start = System.currentTimeMillis();
        try {
            Object result = jp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > 500) {
                log.warn("⚠ SLOW SERVICE CALL: {} took {}ms", method, elapsed);
            } else {
                log.debug("→ {} completed in {}ms", method, elapsed);
            }
            return result;
        } catch (Exception e) {
            log.error("✗ {} threw {}: {}", method, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
