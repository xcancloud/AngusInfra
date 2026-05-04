package cloud.xcan.angus.web;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.persistence.jpa.JpaDynamicQueryUtils.executeDynamicQuery0;
import static cloud.xcan.angus.persistence.jpa.JpaDynamicQueryUtils.objectArrToInt;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.SneakyThrow0;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

/**
 * Check whether the quotas (users, exec, exec_debug - INFO,WARN,ERROR) when privatizing.
 */
@Aspect
@Slf4j
public class SneakyLogConfigurer {

  @Autowired(required = false)
  @PersistenceContext
  protected EntityManager manager;

  @Pointcut("@annotation(cloud.xcan.angus.core.biz.SneakyThrow0)")
  public void logPointCut() {
  }

  @Around("logPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!isUserAction()) {
      return joinPoint.proceed();
    }
    return check(joinPoint);
  }

  private Object check(ProceedingJoinPoint joinPoint) throws Throwable {
    Object object = joinPoint.proceed();

    // Post-processing
    SneakyThrow0 throw0 = ((MethodSignature) joinPoint.getSignature()).getMethod()
        .getAnnotation(SneakyThrow0.class);
    if (nonNull(manager) && getApplicationInfo().isPrivateEdition()) {
      switch (throw0.level()) {
        case "ERROR": {
          checkSneakyError(manager);
          break;
        }
        case "WARN": {
          checkSneakyWarn(manager);
          break;
        }
        case "INFO": {
          checkSneakyInfo(manager);
          break;
        }
        default: {
          checkSneakyInfo(manager);
        }
      }
    }
    return object;
  }

  public static Object checkSneakyInfo(EntityManager manager) {
    int level;
    try {
      // Check users, max 20
      List<?> result = executeDynamicQuery0(manager, "SELECT count(*) FROM user0");
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }
    String limit = System.getProperty("TERM_THROW_INFO", "20");
    if (level > Integer.parseInt(limit) + 1 /*Allow one concurrent user to be missed*/) {
      System.out.println("Internal application error: LE-0901");
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      //System.exit(-1);
    }
    return level;
  }

  public static Object checkSneakyWarn(EntityManager manager) {
    int level;
    try {
      // Check concurrent task, max 2
      List<?> result = executeDynamicQuery0(manager,
          "SELECT count(*) FROM exec WHERE status = 'RUNNING'");
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }

    String limit = System.getProperty("TERM_THROW_WARN", "2");
    if (level > Integer.parseInt(limit) + 1/*Allow one concurrent task to be missed*/) {
      System.out.println("Internal application error: LE-0902");
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      //System.exit(-1);
    }
    return level;
  }

  public static Object checkSneakyError(EntityManager manager) {
    int level;
    try {
      // Check concurrent task, max 2
      List<?> result = executeDynamicQuery0(manager,
          "SELECT count(*) FROM exec_debug WHERE status = 'RUNNING'");
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }

    String limit = System.getProperty("TERM_THROW_ERROR", "2");
    if (level > Integer.parseInt(limit) + 1/*Allow one concurrent task to be missed*/) {
      System.out.println("Internal application error: LE-0903");
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      //System.exit(-1);
    }
    return level;
  }

}
