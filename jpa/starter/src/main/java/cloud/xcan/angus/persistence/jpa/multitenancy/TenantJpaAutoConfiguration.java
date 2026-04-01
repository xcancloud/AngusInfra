package cloud.xcan.angus.persistence.jpa.multitenancy;

import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
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
}
