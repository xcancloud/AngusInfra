package cloud.xcan.angus.sharding;

import cloud.xcan.angus.sharding.annotation.ShardedRepository;
import cloud.xcan.angus.sharding.annotation.ShardedTable;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.strategy.ShardingStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * AOP aspect that intercepts every method invocation on a Spring Data repository annotated with
 * {@link ShardedRepository} and automatically sets the {@link ShardContext} for that invocation
 * based on the entity's {@link ShardedTable#shardKey()}.
 *
 * <p>This removes the need for callers to manage shard context manually for the common case
 * (param name matches shard-key field, or a save method whose entity carries the shard key).
 *
 * <p>The aspect only handles <em>embedded</em> table-only sharding: it produces a
 * {@link ShardInfo} whose {@code shardKey} slot carries the computed table index – matching what
 * {@code ShardingTableInterceptor.buildShardedTableName} appends as the table-name suffix when
 * {@code enableTableSecondaryIndex=false}.
 *
 * <p>If a method does not have a parameter named after the entity shard key (e.g. an
 * {@code agentId}-based query against an entity sharded by {@code nodeId}), the caller is expected
 * to set {@link ShardContext} manually before invoking the method – the aspect detects the
 * pre-existing context and leaves it untouched.
 */
@Slf4j
@Aspect
public class ShardedRepositoryAspect implements Ordered {

  private final ShardingStrategy strategy;
  private final int defaultTableCount;
  private final int order;

  /** Cache: repository proxy interface → resolved entity meta (or {@code null} if not sharded). */
  private final ConcurrentMap<Class<?>, Optional<EntityShardMeta>> metaCache =
      new ConcurrentHashMap<>();

  public ShardedRepositoryAspect(ShardingStrategy strategy, int defaultTableCount) {
    this(strategy, defaultTableCount, Ordered.HIGHEST_PRECEDENCE + 100);
  }

  public ShardedRepositoryAspect(ShardingStrategy strategy, int defaultTableCount, int order) {
    this.strategy = strategy;
    this.defaultTableCount = defaultTableCount;
    this.order = order;
  }

  @Override
  public int getOrder() {
    return order;
  }

  /**
   * Pointcut: every public method on a Spring Data {@link Repository}. The aspect short-circuits
   * (zero overhead beyond a {@link ConcurrentHashMap#get}) when the target proxy does not declare
   * an {@link ShardedRepository}-annotated interface.
   */
  @Around("execution(public * org.springframework.data.repository.Repository+.*(..))")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    Object target = pjp.getThis() != null ? pjp.getThis() : pjp.getTarget();
    if (target == null) {
      return pjp.proceed();
    }

    Class<?> repoIface = findShardedRepoInterface(target.getClass());
    if (repoIface == null) {
      return pjp.proceed();
    }

    Optional<EntityShardMeta> opt = metaCache.computeIfAbsent(repoIface, this::resolveMeta);
    if (opt.isEmpty()) {
      return pjp.proceed();
    }
    EntityShardMeta meta = opt.get();
    ShardedRepository repoAnn = repoIface.getAnnotation(ShardedRepository.class);

    // Outer caller already set context (e.g. agentId→nodeId resolution); honor it.
    if (ShardContext.get() != null) {
      return pjp.proceed();
    }

    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    Long shardValue = resolveShardValue(method, pjp.getArgs(), meta);
    if (shardValue == null) {
      if (repoAnn.failOnUnresolved()) {
        throw new IllegalStateException(
            "ShardedRepositoryAspect could not resolve shard key '" + meta.shardKeyField
                + "' for " + repoIface.getSimpleName() + "." + method.getName()
                + "(...). Either rename a parameter to '" + meta.shardKeyField
                + "', annotate it with @Param(\"" + meta.shardKeyField
                + "\"), or set ShardContext manually before the call.");
      }
      log.warn("Cannot resolve shard key '{}' for {}.{} – proceeding without ShardContext",
          meta.shardKeyField, repoIface.getSimpleName(), method.getName());
      return pjp.proceed();
    }

