package cloud.xcan.angus.sharding;

import cloud.xcan.angus.sharding.annotation.Sharding;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.resolver.ShardKeyResolver;
import cloud.xcan.angus.sharding.strategy.ShardingStrategy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AOP aspect that intercepts methods annotated with {@link Sharding}, resolves the target DB shard
 * and table index, sets the {@link ShardContext} for the current thread, and clears it afterwards.
 *
 * <p>Shard-key extraction delegates to an ordered chain of {@link ShardKeyResolver} beans.
 * Custom resolvers can be registered as Spring beans and will automatically be included in the
 * chain (lower {@link ShardKeyResolver#getOrder()} value = higher priority).
 */
@Slf4j
@Aspect
public class ShardingAspect {

  public static final String SHARD_DS_PREFIX = "shard";
  public static final String SHARD_DS_SUFFIX = "DataSource";

  private final ShardingProperties properties;
  private final ShardingStrategy strategy;
  private final List<ShardKeyResolver> resolvers;

  public ShardingAspect(ShardingProperties properties, ShardingStrategy strategy,
      List<ShardKeyResolver> resolvers) {
    this.properties = properties;
    this.strategy = strategy;
    this.resolvers = resolvers == null ? Collections.emptyList()
        : resolvers.stream().sorted(Comparator.comparingInt(ShardKeyResolver::getOrder)).toList();
  }

  @Around("@annotation(sharding)")
  public Object around(ProceedingJoinPoint joinPoint, Sharding sharding) throws Throwable {
    try {
      ShardInfo info = resolveShardInfo(joinPoint, sharding);
      ShardContext.set(info);
      return joinPoint.proceed();
    } finally {
      ShardContext.clear();
    }
  }

  // ── Package-private for unit testing ────────────────────────────────────

  ShardInfo resolveShardInfo(ProceedingJoinPoint joinPoint, Sharding sharding) {
    long shardKeyValue = extractKey(joinPoint, sharding, sharding.shardKey());
    int dbIndex = strategy.computeDbIndex(shardKeyValue, properties.getShardDbCount());
    String dsKey = SHARD_DS_PREFIX + dbIndex + SHARD_DS_SUFFIX;

    long tableIndex = -1;
    if (properties.isEnableTableSecondaryIndex()) {
      String tableKeyField = sharding.tableKey().isEmpty()
          ? sharding.shardKey() : sharding.tableKey();
      long tableKeyValue = tableKeyField.equals(sharding.shardKey())
          ? shardKeyValue
          : extractKey(joinPoint, sharding, tableKeyField);
      if (tableKeyValue >= 0) {
        tableIndex = strategy.computeTableIndex(tableKeyValue, properties.getShardTableCount());
      }
    }
    return new ShardInfo(shardKeyValue, dsKey, tableIndex);
  }

  /**
   * Delegates key extraction to the resolver chain; returns {@code 0} when no resolver matches.
   */
  long extractKey(ProceedingJoinPoint joinPoint, Sharding sharding, String fieldName) {
    Object[] args = joinPoint.getArgs();
    String[] paramNames = null;
    if (joinPoint.getSignature() instanceof MethodSignature ms) {
      paramNames = ms.getParameterNames();
    }

    for (ShardKeyResolver resolver : resolvers) {
      Long val = resolver.resolve(args, paramNames, sharding, fieldName);
      if (val != null) {
        return val;
      }
    }

    log.debug("No resolver produced a shard key for field='{}' on {}.{}; defaulting to 0",
        fieldName,
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName());
    return 0L;
  }
}
