package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TenantMetadataIsolationTest {

  @Test
  void isMultiTenantEntity_trueWhenClassAnnotated() {
    assertTrue(TenantMetadata.isMultiTenantEntity(MtIsolationEntities.MultiTenantEntity.class));
  }

  @Test
  void isMultiTenantEntity_falseWhenNotAnnotated() {
    assertFalse(TenantMetadata.isMultiTenantEntity(MtIsolationEntities.NonMultiTenantEntity.class));
  }

  @Test
  void isMultiTenantEntity_falseForNull() {
    assertFalse(TenantMetadata.isMultiTenantEntity(null));
  }
}