    int tableCount = meta.tableCount > 0 ? meta.tableCount : defaultTableCount;
    int tableIndex = strategy.computeTableIndex(shardValue, tableCount);
    // shardKey slot carries tableIndex – matches ShardingTableInterceptor's suffixing semantics
    // when enableTableSecondaryIndex=false.
    ShardContext.set(new ShardInfo(tableIndex, repoAnn.dataSourceKey(), -1));
    try {
      return pjp.proceed();
    } finally {
      ShardContext.clear();
    }
  }

  // ── shard-value resolution ──────────────────────────────────────────────────

  Long resolveShardValue(Method method, Object[] args, EntityShardMeta meta) {
    if (args == null || args.length == 0) {
      return null;
    }
    String name = method.getName();

    // 1. save / saveAndFlush / saveAll / saveAllAndFlush → read entity field
    if (name.startsWith("save") && args.length == 1) {
      Object arg = args[0];
      if (arg == null) {
        return null;
      }
      if (arg instanceof Iterable<?> it) {
        Long first = null;
        for (Object e : it) {
          if (e == null) {
            continue;
          }
          Long v = readShardField(e, meta.shardKeyField);
          if (v == null) {
            return null;
          }
          if (first == null) {
            first = v;
          } else if (!first.equals(v)) {
            throw new IllegalStateException(
                "Mixed shard keys in batch " + name + " for "
                    + meta.entityClass.getSimpleName() + ": first=" + first + ", got=" + v);
          }
        }
        return first;
      }
      if (meta.entityClass.isInstance(arg)) {
        return readShardField(arg, meta.shardKeyField);
      }
    }

    // 2. parameter name match (preferred) or @Param("shardKeyField")
    Parameter[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      Parameter p = params[i];
      Param paramAnn = p.getAnnotation(Param.class);
      String paramName = paramAnn != null ? paramAnn.value() : p.getName();
      if (meta.shardKeyField.equals(paramName)) {
        return hashOf(args[i]);
      }
    }

    return null;
  }

  static Long readShardField(Object obj, String field) {
    Class<?> c = obj.getClass();
    while (c != null && c != Object.class) {
      try {
        Field f = c.getDeclaredField(field);
        f.setAccessible(true);
        return hashOf(f.get(obj));
      } catch (NoSuchFieldException e) {
        c = c.getSuperclass();
      } catch (IllegalAccessException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Maps an arbitrary shard-key value to a {@code long} suitable for
   * {@link ShardingStrategy#computeTableIndex(long, int)}. {@link Number} values are taken at face
   * value; {@link String} (and everything else) goes through {@link Object#hashCode()} – matching
   * the long-standing {@code MeterShardKeyHelper} behaviour and ensuring stable distribution
   * across JVM restarts (String.hashCode is contractually stable).
   */
  static Long hashOf(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof Number n) {
      return n.longValue();
    }
    return (long) v.hashCode();
  }

  // ── repo-interface / generic resolution ─────────────────────────────────────

  static Class<?> findShardedRepoInterface(Class<?> target) {
    if (target == null) {
      return null;
    }
    if (target.isInterface() && target.isAnnotationPresent(ShardedRepository.class)) {
      return target;
    }
    for (Class<?> iface : target.getInterfaces()) {
      Class<?> r = findShardedRepoInterface(iface);
      if (r != null) {
        return r;
      }
    }
    Class<?> sup = target.getSuperclass();
    if (sup != null && sup != Object.class) {
      return findShardedRepoInterface(sup);
    }
    return null;
  }

  Optional<EntityShardMeta> resolveMeta(Class<?> repoInterface) {
    Class<?> entity = findEntityClass(repoInterface);
    if (entity == null) {
      log.warn("@ShardedRepository on {} has no resolvable entity type parameter; skipping",
          repoInterface.getName());
      return Optional.empty();
    }
    ShardedTable st = entity.getAnnotation(ShardedTable.class);
    if (st == null || st.shardKey().isEmpty()) {
      log.warn("@ShardedRepository on {} targets entity {} which is missing "
              + "@ShardedTable(shardKey=\"...\"); aspect will be a no-op for this repo",
          repoInterface.getName(), entity.getName());
      return Optional.empty();
    }
    return Optional.of(new EntityShardMeta(entity, st.shardKey(), st.tableCount()));
  }

  static Class<?> findEntityClass(Class<?> repoInterface) {
    for (Type t : repoInterface.getGenericInterfaces()) {
      Class<?> e = entityFromType(t);
      if (e != null) {
        return e;
      }
    }
    for (Class<?> sup : repoInterface.getInterfaces()) {
      Class<?> e = findEntityClass(sup);
      if (e != null) {
        return e;
      }
    }
    return null;
  }

  private static Class<?> entityFromType(Type t) {
    if (t instanceof ParameterizedType pt && pt.getActualTypeArguments().length >= 1) {
      Type arg0 = pt.getActualTypeArguments()[0];
      if (arg0 instanceof Class<?> c && c.isAnnotationPresent(ShardedTable.class)) {
        return c;
      }
    }
    return null;
  }

  /** Internal cache record. Visible for tests. */
  static final class EntityShardMeta {
    final Class<?> entityClass;
    final String shardKeyField;
    final int tableCount;

    EntityShardMeta(Class<?> entityClass, String shardKeyField, int tableCount) {
      this.entityClass = entityClass;
      this.shardKeyField = shardKeyField;
      this.tableCount = tableCount;
    }
  }
}
