package com.app.eventnexus.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that captures and logs every exception thrown from a service method.
 *
 * <p>This works alongside {@link com.app.eventnexus.exceptions.GlobalExceptionHandler}:
 * the aspect records diagnostic information at ERROR level <em>before</em> the
 * exception travels up the call stack and is translated into an HTTP response by
 * the handler.  Both mechanisms are needed — the aspect for observability, the
 * handler for client-facing error shapes.
 */
@Aspect
@Component
public class ExceptionHandlingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingAspect.class);

    /**
     * Fires after any service method exits by throwing an exception.
     * Logs the fully-qualified exception type, its message, and the full
     * stack trace so that root-cause analysis can be performed from logs alone.
     *
     * @param joinPoint the intercepted method invocation
     * @param ex        the exception that was thrown
     */
    @AfterThrowing(
            pointcut = "execution(* com.app.eventnexus.services.*.*(..))",
            throwing  = "ex")
    public void logServiceException(JoinPoint joinPoint, Exception ex) {
        log.error("[SERVICE] Exception in {}.{}() — {}: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);
    }
}
