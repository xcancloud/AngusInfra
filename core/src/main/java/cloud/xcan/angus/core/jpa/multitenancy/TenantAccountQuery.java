package cloud.xcan.angus.core.jpa.multitenancy;

import java.util.List;
import java.util.Optional;

public interface TenantAccountQuery {

  /**
   * 根据当前租户ID查询同账号下的所有租户ID
   *
   * @param currentTenantId 当前租户ID
   * @return 同账号下的租户ID列表
   */
  List<Long> getTenantIdsBySameAccount(Long currentTenantId);

  /**
   * 根据当前租户ID查询主租户账号ID
   *
   * @param currentTenantId 当前租户ID
   * @return 主租户账号ID（如果当前就是主租户，则返回自身）
   */
  Long getMainTenantId(Long currentTenantId);

  /**
   * 检查指定租户ID是否为主租户
   *
   * @param tenantId 租户ID
   * @return 是否为主租户
   */
  boolean isMainTenant(Long tenantId);

}
