package cloud.xcan.angus.persistence.jpa.multitenancy;

import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationClasses;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.experimental.MultiTenant;
import jakarta.persistence.Table;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Resolves physical table names and entity types that participate in multi-tenant isolation
 * (annotated with {@link MultiTenant}).
 */
public final class TenantMetadata {

  private static volatile Set<String> tenantTableNames = Set.of();
  private static final Object LOAD_LOCK = new Object();

  private TenantMetadata() {
  }

  /**
   * Mutable legacy view for callers that still add table names at runtime; prefer
   * {@link #getTenantTableNames()} for reads.
   */
  public static final Set<String> TENANT_TABLES = new CopyOnWriteArraySet<>();

  public static Set<String> getTenantTableNames() {
    ensureLoaded();
    return tenantTableNames;
  }

  public static void ensureLoaded() {
    if (!tenantTableNames.isEmpty()) {
      return;
    }
    synchronized (LOAD_LOCK) {
      if (!tenantTableNames.isEmpty()) {
        return;
      }
      Set<String> loaded = loadAnnotationTable("cloud.xcan.angus", MultiTenant.class);
      tenantTableNames = Collections.unmodifiableSet(new HashSet<>(loaded));
      TENANT_TABLES.clear();
      TENANT_TABLES.addAll(tenantTableNames);
    }
  }

  public static boolean isMultiTenantEntity(Class<?> type) {
    if (type == null) {
      return false;
    }
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      if (c.getAnnotation(MultiTenant.class) != null) {
        return true;
      }
    }
    return false;
  }

  public static Set<String> loadAnnotationTable(String packageName,
      Class<? extends Annotation> annotation) {
    Set<Class<?>> allClazz = getAnnotationClasses(packageName, annotation);
    Set<String> names = new HashSet<>();
    for (Class<?> c : allClazz) {
      Table t = c.getAnnotation(Table.class);
      if (nonNull(t) && !t.name().isEmpty()) {
        names.add(t.name());
      } else {
        names.add(c.getSimpleName().toLowerCase());
      }
    }
    return names;
  }
}
