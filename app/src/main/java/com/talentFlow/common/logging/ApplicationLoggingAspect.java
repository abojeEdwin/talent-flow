package com.talentFlow.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApplicationLoggingAspect {

    @Around("execution(public * com.talentFlow..web..*(..))")
    public Object logWebLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "WEB");
    }

    @Around(
            "execution(public * com.talentFlow..application..*(..)) || " +
            "execution(public * com.talentFlow..worker..*(..)) || " +
            "execution(public * com.talentFlow..infrastructure.mail.queue..*(..))"
    )
    public Object logApplicationLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "APP");
    }

    private Object logExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long startNanos = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("[{}] {} completed in {} ms", layer, signature, durationMs);
            return result;
        } catch (Throwable throwable) {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.error("[{}] {} failed after {} ms: {}", layer, signature, durationMs, throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}
