package cloud.xcan.angus.core.spring.env.checker;

import static cloud.xcan.angus.spec.utils.ObjectUtils.getCauseMessage;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.enums.RedisDeployment;
import cloud.xcan.angus.remote.message.SysException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisChecker {

  public static void checkConnection(RedisDeployment deployment, String host, int port,
      String password, String sentinelMaster, String nodes) {
    switch (deployment) {
      case SINGLE:
        checkSingleInstanceConfig(host, port, password);
        break;
      case SENTINEL:
        checkSentinelConfig(password, sentinelMaster, nodes);
        break;
      case CLUSTER:
        checkClusterValid(password, nodes);
    }
  }

  public static void checkSingleInstanceConfig(String host, int port, String password) {
    try {
      RedisURI redisUri = Builder.redis(host, port)
          .withSsl(false)
          .withPassword(isEmpty(password) ? null : password.toCharArray())
          .withDatabase(0)
          .build();
      RedisClient redisClient = RedisClient.create(redisUri);
      StatefulRedisConnection<String, String> connection = redisClient.connect();
      connection.close();
      redisClient.shutdown();
    } catch (Exception e) {
      String message = getCauseMessage(e);
      log.error("Configure and get single redis connection error: {}", message);
      throw SysException.of("Configure and get single redis connection error: " + message);
    }
  }

  public static void checkSentinelConfig(String password, String sentinelMaster, String srcNode) {
    try {
      if (isEmpty(sentinelMaster)) {
        throw new IllegalArgumentException("The redis sentinel master id is not configured");
      }

      String[] nodes = srcNode.split(",");
      String[] node1 = nodes[0].split(":");
      Builder builder = Builder.sentinel(node1[0], Integer.parseInt(node1[1]));
      if (nodes.length > 1) {
        for (int i = 1; i < nodes.length; i++) {
          String[] node0 = nodes[i].split(":");
          builder.withSentinel(node0[0], Integer.parseInt(node0[1]));
        }
      }
      RedisURI redisUri = builder.withSsl(false)
          .withSentinelMasterId(sentinelMaster)
          .withPassword(isEmpty(password) ? null : password.toCharArray())
          .withDatabase(0)
          .build();
      RedisClient redisClient = RedisClient.create(redisUri);
      StatefulRedisConnection<String, String> connection = redisClient.connect();
      connection.close();
      redisClient.shutdown();
    } catch (Exception e) {
      String message = getCauseMessage(e);
      log.error("Configure and get sentinel redis connection error: {}", message);
      throw SysException.of("Configure and get sentinel redis connection error: " + message);
    }
  }

  public static void checkClusterValid(String password, String srcNode) {
    try {
      if (isEmpty(srcNode)) {
        throw new IllegalArgumentException("The redis nodes is not configured");
      }

      String[] nodes = srcNode.split(",");
      List<RedisURI> nodeUris = new ArrayList<>();
      for (String node : nodes) {
        String[] node0 = node.split(":");
        nodeUris.add(Builder.redis(node0[0], Integer.parseInt(node0[1]))
            .withPassword(isEmpty(password) ? null : password.toCharArray())
            .build());
      }
      RedisClusterClient clusterClient = RedisClusterClient.create(nodeUris);
      StatefulRedisClusterConnection<String, String> connection = clusterClient.connect();
      connection.close();
      clusterClient.shutdown();
    } catch (Exception e) {
      String message = getCauseMessage(e);
      log.error("Configure and get cluster redis connection error: {}", message);
      throw SysException.of("Configure and get cluster redis connection error: " + message);
    }
  }

}
