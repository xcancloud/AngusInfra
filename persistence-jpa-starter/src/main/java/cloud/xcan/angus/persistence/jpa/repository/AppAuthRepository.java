package cloud.xcan.angus.persistence.jpa.repository;

import cloud.xcan.angus.persistence.jpa.repository.app.AppAuth;

public interface AppAuthRepository {

  AppAuth findLatestByAppCode(String appCode);

  AppAuth findLatestByTenantIdAndAppCode(Long tenantId, String appCode);

}
