package cloud.xcan.angus.persistence.jpa;

import static org.springframework.util.StringUtils.toStringArray;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Secondary JPA configuration for the common-link datasource.
 *
 * <p>Shares the same {@link PersistenceManagedTypes} (entity scan result) as the primary EMF,
 * but connects to a separate {@code commonLinkDataSource}. All repositories remain bound to
 * the primary {@code entityManagerFactory}; this factory is used only when common-link
 * operations need to run against a different database.
 */
@ConditionalOnProperty(name = "angus.datasource.commonlink.enabled", havingValue = "true")
@Configuration(proxyBeanMethods = false)
public class CommonLinkJpaHibernateConfiguration {

  @Bean(name = "commonLinkEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("commonLinkDataSource") DataSource commonLinkDataSource,
      JpaProperties jpaProperties, PersistenceManagedTypes persistenceManagedTypes) {
    List<String> mappingResources = jpaProperties.getMappingResources();
    return builder
        .dataSource(commonLinkDataSource)
        .managedTypes(persistenceManagedTypes)
        .mappingResources(
            (mappingResources != null && !mappingResources.isEmpty())
                ? toStringArray(mappingResources)
                : null)
        .build();
  }

  @Bean(name = "commonLinkTransactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier("commonLinkEntityManagerFactory") EntityManagerFactory commonLinkEntityManagerFactory,
      ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
    JpaTransactionManager transactionManager = new JpaTransactionManager(
        commonLinkEntityManagerFactory);
    transactionManagerCustomizers.ifAvailable(
        (customizers) -> customizers.customize(transactionManager));
    return transactionManager;
  }

}
