package com.gersimuca.erp.common.aspect.log;

import com.gersimuca.erp.common.util.LoggerUtils;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  private final ThreadLocal<Long> startTime = new ThreadLocal<>();

  @Before("execution(* com.gersimuca.erp.feature..*Service.*(..))")
  public void logBefore(JoinPoint joinPoint) {
    startTime.set(System.currentTimeMillis());
    LoggerUtils.info(
        log,
        "Method called: {}.{}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName());
  }

  @After("execution(* com.gersimuca.erp.feature..*Service.*(..))")
  public void logAfter(JoinPoint joinPoint) {
    LoggerUtils.info(
        log,
        "Method finished: {}.{} [Execution time: {}]",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        getDuration());
    clearTimer();
  }

  @AfterThrowing(
      pointcut = "execution(* com.gersimuca.erp.feature..*Service.*(..))",
      throwing = "exception")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
    LoggerUtils.error(
        log,
        "Exception in method: {}.{} after {} -> {}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        getDuration(),
        exception.getMessage());
  }

  private String getDuration() {
    Long start = ObjectUtils.defaultIfNull(startTime.get(), System.currentTimeMillis());
    Long duration = System.currentTimeMillis() - start;
    return formatDuration(duration);
  }

  private String formatDuration(Long millis) {
    Duration duration = Duration.ofMillis(millis);
    Long minutes = duration.toMinutes();
    Long seconds = duration.minusMinutes(minutes).getSeconds();
    Long milliseconds = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis();
    StringBuilder sb = new StringBuilder();

    if (minutes > 0) {
      sb.append(minutes).append("m");
    }

    if (seconds > 0 || minutes > 0) {
      sb.append(seconds).append("s");
    }

    sb.append(milliseconds).append("ms");
    return sb.toString().trim();
  }

  private void clearTimer() {
    startTime.remove();
  }
}
