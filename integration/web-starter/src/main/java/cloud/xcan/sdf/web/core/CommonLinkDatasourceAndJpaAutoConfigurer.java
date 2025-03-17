package cloud.xcan.sdf.web.core;

import cloud.xcan.sdf.core.jpa.repository.BaseRepositoryImpl;
import cloud.xcan.sdf.core.spring.condition.MySqlEnvCondition;
import cloud.xcan.sdf.core.spring.condition.PostgresEnvCondition;
import cloud.xcan.sdf.datasource.config.DataSourceExtraProperties;
import cloud.xcan.sdf.datasource.config.DataSourceProperties;
import cloud.xcan.sdf.datasource.config.HikariProperties;
import cloud.xcan.sdf.jpa.CommonLinkHibernateJpaConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for UC View Repository.
 *
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class,
    SessionImplementor.class})
@EnableConfigurationProperties({JpaProperties.class, DataSourceExtraProperties.class,
    HikariProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@Import(CommonLinkHibernateJpaConfiguration.class)
@ConditionalOnProperty(name = "xcan.datasource.commonlink.enabled", havingValue = "true")
public class CommonLinkDatasourceAndJpaAutoConfigurer {

  @Bean("commonLinkDataSourceProperties")
  @ConfigurationProperties(prefix = "xcan.datasource.commonlink.mysql")
  @Conditional(MySqlEnvCondition.class)
  public DataSourceProperties loadCommonLinkMySqlDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean("commonLinkDataSourceProperties")
  @ConfigurationProperties(prefix = "xcan.datasource.commonlink.postgresql")
  @Conditional(PostgresEnvCondition.class)
  public DataSourceProperties loadCommonLinkPgDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "commonLinkDataSource")
  public DataSource commonLinkDataSource(
      @Qualifier("commonLinkDataSourceProperties") DataSourceProperties commonLinkDataSourceProperties,
      HikariProperties hikariProperties) {
    HikariDataSource dataSource = commonLinkDataSourceProperties.initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
    dataSource.setPoolName(hikariProperties.getPoolName() + "CommonLink");
    dataSource.setMaximumPoolSize(hikariProperties.getMaximumPoolSize());
    dataSource.setMinimumIdle(hikariProperties.getMinimumIdle());
    dataSource.setMaxLifetime(hikariProperties.getMaxLifetime());
    dataSource.setLeakDetectionThreshold(30000);
    dataSource.setConnectionTimeout(hikariProperties.getConnectionTimeout());
    dataSource.setValidationTimeout(hikariProperties.getValidationTimeout());
    dataSource.setIdleTimeout(hikariProperties.getIdleTimeout());
    dataSource.setInitializationFailTimeout(hikariProperties.getInitializationFailTimeout());
    dataSource.setAutoCommit(hikariProperties.isAutoCommit());
    dataSource.setReadOnly(hikariProperties.isReadOnly());
    dataSource.setConnectionTestQuery(hikariProperties.getConnectionTestQuery());
    return dataSource;
  }

  @Bean(name = "commonLinkEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean commonLinkEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("commonLinkDataSource") DataSource commonLinkDataSource) {
    return builder
        .dataSource(commonLinkDataSource)
        .packages("cloud.xcan.sdf.api.commonlink")
        //.properties(properties)
        .build();
  }

  @Bean(name = "commonLinkTransactionManager")
  public PlatformTransactionManager commonLinkTransactionManager(
      @Qualifier("commonLinkEntityManagerFactory") EntityManagerFactory commonLinkEntityManagerFactory) {
    return new JpaTransactionManager(commonLinkEntityManagerFactory);
  }

  @EnableTransactionManagement
  @EnableJpaRepositories(
      repositoryBaseClass = BaseRepositoryImpl.class,
      entityManagerFactoryRef = "commonLinkEntityManagerFactory",
      transactionManagerRef = "commonLinkTransactionManager",
      basePackages = {"cloud.xcan.sdf.api.commonlink"})
  public static class JpaEnableCommonLinkConfiguration {

  }
}
