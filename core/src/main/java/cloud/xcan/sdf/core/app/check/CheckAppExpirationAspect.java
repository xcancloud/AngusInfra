
package cloud.xcan.sdf.core.app.check;

import static cloud.xcan.sdf.api.CommonMessage.APP_EXPIRED_CODE;
import static cloud.xcan.sdf.api.CommonMessage.APP_EXPIRED_T;
import static cloud.xcan.sdf.api.CommonMessage.APP_NOT_OPENED_CODE;
import static cloud.xcan.sdf.api.CommonMessage.APP_NOT_OPENED_T;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isPrivateEdition;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.sdf.core.utils.PrincipalContextUtils.isValidOptTenantId;

import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.jpa.repository.AppAuthRepository;
import cloud.xcan.sdf.core.jpa.repository.app.AppAuth;
import cloud.xcan.sdf.spec.experimental.Assert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Check whether the application is opened and expired when cloud service.
 * <p>
 * Check whether the application license expires when privatizing.
 *
 * @author liuxiaolong
 */
@Aspect
@Slf4j
public class CheckAppExpirationAspect {

  @Autowired(required = false)
  private List<AppAuthRepository> commonAppOpenRepo;

  @Pointcut("@annotation(cloud.xcan.sdf.core.app.check.CheckAppNotExpired)"
  )
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
    CheckAppNotExpired appExpiration = ((MethodSignature) joinPoint.getSignature()).getMethod()
        .getAnnotation(CheckAppNotExpired.class);
    Assert.assertNotEmpty(commonAppOpenRepo, "AppAuthRepository instance is required");
    AppAuth appAuth = null;
    if (isCloudServiceEdition()) {
      Long optTenantId = getOptTenantId();
      if (isValidOptTenantId(optTenantId)) {
        appAuth = commonAppOpenRepo.get(0)
            .findLatestByTenantIdAndAppCode(optTenantId, appExpiration.appCode());
        BizAssert.assertNotNull(appAuth, BizException.of(APP_NOT_OPENED_CODE,
            APP_NOT_OPENED_T, new Object[]{appExpiration.appCode()}));
      } else {
        throw new IllegalStateException(
            "CheckAppExpiration parameter is missing, the tenantId was not read from the  Isn't it a user interface?");
      }
    } else if (isPrivateEdition()) {
      appAuth = commonAppOpenRepo.get(0).findLatestByAppCode(appExpiration.appCode());
      BizAssert.assertNotNull(appAuth, BizException.of(APP_NOT_OPENED_CODE,
          APP_NOT_OPENED_T, new Object[]{appExpiration.appCode()}));
    }
    if (Objects.nonNull(appAuth)) {
      if (appAuth.getExpirationDate().isBefore(LocalDateTime.now())) {
        throw BizException
            .of(APP_EXPIRED_CODE, APP_EXPIRED_T, new Object[]{appExpiration.appCode()});
      }
    }
    return joinPoint.proceed();
  }

}
