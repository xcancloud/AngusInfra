package cloud.xcan.angus.web;

import static cloud.xcan.angus.core.jpa.JpaDynamicQueryUtils.executeDynamicQuery0;
import static cloud.xcan.angus.core.jpa.JpaDynamicQueryUtils.objectArrToInt;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.obf.Str0;
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

    // Post processing
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
      List<?> result = executeDynamicQuery0(manager, new Str0(
          new long[]{0x63021F438C28C76L, 0x2D00BBB80A94D62FL, 0xFD309A4276A7CAB2L,
              0x6B4676B2A5C8F831L, 0xE48757146213EF16L})
          .toString() /* => "SELECT count(*) FROM user0" */);
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }
    String limit = System.getProperty(
        new Str0(new long[]{0xD4EF4227FF54A4E2L, 0xAD7C114B3D0559AEL, 0x39962D51FCBFAC07L})
            .toString() /* => "TERM_THROW_INFO" */, "20");
    if (level > Integer.parseInt(limit) + 1 /*Allow one concurrent user to be missed*/) {
      System.out.println(new Str0(
          new long[]{0x113AEFA3DA397343L, 0xA450332D61315E92L, 0x40EB663FCD226F17L,
              0xC06F2E111EF68CE1L, 0xBF486396F3ADDF1FL, 0x7CC423C5D7737A9EL})
          .toString() /* => "Internal application error: LE-0901" */);
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
          new Str0(new long[]{0x80D1DDF8B6C7D399L, 0xDDFFD58C1AA98CD1L, 0x33D0659A555C6CF7L,
              0x668412E32FEA9073L, 0xB78378445B2FC090L, 0x6498404A14C064F5L, 0xC91DC42B7DB6E47EL,
              0xD7BAB69547119280L})
              .toString() /* => "SELECT count(*) FROM exec WHERE status = 'RUNNING'" */);
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }

    String limit = System.getProperty(
        new Str0(new long[]{0x308B191429AB80B6L, 0xE6547FEDF2921DCFL, 0x533EDA5633148E1EL})
            .toString() /* => "TERM_THROW_WARN" */, "2");
    if (level > Integer.parseInt(limit) + 1/*Allow one concurrent task to be missed*/) {
      System.out.println(new Str0(
          new long[]{0xE398A2E04516BECBL, 0x28AF6C31C60F25DBL, 0x695625C65D887DF2L,
              0xAEBE4465A8200066L, 0x2FD8B943CD1BB482L, 0xBC7F0409EEF5DB21L})
          .toString() /* => "Internal application error: LE-0902" */);
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
          new Str0(new long[]{0xE4DEB93B1AA0E85DL, 0x1650C9221E6564BEL, 0x306C7CF93698033AL,
              0x650D15002FC70A15L, 0x7DD004D0866EF34BL, 0xA070500CC3AE5B60L, 0xDB332A8C413BF619L,
              0x44FB80AE1D66CE31L})
              .toString() /* => "SELECT count(*) FROM exec_debug WHERE status = 'RUNNING'" */);
      level = objectArrToInt(result);
    } catch (Exception e) {
      // NOOP
      return null;
    }

    String limit = System.getProperty(
        new Str0(new long[]{0x3ABE1AD18127B9F9L, 0x96F7A1B90A43F287L, 0x9DC6965C2F42D561L})
            .toString() /* => "TERM_THROW_ERROR" */, "2");
    if (level > Integer.parseInt(limit) + 1/*Allow one concurrent task to be missed*/) {
      System.out.println(new Str0(
          new long[]{0xE739CA8D0CC1C0DEL, 0xBFA29B73F76C851CL, 0xDE8E40B48F2E64C4L,
              0x75E739AFD779B8E8L, 0x276A68788FF55E3AL, 0xF16B4ACD278803ECL})
          .toString() /* => "Internal application error: LE-0903" */);
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      //System.exit(-1);
    }
    return level;
  }

}
