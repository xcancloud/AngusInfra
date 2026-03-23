package cloud.xcan.angus.persistence.jpa.multitenancy;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Fixes {@link TenantMetadata#tenantTableNames} for deterministic tests. When non-empty,
 * {@link TenantMetadata#ensureLoaded()} returns without rescanning the classpath.
 */
final class TenantMetadataTestSupport {

  private TenantMetadataTestSupport() {
  }

  static void installTenantTableNames(Set<String> names) {
    try {
      Field f = TenantMetadata.class.getDeclaredField("tenantTableNames");
      f.setAccessible(true);
      Set<String> copy = Set.copyOf(names);
      f.set(null, copy);
      TenantMetadata.TENANT_TABLES.clear();
      TenantMetadata.TENANT_TABLES.addAll(copy);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  static Set<String> currentTenantTableNames() {
    TenantMetadata.ensureLoaded();
    return new HashSet<>(TenantMetadata.getTenantTableNames());
  }
}
