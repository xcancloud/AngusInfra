package cloud.xcan.angus.idgen.dao;

import cloud.xcan.angus.idgen.entity.IdConfig;

/**
 * Persistence operations for {@link IdConfig}. Core module only declares repository capabilities,
 * while starter module provides Spring Data JPA implementation.
 */
public interface IdConfigRepo {

  /**
   * Save id config.
   */
  IdConfig save(IdConfig idConfig);

  /**
   * Get {@link IdConfig} by bizKey and tenantId.
   */
  IdConfig findByBizKeyAndTenantId(String bizKey, Long tenantId);

  /**
   * Increment Id step by bizKey and tenantId.
   */
  int incrementByBizKeyAndTenantId(Long step, String bizKey, Long tenantId);

  /**
   * Get {@link IdConfig#getMaxId()} by bizKey and tenantId.
   */
  long findMaxIdByBizKeyAndTenantId(String bizKey, Long tenantId);

}
