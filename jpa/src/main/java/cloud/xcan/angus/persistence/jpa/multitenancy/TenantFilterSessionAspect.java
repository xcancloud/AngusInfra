package cloud.xcan.angus.persistence.jpa.multitenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Applies {@link TenantFilterApplicator} on {@code @Transactional} entry. Runs with low precedence
 * so the transaction interceptor has typically bound a synchronization / session already.
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class TenantFilterSessionAspect {

  @PersistenceContext
  private EntityManager entityManager;

  @Before("@annotation(org.springframework.transaction.annotation.Transactional) "
      + "|| @within(org.springframework.transaction.annotation.Transactional)")
  public void syncTenantFilterOnTransactionalBoundary() {
    Session session = entityManager.unwrap(Session.class);
    TenantFilterApplicator.syncSession(session);
  }
}
