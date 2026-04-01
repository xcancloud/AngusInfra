package cloud.xcan.angus.sharding.autoconfigure;

import cloud.xcan.angus.sharding.annotation.Sharding;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.strategy.ShardingStrategy;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AOP aspect that intercepts repository methods annotated with {@link Sharding} and sets the
 * appropriate {@link ShardContext} before method execution.
 *
 * <p>The aspect resolves the shard key from method arguments by:
 * <ol>
 *   <li>Looking for a field matching {@link Sharding#shardKey()} on Iterable or entity arguments</li>
 *   <li>Falling back to the first {@code Long} argument</li>
 * </ol>
 */
@Slf4j
@Aspect
public class ShardingAspect {

  public static final String SHARD_DS_PREFIX = "shard";
  public static final String SHARD_DS_SUFFIX = "DataSource";

  private final ShardingProperties properties;
  private final ShardingStrategy strategy;

  public ShardingAspect(ShardingProperties properties, ShardingStrategy strategy) {
    this.properties = properties;
    this.strategy = strategy;
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

  ShardInfo resolveShardInfo(ProceedingJoinPoint joinPoint, Sharding sharding) {
    long shardKeyValue = extractShardKey(joinPoint, sharding.shardKey());

    int dbIndex = strategy.computeDbIndex(shardKeyValue, properties.getShardDbCount());
    String dsKey = SHARD_DS_PREFIX + dbIndex + SHARD_DS_SUFFIX;

    long tableIndex = -1;
    if (properties.isEnableTableSecondaryIndex()) {
      String tableKeyField = sharding.tableKey().isEmpty() ? sharding.shardKey() : sharding.tableKey();
      long tableKeyValue = tableKeyField.equals(sharding.shardKey())
          ? shardKeyValue
          : extractShardKey(joinPoint, tableKeyField);
      if (tableKeyValue >= 0) {
        tableIndex = strategy.computeTableIndex(tableKeyValue, properties.getShardTableCount());
      }
    }

    return new ShardInfo(shardKeyValue, dsKey, tableIndex);
  }

  long extractShardKey(ProceedingJoinPoint joinPoint, String fieldName) {
    Object[] args = joinPoint.getArgs();
    if (args == null || args.length == 0) {
      return 0;
    }

    // Try to find by field name in arguments
    if (fieldName != null && !fieldName.isEmpty()) {
      for (Object arg : args) {
        if (arg == null) {
          continue;
        }
        if (arg instanceof Iterable<?> iterable) {
          for (Object item : iterable) {
            if (item == null) {
              continue;
            }
            Long val = readField(item, fieldName);
            if (val != null) {
              return val;
            }
            break; // only check first element
          }
        } else {
          Long val = readField(arg, fieldName);
          if (val != null) {
            return val;
          }
        }
      }
    }

    // Fallback: first Long argument
    for (Object arg : args) {
      if (arg instanceof Long l) {
        return l;
      }
    }

    // Check parameter names matching the field
    if (fieldName != null && !fieldName.isEmpty()) {
      MethodSignature sig = (MethodSignature) joinPoint.getSignature();
      String[] paramNames = sig.getParameterNames();
      if (paramNames != null) {
        for (int i = 0; i < paramNames.length; i++) {
          if (fieldName.equals(paramNames[i]) && args[i] instanceof Number n) {
            return n.longValue();
          }
        }
      }
    }

    return 0;
  }

  private Long readField(Object obj, String fieldName) {
    try {
      Class<?> clazz = obj.getClass();
      while (clazz != null) {
        try {
          Field f = clazz.getDeclaredField(fieldName);
          f.setAccessible(true);
          Object val = f.get(obj);
          if (val instanceof Number n) {
            return n.longValue();
          }
          return null;
        } catch (NoSuchFieldException e) {
          clazz = clazz.getSuperclass();
        }
      }
    } catch (Exception e) {
      log.trace("Failed to read field '{}' from {}: {}", fieldName, obj.getClass().getSimpleName(), e.getMessage());
    }
    return null;
  }
}
