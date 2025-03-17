package cloud.xcan.sdf.lettucex.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

/**
 * Redis connection configuration using Lettuce.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = "xcan.redis.enabled", havingValue = "true", matchIfMissing = false)
public class LettuceConnectionConfiguration extends RedisConnectionConfiguration {

  LettuceConnectionConfiguration(RedisProperties properties,
      ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
      ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
    super(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
  }

  @Bean(destroyMethod = "shutdown")
  @ConditionalOnMissingBean(ClientResources.class)
  DefaultClientResources lettuceClientResources() {
    return DefaultClientResources.create();
  }

  @Bean
  @ConditionalOnMissingBean(RedisConnectionFactory.class)
  LettuceConnectionFactory redisConnectionFactory(
      ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
      ClientResources clientResources) {
    LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(
        builderCustomizers, clientResources, getProperties().getLettuce().getPool());
    return createLettuceConnectionFactory(clientConfig);
  }

  private LettuceConnectionFactory createLettuceConnectionFactory(
      LettuceClientConfiguration clientConfiguration) {
    if (getSentinelConfig() != null) {
      return new LettuceConnectionFactory(getSentinelConfig(), clientConfiguration);
    }
    if (getClusterConfiguration() != null) {
      return new LettuceConnectionFactory(getClusterConfiguration(), clientConfiguration);
    }
    return new LettuceConnectionFactory(getSingleConfig(), clientConfiguration);
  }

  private LettuceClientConfiguration getLettuceClientConfiguration(
      ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
      ClientResources clientResources, Pool pool) {
    LettuceClientConfigurationBuilder builder = createBuilder(pool);
    applyProperties(builder);
    if (StringUtils.hasText(getProperties().getUrl())) {
      customizeConfigurationFromUrl(builder);
    }
    builder.clientOptions(createClientOptions());
    builder.clientResources(clientResources);
    builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }

  private LettuceClientConfigurationBuilder createBuilder(Pool pool) {
    if (pool == null) {
      return LettuceClientConfiguration.builder();
    }
    return new PoolBuilderFactory().createBuilder(pool);
  }

  private LettuceClientConfigurationBuilder applyProperties(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    if (getProperties().getSsl().isEnabled()) {
      builder.useSsl();
    }
    if (getProperties().getTimeout() != null) {
      builder.commandTimeout(getProperties().getTimeout());
    }
    if (getProperties().getLettuce() != null) {
      RedisProperties.Lettuce lettuce = getProperties().getLettuce();
      if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
        builder.shutdownTimeout(getProperties().getLettuce().getShutdownTimeout());
      }
    }
    if (StringUtils.hasText(getProperties().getClientName())) {
      builder.clientName(getProperties().getClientName());
    }
    return builder;
  }

  private ClientOptions createClientOptions() {
    ClientOptions.Builder builder = initializeClientOptionsBuilder();
    Duration connectTimeout = getProperties().getConnectTimeout();
    if (connectTimeout != null) {
      builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
    }
    return builder.timeoutOptions(TimeoutOptions.enabled()).build();
  }

  private ClientOptions.Builder initializeClientOptionsBuilder() {
    if (getProperties().getCluster() != null) {
      ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
      Refresh refreshProperties = getProperties().getLettuce().getCluster().getRefresh();
      Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
          .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
      if (refreshProperties.getPeriod() != null) {
        refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
      }
      if (refreshProperties.isAdaptive()) {
        refreshBuilder.enableAllAdaptiveRefreshTriggers();
      }
      return builder.topologyRefreshOptions(refreshBuilder.build());
    }
    return ClientOptions.builder();
  }

  private void customizeConfigurationFromUrl(
      LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
    ConnectionInfo connectionInfo = parseUrl(getProperties().getUrl());
    if (connectionInfo.isUseSsl()) {
      builder.useSsl();
    }
  }

  /**
   * Inner class to allow optional commons-pool2 dependency.
   */
  private static class PoolBuilderFactory {

    LettuceClientConfigurationBuilder createBuilder(Pool properties) {
      return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
    }

    private GenericObjectPoolConfig<?> getPoolConfig(Pool properties) {
      GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
      config.setMaxTotal(properties.getMaxActive());
      config.setMaxIdle(properties.getMaxIdle());
      config.setMinIdle(properties.getMinIdle());
      if (properties.getTimeBetweenEvictionRuns() != null) {
        config.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRuns().toMillis());
      }
      if (properties.getMaxWait() != null) {
        config.setMaxWaitMillis(properties.getMaxWait().toMillis());
      }
      return config;
    }

  }

}
