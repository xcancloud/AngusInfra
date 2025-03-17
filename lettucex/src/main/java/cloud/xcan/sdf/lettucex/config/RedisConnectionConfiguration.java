package cloud.xcan.sdf.lettucex.config;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Base Redis connection configuration.
 */
abstract class RedisConnectionConfiguration {

  private final RedisProperties properties;

  private final RedisSentinelConfiguration sentinelConfiguration;

  private final RedisClusterConfiguration clusterConfiguration;

  protected RedisConnectionConfiguration(RedisProperties properties,
      ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
      ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
    this.properties = properties;
    this.sentinelConfiguration = sentinelConfigurationProvider.getIfAvailable();
    this.clusterConfiguration = clusterConfigurationProvider.getIfAvailable();
  }

  protected final RedisStandaloneConfiguration getSingleConfig() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    if (StringUtils.hasText(this.properties.getUrl())) {
      ConnectionInfo connectionInfo = parseUrl(this.properties.getUrl());
      config.setHostName(connectionInfo.getHostName());
      config.setPort(connectionInfo.getPort());
      config.setUsername(connectionInfo.getUsername());
      config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
    } else {
      config.setHostName(this.properties.getHost());
      config.setPort(this.properties.getPort());
      config.setUsername(this.properties.getUsername());
      config.setPassword(RedisPassword.of(this.properties.getPassword()));
    }
    config.setDatabase(this.properties.getDatabase());
    return config;
  }

  protected final RedisSentinelConfiguration getSentinelConfig() {
    if (this.sentinelConfiguration != null) {
      return this.sentinelConfiguration;
    }
    if (!properties.getDeployment().isSentinel()){
      return null;
    }
    RedisProperties.Sentinel sentinelProperties = this.properties.getSentinel();
    if (sentinelProperties == null || isEmpty(sentinelProperties.getNodes())) {
      throw new IllegalArgumentException("Sentinel nodes is missing");
    }
    RedisSentinelConfiguration config = new RedisSentinelConfiguration();
    config.master(sentinelProperties.getMaster());
    config.setSentinels(createSentinels(sentinelProperties));
    config.setUsername(this.properties.getUsername());
    if (this.properties.getPassword() != null) {
      config.setPassword(RedisPassword.of(this.properties.getPassword()));
    }
    if (sentinelProperties.getPassword() != null) {
      config.setSentinelPassword(RedisPassword.of(sentinelProperties.getPassword()));
    }
    config.setDatabase(this.properties.getDatabase());
    return config;
  }

  /**
   * Create a {@link RedisClusterConfiguration} if necessary.
   *
   * @return {@literal null} if no cluster settings are set.
   */
  protected final RedisClusterConfiguration getClusterConfiguration() {
    if (this.clusterConfiguration != null) {
      return this.clusterConfiguration;
    }
    if (!properties.getDeployment().isCluster()){
      return null;
    }
    RedisProperties.Cluster clusterProperties = this.properties.getCluster();
    if (clusterProperties == null || isEmpty(clusterProperties.getNodes())) {
      throw new IllegalArgumentException("Cluster nodes is missing");
    }
    RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
    if (clusterProperties.getMaxRedirects() != null) {
      config.setMaxRedirects(clusterProperties.getMaxRedirects());
    }
    config.setUsername(this.properties.getUsername());
    if (this.properties.getPassword() != null) {
      config.setPassword(RedisPassword.of(this.properties.getPassword()));
    }
    return config;
  }

  protected final RedisProperties getProperties() {
    return this.properties;
  }

  private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
    List<RedisNode> nodes = new ArrayList<>();
    for (String node : sentinel.getNodes()) {
      try {
        String[] parts = StringUtils.split(node, ":");
        Assert.state(parts.length == 2, "Must be defined as 'host:port'");
        nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
      } catch (RuntimeException ex) {
        throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", ex);
      }
    }
    return nodes;
  }

  protected ConnectionInfo parseUrl(String url) {
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
        throw new RedisUrlSyntaxException(url);
      }
      boolean useSsl = ("rediss".equals(scheme));
      String username = null;
      String password = null;
      if (uri.getUserInfo() != null) {
        String candidate = uri.getUserInfo();
        int index = candidate.indexOf(':');
        if (index >= 0) {
          username = candidate.substring(0, index);
          password = candidate.substring(index + 1);
        } else {
          password = candidate;
        }
      }
      return new ConnectionInfo(uri, useSsl, username, password);
    } catch (URISyntaxException ex) {
      throw new RedisUrlSyntaxException(url, ex);
    }
  }

  static class ConnectionInfo {

    private final URI uri;

    private final boolean useSsl;

    private final String username;

    private final String password;

    ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
      this.uri = uri;
      this.useSsl = useSsl;
      this.username = username;
      this.password = password;
    }

    boolean isUseSsl() {
      return this.useSsl;
    }

    String getHostName() {
      return this.uri.getHost();
    }

    int getPort() {
      return this.uri.getPort();
    }

    String getUsername() {
      return this.username;
    }

    String getPassword() {
      return this.password;
    }

  }

}
