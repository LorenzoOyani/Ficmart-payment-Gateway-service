package com.org.infrastructure.bank;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Component
public class BankChaosAspect {


    @Around("execution(* com.org.infrastructure.bank.MockBankCoreService.*(..)))")
    public Object chaos(ProceedingJoinPoint joinPoint) throws Throwable {
            int threadLocale =  ThreadLocalRandom.current().nextInt(100, 2001);
            Thread.sleep(threadLocale);
            if(threadLocale < 5){
                throw  new RuntimeException("500 failure");
            }

            return joinPoint.proceed();
    }
}
