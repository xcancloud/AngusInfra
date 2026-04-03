package cloud.xcan.angus.sharding;

import cloud.xcan.angus.sharding.registry.InMemoryShardTableRegistry;
import cloud.xcan.angus.sharding.resolver.DefaultShardKeyResolver;
import cloud.xcan.angus.sharding.config.HikariShardingProperties;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.registry.JdbcShardTableRegistry;
import cloud.xcan.angus.sharding.resolver.ShardKeyResolver;
import cloud.xcan.angus.sharding.strategy.ModuloShardingStrategy;
import cloud.xcan.angus.sharding.strategy.ShardingStrategy;
import cloud.xcan.angus.sharding.table.ShardTableManager;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Auto-configuration for the Angus Sharding framework.
 *
 * <p>Creates a routing data source that distributes queries across multiple shard databases,
 * configures JPA for the sharded entities, and wires up the AOP aspect for automatic shard context
 * resolution.
 *
 * <p>Activated by the property {@code angus.sharding.enabled=true}.
 */
@Slf4j
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class,
    SessionImplementor.class})
@ConditionalOnProperty(prefix = "angus.sharding", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({JpaProperties.class, ShardingProperties.class,
    HikariShardingProperties.class})
@EnableTransactionManagement
public class ShardingAutoConfiguration {

  public static final String ROUTING_DATASOURCE = "shardingRoutingDataSource";
  public static final String ENTITY_MANAGER_FACTORY = "shardingEntityManagerFactory";
  public static final String TRANSACTION_MANAGER = "shardingTransactionManager";

  @Bean
  @ConditionalOnMissingBean(ShardingStrategy.class)
  public ShardingStrategy shardingStrategy() {
    return new ModuloShardingStrategy();
  }

  @Bean(name = ROUTING_DATASOURCE)
  public DataSource shardingRoutingDataSource(ShardingProperties props,
      HikariShardingProperties hikari, DataSource primaryDataSource) {
    int shardDbCount = props.getShardDbCount();
    if (shardDbCount < 1 || shardDbCount > ShardingProperties.MAX_SHARD_DB_COUNT) {
      throw new IllegalArgumentException(
          "shardDbCount must be between 1 and " + ShardingProperties.MAX_SHARD_DB_COUNT);
    }
    if (props.getShardTableCount() < 1
        || props.getShardTableCount() > ShardingProperties.MAX_SHARD_TABLE_COUNT) {
      throw new IllegalArgumentException(
          "shardTableCount must be between 1 and " + ShardingProperties.MAX_SHARD_TABLE_COUNT);
    }

    ShardingRoutingDataSource routing = new ShardingRoutingDataSource();
    Map<Object, Object> targetDataSources = new HashMap<>();

    for (int i = 0; i < shardDbCount; i++) {
      String key = ShardingAspect.SHARD_DS_PREFIX + i + ShardingAspect.SHARD_DS_SUFFIX;
      log.info("Creating shard data source: {}", key);
      targetDataSources.put(key, createShardDataSource(i, props, hikari));
    }

    targetDataSources.put(ShardContext.MASTER_DATASOURCE_KEY, primaryDataSource);
    routing.setDefaultTargetDataSource(primaryDataSource);
    routing.setTargetDataSources(targetDataSources);
    return routing;
  }

  @Bean(name = ENTITY_MANAGER_FACTORY)
  public LocalContainerEntityManagerFactoryBean shardingEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier(ROUTING_DATASOURCE) DataSource routingDataSource,
      ShardingProperties props, JpaProperties jpaProperties,
      ShardingTableInterceptor interceptor) {
    Map<String, String> jpaProps = new HashMap<>(jpaProperties.getProperties());
    jpaProps.put("hibernate.physical_naming_strategy",
        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
    jpaProps.put("hibernate.session_factory.statement_inspector",
        "shardingTableInterceptor");

    String[] packages = props.getEntityPackages();
    if (packages == null || packages.length == 0) {
      throw new IllegalArgumentException(
          "angus.sharding.entity-packages must be configured for JPA entity scanning");
    }

    return builder
        .dataSource(routingDataSource)
        .properties(jpaProps)
        .packages(packages)
        .persistenceUnit("sharding")
        .build();
  }

  @Bean(name = TRANSACTION_MANAGER)
  public PlatformTransactionManager shardingTransactionManager(
      @Qualifier(ENTITY_MANAGER_FACTORY) EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }

  @Bean
  @ConditionalOnMissingBean(ShardingTableInterceptor.class)
  public ShardingTableInterceptor shardingTableInterceptor(ShardingProperties props) {
    ShardingTableInterceptor interceptor = new ShardingTableInterceptor(props);
    String[] packages = props.getEntityPackages();
    if (packages != null && packages.length > 0) {
      interceptor.scanAndRegister(packages);
    }
    return interceptor;
  }

  // ── ShardKeyResolver chain ───────────────────────────────────────────────

  @Bean
  @ConditionalOnMissingBean(DefaultShardKeyResolver.class)
  public DefaultShardKeyResolver defaultShardKeyResolver() {
    return new DefaultShardKeyResolver();
  }

  @Bean
  public ShardingAspect shardingAspect(ShardingProperties props, ShardingStrategy strategy,
      List<ShardKeyResolver> resolvers) {
    return new ShardingAspect(props, strategy, resolvers);
  }

  // ── ShardTableRegistry ───────────────────────────────────────────────────

  /**
   * Registers an in-memory registry when no durable alternative is present and the JDBC registry is
   * not explicitly requested.
   */
  @Bean
  @ConditionalOnMissingBean(ShardTableRegistry.class)
  @ConditionalOnProperty(prefix = "angus.sharding", name = "table-registry-enabled",
      havingValue = "false", matchIfMissing = true)
  public ShardTableRegistry inMemoryShardTableRegistry() {
    return new InMemoryShardTableRegistry();
  }

  /**
   * Registers a JDBC-backed registry when {@code angus.sharding.table-registry-enabled=true}.
   */
  @Bean
  @ConditionalOnMissingBean(ShardTableRegistry.class)
  @ConditionalOnProperty(prefix = "angus.sharding", name = "table-registry-enabled",
      havingValue = "true")
  public ShardTableRegistry jdbcShardTableRegistry(ShardingProperties props,
      DataSource primaryDataSource) {
    log.info("Activating JDBC shard table registry on table '{}'.", props.getTableRegistryTable());
    return new JdbcShardTableRegistry(primaryDataSource, props.getTableRegistryTable());
  }

  // ── ShardTableManager ────────────────────────────────────────────────────

  @Bean
  @ConditionalOnMissingBean(ShardTableManager.class)
  @ConditionalOnBean(name = ROUTING_DATASOURCE)
  public ShardTableManager shardTableManager(
      @Qualifier(ROUTING_DATASOURCE) DataSource routingDataSource,
      ShardingProperties props, ShardingTableInterceptor interceptor,
      ShardTableRegistry registry) {
    SqlTemplateTableManager manager = new SqlTemplateTableManager(
        routingDataSource,
        props.getSchemaPath(),
        props.getTemplateTableNames() != null
            ? Arrays.asList(props.getTemplateTableNames()) : null,
        registry);
    interceptor.setTableManager(manager);
    return manager;
  }

  private HikariDataSource createShardDataSource(int index, ShardingProperties props,
      HikariShardingProperties hk) {
    DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .username(props.getUsername())
        .password(props.getPassword());

    if ("postgres".equalsIgnoreCase(props.getDbType())
        || "postgresql".equalsIgnoreCase(props.getDbType())) {
      builder.driverClassName(props.getPostgresql().getDriverClassName())
          .url(getUrl(props.getPostgresql().getUrls(), index));
    } else {
      builder.driverClassName(props.getMysql().getDriverClassName())
          .url(getUrl(props.getMysql().getUrls(), index));
    }

    HikariDataSource ds = builder.build();
    ds.setPoolName(hk.getPoolName() + "Shard" + index);
    if (hk.getMaximumPoolSize() > 0) {
      ds.setMaximumPoolSize(hk.getMaximumPoolSize());
    }
    if (hk.getMinimumIdle() > 0) {
      ds.setMinimumIdle(hk.getMinimumIdle());
    }
    ds.setMaxLifetime(hk.getMaxLifetime());
    ds.setConnectionTimeout(hk.getConnectionTimeout());
    ds.setValidationTimeout(hk.getValidationTimeout());
    ds.setIdleTimeout(hk.getIdleTimeout());
    ds.setInitializationFailTimeout(hk.getInitializationFailTimeout());
    ds.setAutoCommit(hk.isAutoCommit());
    ds.setReadOnly(hk.isReadOnly());
    ds.setConnectionTestQuery(hk.getConnectionTestQuery());
    ds.setLeakDetectionThreshold(hk.getLeakDetectionThreshold());
    return ds;
  }

  private String getUrl(String[] urls, int index) {
    if (urls == null || urls.length == 0) {
      throw new IllegalArgumentException(
          "Shard database URLs not configured for index " + index);
    }
    if (index >= urls.length) {
      throw new IllegalArgumentException(
          "Not enough shard database URLs configured. Need at least " + (index + 1)
              + " but found " + urls.length);
    }
    return urls[index];
  }
}
