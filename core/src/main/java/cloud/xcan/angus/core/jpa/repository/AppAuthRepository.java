package cloud.xcan.angus.core.jpa.repository;

import cloud.xcan.angus.core.jpa.repository.app.AppAuth;

public interface AppAuthRepository {

  AppAuth findLatestByAppCode(String appCode);

  AppAuth findLatestByTenantIdAndAppCode(Long tenantId, String appCode);

}
