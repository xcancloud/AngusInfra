package cloud.xcan.angus.persistence.jpa.multitenancy;

import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Registers tenant Hibernate filter synchronization for transactional boundaries.
 */
@AutoConfiguration
@ConditionalOnClass({Session.class, Aspect.class})
@EnableAspectJAutoProxy
public class TenantJpaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(TenantFilterSessionAspect.class)
  public TenantFilterSessionAspect tenantFilterSessionAspect() {
    return new TenantFilterSessionAspect();
  }

  /**
   * 在 {@code setMultiTenantCtrl} 变更后同步当前 Session Filter，
   * 避免事务入口 Aspect 已 enable Filter 后，业务侧关闭多租户标志却仍拼 tenant_id。
   */
  @Bean
  public MultiTenantCtrlSessionSyncRegistrar multiTenantCtrlSessionSyncRegistrar() {
    return new MultiTenantCtrlSessionSyncRegistrar();
  }

  static class MultiTenantCtrlSessionSyncRegistrar implements InitializingBean, DisposableBean {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void afterPropertiesSet() {
      PrincipalContextUtils.setMultiTenantCtrlSessionSync(ctrl -> {
        if (entityManager == null || !entityManager.isOpen()) {
          return;
        }
        // 仅在加入事务时 sync；事务外无 Session Filter 可同步
        if (!entityManager.isJoinedToTransaction()) {
          return;
        }
        TenantFilterApplicator.syncSession(entityManager.unwrap(Session.class));
      });
    }

    @Override
    public void destroy() {
      PrincipalContextUtils.setMultiTenantCtrlSessionSync(null);
    }
  }
}
