package com.gersimuca.erp.common.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.gersimuca.erp.common.aspect.log.LoggingAspect;
import com.gersimuca.erp.common.util.LoggerUtils;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.stream.Stream;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

  @InjectMocks private LoggingAspect aspect;

  private MockedStatic<LoggerUtils> loggerUtilsMock;

  @BeforeEach
  void setUp() {
    loggerUtilsMock = mockStatic(LoggerUtils.class);
  }

  @BeforeEach
  void contextLoads() {
    assertNotNull(aspect, "Aspect should not be null");
    assertNotNull(loggerUtilsMock, "LoggerUtils mock should not be null");
  }

  @AfterEach
  void tearDown() {
    ReflectionTestUtils.invokeMethod(aspect, "clearTimer");
    loggerUtilsMock.close();
  }

  @Test
  void logBefore_setsStartTimeAndLogs() throws Exception {
    JoinPoint joinPoint =
        mockJoinPoint("com.gersimuca.erp.feature.user.UserServiceImpl", "mustLoadByUsername");

    aspect.logBefore(joinPoint);

    Field tlField = LoggingAspect.class.getDeclaredField("startTime");
    tlField.setAccessible(true);
    ThreadLocal<?> tl = (ThreadLocal<?>) tlField.get(aspect);
    assertNotNull(tl.get(), "startTime should be set");

    loggerUtilsMock.verify(
        () ->
            LoggerUtils.info(
                any(),
                eq("Method called: {}.{}"),
                eq("com.gersimuca.erp.feature.user.UserServiceImpl"),
                eq("mustLoadByUsername")),
        times(1));
  }

  @Test
  void logAfter_logsDurationAndClears() throws Exception {
    JoinPoint joinPoint =
        mockJoinPoint("com.gersimuca.erp.feature.user.UserServiceImpl", "mustLoadByUsername");

    aspect.logBefore(joinPoint);

    Awaitility.await().atMost(Duration.ofSeconds(1L)).untilAsserted(() -> {});

    aspect.logAfter(joinPoint);

    loggerUtilsMock.verify(
        () ->
            LoggerUtils.info(
                any(),
                eq("Method finished: {}.{} [Execution time: {}]"),
                eq("com.gersimuca.erp.feature.user.UserServiceImpl"),
                eq("mustLoadByUsername"),
                any()),
        times(1));

    Field tlField = LoggingAspect.class.getDeclaredField("startTime");
    tlField.setAccessible(true);
    ThreadLocal<?> tl = (ThreadLocal<?>) tlField.get(aspect);
    assertNull(tl.get(), "startTime should have been cleared");
  }

  @Test
  void logAfterThrowing_logsExceptionAndClears() throws Exception {
    JoinPoint joinPoint =
        mockJoinPoint("com.gersimuca.erp.feature.user.UserServiceImpl", "mustLoadByUsername");

    aspect.logBefore(joinPoint);

    Awaitility.await().atMost(Duration.ofSeconds(1L)).untilAsserted(() -> {});

    RuntimeException ex = new RuntimeException("Exception");
    aspect.logAfterThrowing(joinPoint, ex);

    loggerUtilsMock.verify(
        () ->
            LoggerUtils.error(
                any(),
                eq("Exception in method: {}.{} after {} -> {}"),
                eq("com.gersimuca.erp.feature.user.UserServiceImpl"),
                eq("mustLoadByUsername"),
                any(),
                eq("Exception")),
        times(1));

    Field tlField = LoggingAspect.class.getDeclaredField("startTime");
    tlField.setAccessible(true);
    ThreadLocal<?> tl = (ThreadLocal<?>) tlField.get(aspect);
    assertNotNull(tl.get(), "startTime should not been cleared");
  }

  @ParameterizedTest
  @MethodSource("durationProvider")
  void formatDuration_producesExpectedString(Duration input, String expected) {
    String result = ReflectionTestUtils.invokeMethod(aspect, "formatDuration", input.toMillis());
    assertEquals(expected, result);
  }

  private JoinPoint mockJoinPoint(String className, String methodName) {
    JoinPoint joinPoint = org.mockito.Mockito.mock(JoinPoint.class);
    Signature signature = org.mockito.Mockito.mock(Signature.class);
    when(signature.getDeclaringTypeName()).thenReturn(className);
    when(signature.getName()).thenReturn(methodName);
    when(joinPoint.getSignature()).thenReturn(signature);
    return joinPoint;
  }

  static Stream<Arguments> durationProvider() {
    return Stream.of(
        Arguments.of(Duration.ofMillis(0), "0ms"),
        Arguments.of(Duration.ofMillis(345), "345ms"),
        Arguments.of(Duration.ofSeconds(1), "1s0ms"),
        Arguments.of(Duration.ofSeconds(40), "40s0ms"),
        Arguments.of(Duration.ofMinutes(1).plusSeconds(2).plusMillis(345), "1m2s345ms"));
  }
}
