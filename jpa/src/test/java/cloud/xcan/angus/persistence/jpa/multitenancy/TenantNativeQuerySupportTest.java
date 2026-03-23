package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 原生 SQL 路径：在「自动多租户」开启且实体带 {@link cloud.xcan.angus.spec.experimental.MultiTenant} 时
 * 应追加 {@code tenant_id} 条件并绑定 {@link TenantNativeQuerySupport#PARAM_NAME}；关闭或实体非多租户时不应追加。
 */
class TenantNativeQuerySupportTest extends PrincipalTestSupport {

  private static Set<String> originalTenantTables;

  @BeforeAll
  static void captureTenantMetadata() {
    originalTenantTables = TenantMetadataTestSupport.currentTenantTableNames();
  }

  @BeforeEach
  void tenantTablesForNativeScoping() {
    TenantMetadataTestSupport.installTenantTableNames(Set.of("mt_isolation_verify"));
  }

  @AfterEach
  void restoreTenantMetadata() {
    TenantMetadataTestSupport.installTenantTableNames(originalTenantTables);
  }

  @Test
  void shouldAppendTenantPredicate_falseForNonMultiTenantEntity() {
    bindPrincipal(apiTenantPrincipal(200L));
    assertFalse(
        TenantNativeQuerySupport.shouldAppendTenantPredicate(
            MtIsolationEntities.NonMultiTenantEntity.class));
  }

  @Test
  void shouldAppendTenantPredicate_falseWhenMultiTenantCtrlOff() {
    bindPrincipal(multiTenantOff(200L));
    assertFalse(
        TenantNativeQuerySupport.shouldAppendTenantPredicate(
            MtIsolationEntities.MultiTenantEntity.class));
  }

  @Test
  void shouldAppendTenantPredicate_trueWhenScopedAndEntityIsMultiTenant() {
    bindPrincipal(apiTenantPrincipal(77L));
    assertTrue(
        TenantNativeQuerySupport.shouldAppendTenantPredicate(
            MtIsolationEntities.MultiTenantEntity.class));
  }

  @Test
  void shouldAppendTenantPredicate_throwsFailClosedWhenScopedButNoValidTenant() {
    bindPrincipal(
        new cloud.xcan.angus.spec.principal.Principal()
            .setMultiTenantCtrl(true)
            .setApiType(cloud.xcan.angus.api.enums.ApiType.API)
            .setTenantId(0L)
            .setOptTenantId(null));
    assertThrows(
        IllegalStateException.class,
        () ->
            TenantNativeQuerySupport.shouldAppendTenantPredicate(
                MtIsolationEntities.MultiTenantEntity.class));
  }

  @Test
  void appendQualifiedTenantClause_appendsPredicateWhenApplicable() {
    bindPrincipal(apiTenantPrincipal(55L));
    StringBuilder sql = new StringBuilder("WHERE 1=1");
    TenantNativeQuerySupport.appendQualifiedTenantClause(sql, "t",
        MtIsolationEntities.MultiTenantEntity.class);
    assertTrue(sql.toString().contains("t.tenant_id = :"));
    assertTrue(sql.toString().contains(TenantNativeQuerySupport.PARAM_NAME));
  }

  @Test
  void appendUnqualifiedTenantClause_appendsWhenApplicable() {
    bindPrincipal(apiTenantPrincipal(56L));
    StringBuilder sql = new StringBuilder("WHERE id = 1");
    TenantNativeQuerySupport.appendUnqualifiedTenantClause(sql,
        MtIsolationEntities.MultiTenantEntity.class);
    assertTrue(sql.toString().contains("tenant_id = :"));
  }

  @Test
  void bindTenantParameterIfNeeded_setsParameterWhenApplicable() {
    bindPrincipal(apiTenantPrincipal(88L));
    Query query = mock(Query.class);
    TenantNativeQuerySupport.bindTenantParameterIfNeeded(query,
        MtIsolationEntities.MultiTenantEntity.class);
    verify(query).setParameter(eq(TenantNativeQuerySupport.PARAM_NAME), eq(88L));
  }

  @Test
  void bindTenantParameterIfNeeded_skipsWhenNotApplicable() {
    bindPrincipal(apiTenantPrincipal(1L));
    Query query = mock(Query.class);
    TenantNativeQuerySupport.bindTenantParameterIfNeeded(query,
        MtIsolationEntities.NonMultiTenantEntity.class);
    verify(query, never()).setParameter(
        eq(TenantNativeQuerySupport.PARAM_NAME), eq(1L));
  }

  @Test
  void shouldAppendForManagedType_delegatesToJavaType() {
    bindPrincipal(apiTenantPrincipal(33L));
    EntityType<?> entityType = mock(EntityType.class);
    doReturn(MtIsolationEntities.MultiTenantEntity.class).when(entityType).getJavaType();
    assertTrue(TenantNativeQuerySupport.shouldAppendForManagedType(entityType));
  }

  @Test
  void appendClause_noOpWhenNotMultiTenantEntity() {
    bindPrincipal(apiTenantPrincipal(1L));
    StringBuilder sql = new StringBuilder("SELECT 1");
    TenantNativeQuerySupport.appendQualifiedTenantClause(sql, "x",
        MtIsolationEntities.NonMultiTenantEntity.class);
    assertFalse(sql.toString().contains("tenant_id"));
  }

  @Test
  void nativePath_doesNotThrowWhenCtrlOffEvenForMultiTenantEntity() {
    bindPrincipal(multiTenantOff(99L));
    assertDoesNotThrow(
        () ->
            TenantNativeQuerySupport.shouldAppendTenantPredicate(
                MtIsolationEntities.MultiTenantEntity.class));
  }
}
