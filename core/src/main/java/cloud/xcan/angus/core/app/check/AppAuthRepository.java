package cloud.xcan.angus.core.app.check;

public interface AppAuthRepository {

  AppAuth findLatestByAppCode(String appCode);

  AppAuth findLatestByTenantIdAndAppCode(Long tenantId, String appCode);

}
