package com.epam.finaltask.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Log method entry
    @Before("execution(* com.epam.finaltask.service..*(..))")
    public void logMethodStart(JoinPoint joinPoint) {
        log.info("Started method: {} with args: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    // Log method exit
    @AfterReturning(pointcut = "execution(* com.epam.finaltask.service..*(..))", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        log.info("Finished method: {} with result: {}", joinPoint.getSignature(), result);
    }

    // Log exceptions
    @AfterThrowing(pointcut = "execution(* com.epam.finaltask.service..*(..))", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Exception ex) {
        log.error("Exception in method: {} with message: {}", joinPoint.getSignature(), ex.getMessage());
    }

    // Log controllers
    @Before("execution(* com.epam.finaltask.restcontroller..*(..))")
    public void logControllerStart(JoinPoint joinPoint) {
        log.info("Controller call: {} with args: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }
}
