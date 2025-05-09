package cloud.xcan.angus.web;

import cloud.xcan.angus.core.jpa.repository.BaseRepositoryImpl;
import cloud.xcan.angus.core.spring.condition.MySqlEnvCondition;
import cloud.xcan.angus.core.spring.condition.PostgresEnvCondition;
import cloud.xcan.angus.datasource.config.DataSourceExtraProperties;
import cloud.xcan.angus.datasource.config.DataSourceProperties;
import cloud.xcan.angus.datasource.config.HikariProperties;
import cloud.xcan.angus.jpa.CommonLinkHibernateJpaConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
public class CommonLinkJpaAutoConfigurer {

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
      @Qualifier("commonLinkDataSourceProperties") DataSourceProperties dataSourceProperties,
      HikariProperties hikariProperties) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
    hikariConfig.setUsername(dataSourceProperties.getUsername());
    hikariConfig.setPassword(dataSourceProperties.getPassword());
    hikariConfig.setDriverClassName(dataSourceProperties.getDriverClassName());

    hikariConfig.setPoolName(hikariProperties.getPoolName());
    hikariConfig.setMaximumPoolSize(hikariProperties.getMaximumPoolSize());
    hikariConfig.setMinimumIdle(hikariProperties.getMinimumIdle());
    hikariConfig.setMaxLifetime(hikariProperties.getMaxLifetime());
    hikariConfig.setLeakDetectionThreshold(30000);
    hikariConfig.setConnectionTimeout(hikariProperties.getConnectionTimeout());
    hikariConfig.setValidationTimeout(hikariProperties.getValidationTimeout());
    hikariConfig.setIdleTimeout(hikariProperties.getIdleTimeout());
    hikariConfig.setInitializationFailTimeout(hikariProperties.getInitializationFailTimeout());
    hikariConfig.setAutoCommit(hikariProperties.isAutoCommit());
    hikariConfig.setReadOnly(hikariProperties.isReadOnly());
    hikariConfig.setConnectionTestQuery(hikariProperties.getConnectionTestQuery());

    return new HikariDataSource(hikariConfig);
  }

  @EnableTransactionManagement
  @EnableJpaRepositories(
      repositoryBaseClass = BaseRepositoryImpl.class,
      entityManagerFactoryRef = "commonLinkEntityManagerFactory",
      transactionManagerRef = "commonLinkTransactionManager",
      basePackages = {"cloud.xcan.angus.api.commonlink"})
  public static class JpaEnableCommonLinkConfiguration {

  }
}
