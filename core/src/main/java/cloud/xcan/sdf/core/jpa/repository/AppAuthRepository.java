package cloud.xcan.sdf.core.jpa.repository;

import cloud.xcan.sdf.core.jpa.repository.app.AppAuth;

public interface AppAuthRepository {

  AppAuth findLatestByAppCode(String appCode);

  AppAuth findLatestByTenantIdAndAppCode(Long tenantId, String appCode);

}
