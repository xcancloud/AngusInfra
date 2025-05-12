package cloud.xcan.angus.web;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.repository.SimpleSummaryRepository;
import cloud.xcan.angus.core.jpa.repository.SummaryRepository;
import cloud.xcan.angus.core.spring.condition.MySqlEnvCondition;
import cloud.xcan.angus.core.spring.condition.PostgresEnvCondition;
import cloud.xcan.angus.datasource.config.DataSourceExtraProperties;
import cloud.xcan.angus.datasource.config.DataSourceProperties;
import cloud.xcan.angus.datasource.config.HikariProperties;
import cloud.xcan.angus.jpa.HibernateJpaConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.CollectionUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Hibernate JPA.
 *
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class,
    SessionImplementor.class})
@EnableConfigurationProperties({JpaProperties.class, DataSourceExtraProperties.class,
    HikariProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@Import(HibernateJpaConfiguration.class)
@ConditionalOnProperty(name = "xcan.datasource.enabled", havingValue = "true")
public class JpaAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public PhysicalNamingStrategy physicalNamingStrategy() {
    return new CamelCaseToUnderscoresNamingStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  public ImplicitNamingStrategy implicitNamingStrategy() {
    return new ImplicitNamingStrategyJpaCompliantImpl();
  }

  @Primary
  @Bean("dataSourceProperties")
  @ConfigurationProperties(prefix = "xcan.datasource.mysql")
  @Conditional(MySqlEnvCondition.class)
  public DataSourceProperties loadMySqlDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Primary
  @Bean("dataSourceProperties")
  @ConfigurationProperties(prefix = "xcan.datasource.postgresql")
  @Conditional(PostgresEnvCondition.class)
  public DataSourceProperties loadPgDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Primary
  @Bean(name = "dataSource")
  public DataSource dataSource(DataSourceProperties dataSourceProperties,
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

  @Bean
  public DataSourceInitializer dataSourceInitializer(@Qualifier("dataSource") DataSource dataSource,
      DataSourceProperties dataSourceProperties) {
    ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
    List<String> schemaScripts = dataSourceProperties.getSchema();
    if (isNotEmpty(schemaScripts)) {
      for (String sql : schemaScripts) {
        rdp.addScript(new ClassPathResource(sql));
      }
    }
    List<String> dataScripts = dataSourceProperties.getData();
    if (!CollectionUtils.isEmpty(dataScripts)) {
      for (String sql : dataScripts) {
        rdp.addScript(new ClassPathResource(sql));
      }
    }
    rdp.setContinueOnError(true);
    rdp.setIgnoreFailedDrops(true);

    DataSourceInitializer dsi = new DataSourceInitializer();
    dsi.setDataSource(dataSource);
    dsi.setDatabasePopulator(rdp);
    return dsi;
  }

  /**
   * Gateway needs to be excluded!!! -> WebMvcConfigurer not found
   */
  @Bean
  @ConditionalOnProperty(name = "xcan.summary.enabled", havingValue = "true", matchIfMissing = true)
  public SummaryRepository summaryRepository() {
    return new SimpleSummaryRepository();
  }
}
