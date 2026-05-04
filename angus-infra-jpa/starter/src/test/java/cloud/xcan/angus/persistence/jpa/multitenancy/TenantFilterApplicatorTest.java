package cloud.xcan.angus.persistence.jpa.multitenancy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.Set;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantFilterApplicatorTest extends PrincipalTestSupport {

  private static Set<String> originalTenantTables;

  @BeforeAll
  static void captureTenantMetadata() {
    originalTenantTables = TenantMetadataTestSupport.currentTenantTableNames();
  }

  @BeforeEach
  void nonEmptyTenantTablesForScoping() {
    TenantMetadataTestSupport.installTenantTableNames(Set.of("mt_isolation_verify"));
  }

  @AfterEach
  void restoreTenantMetadata() {
    TenantMetadataTestSupport.installTenantTableNames(originalTenantTables);
  }

  @Test
  void shouldApplyTenantFilter_falseWhenPrincipalNull() {
    assertFalse(TenantFilterApplicator.shouldApplyTenantFilter(null));
  }

  @Test
  void shouldApplyTenantFilter_falseWhenMultiTenantCtrlOff() {
    bindPrincipal(multiTenantOff(100L));
    assertFalse(TenantFilterApplicator.shouldApplyTenantFilter(PrincipalContext.get()));
  }

  @Test
  void shouldApplyTenantFilter_falseWhenApiTypeDoesNotRequireTenantAndNoValidOpt() {
    bindPrincipal(viewNoOptTenant(100L));
    assertFalse(TenantFilterApplicator.shouldApplyTenantFilter(PrincipalContext.get()));
  }

  @Test
  void shouldApplyTenantFilter_trueWhenApiRequestWithCtrl() {
    bindPrincipal(apiTenantPrincipal(100L));
    assertTrue(TenantFilterApplicator.shouldApplyTenantFilter(PrincipalContext.get()));
  }

  @Test
  void shouldApplyTenantFilter_trueWhenApiRequestEvenIfTenantTablesEmpty() {
    TenantMetadataTestSupport.installTenantTableNames(Set.of());
    bindPrincipal(apiTenantPrincipal(100L));
    assertTrue(TenantFilterApplicator.shouldApplyTenantFilter(PrincipalContext.get()));
  }

  @Test
  void syncSession_enablesFilterAndSetsTenantParameter() {
    bindPrincipal(apiTenantPrincipal(99L));
    Session session = mock(Session.class);
    Filter hibernateFilter = mock(Filter.class);
    when(session.getEnabledFilter(TenantFilterNames.SCOPE)).thenReturn(null);
    when(session.enableFilter(TenantFilterNames.SCOPE)).thenReturn(hibernateFilter);

    TenantFilterApplicator.syncSession(session);

    verify(session).enableFilter(TenantFilterNames.SCOPE);
    verify(hibernateFilter).setParameter(TenantFilterNames.PARAM_TENANT_ID, 99L);
  }

  @Test
  void syncSession_disablesPreviousFilterThenRebinds() {
    bindPrincipal(apiTenantPrincipal(42L));
    Session session = mock(Session.class);
    Filter previous = mock(Filter.class);
    Filter next = mock(Filter.class);
    when(session.getEnabledFilter(TenantFilterNames.SCOPE)).thenReturn(previous);
    when(session.enableFilter(TenantFilterNames.SCOPE)).thenReturn(next);

    TenantFilterApplicator.syncSession(session);

    verify(session).disableFilter(TenantFilterNames.SCOPE);
    verify(next).setParameter(TenantFilterNames.PARAM_TENANT_ID, 42L);
  }

  @Test
  void syncSession_noOpWhenFilterNotApplicable() {
    bindPrincipal(multiTenantOff(10L));
    Session session = mock(Session.class);

    TenantFilterApplicator.syncSession(session);

    verify(session, never()).enableFilter(TenantFilterNames.SCOPE);
  }

  @Test
  void syncSession_throwsWhenFilterRequiredButTenantIdInvalid() {
    Principal bad = new Principal()
        .setMultiTenantCtrl(true)
        .setApiType(cloud.xcan.angus.api.enums.ApiType.API)
        .setTenantId(0L)
        .setOptTenantId(null);
    bindPrincipal(bad);
    Session session = mock(Session.class);
    when(session.getEnabledFilter(TenantFilterNames.SCOPE)).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> TenantFilterApplicator.syncSession(session));
  }
}
