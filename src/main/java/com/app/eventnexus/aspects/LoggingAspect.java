package com.app.eventnexus.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP aspect that logs every service method call with its execution time.
 *
 * <p>Sensitive fields (passwords) are never logged — argument types are
 * recorded instead of raw values so that no credentials leak into log files.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Wraps every public method in the services package.
     * Logs the method name, sanitized argument types, and wall-clock execution
     * time in milliseconds on successful completion.
     *
     * @param joinPoint the intercepted method invocation
     * @return the value returned by the target method
     * @throws Throwable if the target method throws; the exception propagates unchanged
     */
    @Around("execution(* com.app.eventnexus.services.*.*(..))")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String args = sanitizeArgs(joinPoint.getArgs());

        log.debug("[SERVICE] --> {} args=[{}]", methodName, args);
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.debug("[SERVICE] <-- {} completed in {}ms", methodName, elapsed);

        return result;
    }

    /**
     * Converts the method arguments to a log-safe string.
     * Logs the simple class name of each argument rather than its value to
     * prevent any sensitive data (e.g. passwords in request DTOs) from being
     * written to log output.
     *
     * @param args the raw method arguments
     * @return a comma-separated list of argument class names, or "none"
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }
}
