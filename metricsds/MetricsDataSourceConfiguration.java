package cloud.xcan.angus.core.tester.infra.metricsds;

import cloud.xcan.angus.core.jpa.repository.BaseRepositoryImpl;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceExtraProperties;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceProperties;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsHikariProperties;
import cloud.xcan.angus.jpa.HibernateJpaConfiguration;
import cloud.xcan.angus.spec.experimental.Assert;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class,
    SessionImplementor.class})
@EnableConfigurationProperties({JpaProperties.class, MetricsDataSourceProperties.class,
    MetricsHikariProperties.class, MetricsDataSourceExtraProperties.class})
@Import({HibernateJpaConfiguration.class, JpaEnableMetricsMysqlConfiguration.class,
    MetricsJpaEnableMetricsPostgresConfiguration.class})
public class MetricsDataSourceConfiguration {

  public static final String MASTER_DATASOURCE_KEY = "dataSource";
  public static final String METRICS_DATASOURCE_PREFIX = "metrics";
  public static final String METRICS_DATASOURCE_SUFFIX = "DataSource";
  public static final String METRICS_ENTITY_MANAGER_FACTORY = "metricsEntityManagerFactory";

  public static final int MAX_SHARD_DB_NUM = 10;
  public static final int MAX_SHARD_TABLE_NUM = 50;

  @Resource
  private JpaProperties jpaProperties;

  @Resource
  private MetricsDataSourceProperties metricsDataSourceProperties;

  @Resource(name = MASTER_DATASOURCE_KEY)
  private DataSource dataSource;

  @Bean(name = "routingDataSource")
  public DataSource routingDataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    int shardDbNum = mds.getShardDbNum();
    Assert.assertTrue(shardDbNum >= 1 && shardDbNum <= MAX_SHARD_DB_NUM,
        "shardDbNum value range: 1-" + MAX_SHARD_DB_NUM);
    Assert.assertTrue(mds.getShardTabledNum() >= 1 && mds.getShardTabledNum() <= MAX_SHARD_TABLE_NUM,
        "shardTabledNum value range: 1-" + MAX_SHARD_TABLE_NUM);

    MetricsDynamicDataSourceRouter proxy = new MetricsDynamicDataSourceRouter();
    Map<Object, Object> targetDataSources = new HashMap<>();

    for (int i = 0; i < shardDbNum; i++) {
      String key = METRICS_DATASOURCE_PREFIX + i + METRICS_DATASOURCE_SUFFIX;
      log.info("Creating {} ...", key);
      targetDataSources.put(key, createDataSource(i, mds, mdse, mk));
    }

    targetDataSources.put(MASTER_DATASOURCE_KEY, dataSource);
    proxy.setDefaultTargetDataSource(dataSource);
    proxy.setTargetDataSources(targetDataSources);
    return proxy;
  }

  private HikariDataSource createDataSource(int index, MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .username(mds.getUsername())
        .password(mds.getPassword());
    if ("postgres".equalsIgnoreCase(mdse.getDbType())) {
      builder.driverClassName(mds.getPostgresql().getDriverClassName())
          .url(mds.getPostgresql().getUrls()[index]);
    } else {
      builder.driverClassName(mds.getMysql().getDriverClassName())
          .url(mds.getMysql().getUrls()[index]);
    }
    HikariDataSource ds = builder.build();
    ds.setPoolName(mk.getPoolName() + "Metrics" + index);
    ds.setMaximumPoolSize(mk.getMaximumPoolSize());
    ds.setMinimumIdle(mk.getMinimumIdle());
    ds.setMaxLifetime(mk.getMaxLifetime());
    ds.setLeakDetectionThreshold(30000);
    ds.setConnectionTimeout(mk.getConnectionTimeout());
    ds.setValidationTimeout(mk.getValidationTimeout());
    ds.setIdleTimeout(mk.getIdleTimeout());
    ds.setInitializationFailTimeout(mk.getInitializationFailTimeout());
    ds.setAutoCommit(mk.isAutoCommit());
    ds.setReadOnly(mk.isReadOnly());
    ds.setConnectionTestQuery(mk.getConnectionTestQuery());
    return ds;
  }

  @Bean(name = METRICS_ENTITY_MANAGER_FACTORY)
  public LocalContainerEntityManagerFactoryBean metricsEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("routingDataSource") DataSource routingDataSource) {
    Map<String, String> properties = jpaProperties.getProperties();
    properties.put("hibernate.physical_naming_strategy",
        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
    return builder
        .dataSource(routingDataSource)
        .properties(properties)
        .packages(metricsDataSourceProperties.getEntityPackages())
        .build();
  }

  @Bean(name = "metricsTransactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier(METRICS_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

}

@EnableTransactionManagement
@EnableJpaRepositories(
    repositoryBaseClass = BaseRepositoryImpl.class,
    entityManagerFactoryRef = MetricsDataSourceConfiguration.METRICS_ENTITY_MANAGER_FACTORY,
    transactionManagerRef = "metricsTransactionManager",
    basePackages = {"cloud.xcan.angus.core.tester.infra.persistence.mysql.metrics"})
@ConditionalOnProperty(name = "xcan.datasource.extra.dbType", havingValue = "mysql")
class JpaEnableMetricsMysqlConfiguration {

}

@EnableTransactionManagement
@EnableJpaRepositories(
    repositoryBaseClass = BaseRepositoryImpl.class,
    entityManagerFactoryRef = MetricsDataSourceConfiguration.METRICS_ENTITY_MANAGER_FACTORY,
    transactionManagerRef = "metricsTransactionManager",
    basePackages = {"cloud.xcan.angus.core.tester.infra.persistence.postgres.metrics"})
@ConditionalOnProperty(name = "xcan.datasource.extra.dbType", havingValue = "postgres")
class MetricsJpaEnableMetricsPostgresConfiguration {

}
