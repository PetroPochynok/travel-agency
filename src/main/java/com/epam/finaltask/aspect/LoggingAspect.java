package com.epam.finaltask.aspect;

import com.epam.finaltask.annotation.Sensitive;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Log method entry
    @Before("execution(* com.epam.finaltask.service..*(..))")
    public void logMethodStart(JoinPoint joinPoint) {
        log.info("Started method: {} with args: {}",
                joinPoint.getSignature(),
                maskArgs(joinPoint.getArgs()));
    }

    // Log method exit
    @AfterReturning(pointcut = "execution(* com.epam.finaltask.service..*(..))", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        log.info("Finished method: {} with result: {}",
                joinPoint.getSignature(),
                maskSensitiveFields(result));
    }

    // Log exceptions
    @AfterThrowing(pointcut = "execution(* com.epam.finaltask.service..*(..))", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Exception ex) {
        log.error("Exception in method: {} with message: {}", joinPoint.getSignature(), ex.getMessage());
    }

    // Log controllers
    @Before("execution(* com.epam.finaltask.restcontroller..*(..))")
    public void logControllerStart(JoinPoint joinPoint) {
        log.info("Controller call: {} with args: {}",
                joinPoint.getSignature(),
                maskArgs(joinPoint.getArgs()));
    }

    // Log execution time
    @Around("execution(* com.epam.finaltask.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        log.info("Method {} executed in {} ms", joinPoint.getSignature(), duration);
        return result;
    }

    private Object[] maskArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::maskSensitiveFields)
                .toArray();
    }

    private Object maskSensitiveFields(Object arg) {
        if (arg == null) return null;

        Class<?> clazz = arg.getClass();
        if (clazz.isPrimitive()
                || clazz.equals(String.class)
                || Number.class.isAssignableFrom(clazz)
                || clazz.equals(Boolean.class)) {
            return arg;
        }

        if (arg instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::maskSensitiveFields)
                    .toList();
        }

        if (arg instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> maskSensitiveFields(e.getValue())
                    ));
        }

        if (clazz.isArray()) {
            return Arrays.stream((Object[]) arg)
                    .map(this::maskSensitiveFields)
                    .toArray();
        }

        if (clazz.getPackageName().startsWith("org.springframework")) {
            return arg;
        }

        try {
            Object copy = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Sensitive.class)) {
                    field.set(copy, "******");
                } else {
                    Object fieldValue = field.get(arg);
                    field.set(copy, maskSensitiveFields(fieldValue));
                }
            }

            return copy;
        } catch (Exception e) {
            return arg;
        }
    }

}
