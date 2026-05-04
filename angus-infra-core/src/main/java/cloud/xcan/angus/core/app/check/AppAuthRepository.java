package cloud.xcan.angus.core.app.check;

import org.springframework.lang.Nullable;

/**
 * Loads {@link AppAuth} for {@link CheckAppExpirationAspect}.
 */
public interface AppAuthRepository {

  @Nullable
  AppAuth findLatestByAppCode(String appCode);

  @Nullable
  AppAuth findLatestByTenantIdAndAppCode(Long tenantId, String appCode);
}
