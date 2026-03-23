package cloud.xcan.angus.core.app.check;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isPrivateEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isValidOptTenantId;
import static cloud.xcan.angus.remote.CommonMessage.APP_EXPIRED_CODE;
import static cloud.xcan.angus.remote.CommonMessage.APP_EXPIRED_T;
import static cloud.xcan.angus.remote.CommonMessage.APP_NOT_OPENED_CODE;
import static cloud.xcan.angus.remote.CommonMessage.APP_NOT_OPENED_T;

import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.spec.experimental.Assert;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cloud: ensure the app is registered for the tenant and within the licensed window. Private: ensure
 * license / expiry rules for the given {@link CheckAppNotExpired#appCode()}.
 *
 * @author XiaoLong Liu
 */
@Aspect
@Slf4j
public class CheckAppExpirationAspect {

  @Autowired(required = false)
  private List<AppAuthRepository> appAuthRepositories;

  @Pointcut("@annotation(cloud.xcan.angus.core.app.check.CheckAppNotExpired)")
  public void checkAppNotExpiredPointcut() {
  }

  @Around("checkAppNotExpiredPointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!isUserAction()) {
      return joinPoint.proceed();
    }
    return check(joinPoint);
  }

  private Object check(ProceedingJoinPoint joinPoint) throws Throwable {
    CheckAppNotExpired appExpiration = ((MethodSignature) joinPoint.getSignature()).getMethod()
        .getAnnotation(CheckAppNotExpired.class);
    Assert.assertNotEmpty(appAuthRepositories, "AppAuthRepository instance is required");

    AppAuthRepository repository = appAuthRepositories.getFirst();
    AppAuth appAuth = null;

    if (isCloudServiceEdition()) {
      Long optTenantId = getOptTenantId();
      if (isValidOptTenantId(optTenantId)) {
        appAuth = repository.findLatestByTenantIdAndAppCode(optTenantId, appExpiration.appCode());
        BizAssert.assertNotNull(appAuth, BizException.of(APP_NOT_OPENED_CODE,
            APP_NOT_OPENED_T, new Object[]{appExpiration.appCode()}));
      } else {
        throw new IllegalStateException(
            "CheckAppNotExpired requires a valid tenant context (optTenantId) on cloud edition");
      }
    } else if (isPrivateEdition()) {
      appAuth = repository.findLatestByAppCode(appExpiration.appCode());
      BizAssert.assertNotNull(appAuth, BizException.of(APP_NOT_OPENED_CODE,
          APP_NOT_OPENED_T, new Object[]{appExpiration.appCode()}));
    }

    if (appAuth != null) {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime openDate = appAuth.getOpenDate();
      if (openDate != null && openDate.isAfter(now)) {
        throw BizException.of(APP_NOT_OPENED_CODE, APP_NOT_OPENED_T,
            new Object[]{appExpiration.appCode()});
      }
      LocalDateTime expirationDate = appAuth.getExpirationDate();
      if (expirationDate != null && expirationDate.isBefore(now)) {
        throw BizException.of(APP_EXPIRED_CODE, APP_EXPIRED_T,
            new Object[]{appExpiration.appCode()});
      }
    }

    return joinPoint.proceed();
  }
}
