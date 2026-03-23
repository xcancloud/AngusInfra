package cloud.xcan.angus.persistence.jpa.multitenancy;

import cloud.xcan.angus.spec.experimental.MultiTenant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Test-only JPA types for multitenancy metadata / {@link TenantMetadata#isMultiTenantEntity} checks
 * (under {@code cloud.xcan.angus} so they participate in normal scans when loaded).
 */
final class MtIsolationEntities {

  private MtIsolationEntities() {
  }

  @MultiTenant
  @Entity
  @Table(name = "mt_isolation_verify")
  static class MultiTenantEntity {

    @Id
    private Long id;
  }

  @Entity
  @Table(name = "non_mt_isolation_verify")
  static class NonMultiTenantEntity {

    @Id
    private Long id;
  }
}
