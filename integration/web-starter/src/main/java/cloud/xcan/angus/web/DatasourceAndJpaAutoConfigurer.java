package cloud.xcan.angus.web;


import cloud.xcan.angus.core.jpa.repository.SimpleSummaryRepository;
import cloud.xcan.angus.core.jpa.repository.SummaryRepository;
import cloud.xcan.angus.core.spring.condition.MySqlEnvCondition;
import cloud.xcan.angus.core.spring.condition.PostgresEnvCondition;
import cloud.xcan.angus.datasource.config.DataSourceExtraProperties;
import cloud.xcan.angus.datasource.config.DataSourceProperties;
import cloud.xcan.angus.datasource.config.HikariProperties;
import cloud.xcan.angus.jpa.HibernateJpaConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
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
public class DatasourceAndJpaAutoConfigurer {

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
    HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
    dataSource.setPoolName(hikariProperties.getPoolName());
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

  @Bean
  @Primary
  public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
    return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
  }

  @Primary
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder, @Qualifier("dataSource") DataSource dataSource,
      DataSourceExtraProperties jpaExtraProperties) {
    return builder
        .dataSource(dataSource)
        .packages(jpaExtraProperties.getEntityPackages())
        //.properties(properties)
        .build();
  }

  @Primary
  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean
  public DataSourceInitializer dataSourceInitializer(@Qualifier("dataSource") DataSource dataSource,
      DataSourceProperties dataSourceProperties) {
    ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
    List<String> schemaScripts = dataSourceProperties.getSchema();
    if (!CollectionUtils.isEmpty(schemaScripts)) {
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
