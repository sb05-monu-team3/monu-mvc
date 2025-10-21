package com.monew.monew_server.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 서비스 계층의 모든 메서드를 포함하는 포인트 컷
    @Pointcut("execution(* com.monew.monew_server.domain..service..*(..))")
    public void serviceLayerPointcut() {}

    // 컨트롤러 계층의 모든 메서드를 포함하는 포인트 컷
    @Pointcut("execution(* com.monew.monew_server.domain..controller..*(..))")
    public void controllerLayerPointcut() {}

    // 메서드 시작 이전의 로그를 남기는 메서드
    @Before( "serviceLayerPointcut() || controllerLayerPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("==> {}.{}({})", className, methodName, Arrays.toString(args));
    }

    // 메서드가 정상적으로 호출된 이후에 남기는 메서드
    @AfterReturning(pointcut = "serviceLayerPointcut() || controllerLayerPointcut()", returning = "result" )
    public void logAfter(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("<== {}.{}({}), return : {}", className, methodName, Arrays.toString(args), result);
    }

    @AfterThrowing(pointcut = "serviceLayerPointcut() || controllerLayerPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Exception e) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.error("<== {}.{}({})", className, methodName, e.getMessage(), e);
    }

    // 실행시간 측정하여 시간 오버 된 경우 에러 남기기
    @Around("serviceLayerPointcut()")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            Object result = proceedingJoinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("{}#{} 실행 시간: {}ms", className, methodName, executionTime);

            // 성능 경고 (1초 이상 소요 시)
            if (executionTime > 1000) {
                log.warn("[s2][LoggingAspect] {}#{} 실행 시간이 {}ms로 느립니다. 성능 최적화가 필요합니다.",
                        className, methodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.error("{}#{} 실행 실패 - 실행 시간: {}ms, 예외: {}",
                    className, methodName, executionTime, throwable.getMessage());
            throw throwable;
        }
    }
}
