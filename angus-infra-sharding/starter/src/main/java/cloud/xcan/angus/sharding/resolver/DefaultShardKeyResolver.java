package cloud.xcan.angus.sharding.resolver;

import cloud.xcan.angus.sharding.annotation.Sharding;
import cloud.xcan.angus.sharding.resolver.ShardKeyResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * Default reflection-based {@link ShardKeyResolver} covering the most common patterns:
 * <ol>
 *   <li>Named field read from the first matching argument object via reflection (walks the class
 *       hierarchy). Supports both plain objects and the first element of an {@link Iterable}.</li>
 *   <li>Method parameter name matching – falls back to matching the {@code fieldName} against
 *       compiled parameter names (requires javac {@code -parameters} flag).</li>
 *   <li>First argument of type {@link Long} – last-resort fallback when no named match is
 *       found.</li>
 * </ol>
 *
 * <p>This resolver has order {@code 100} (lowest priority among built-ins).  Custom resolvers
 * should use a value below {@code 100} to take precedence.
 */
@Slf4j
public class DefaultShardKeyResolver implements ShardKeyResolver {

  @Override
  public Long resolve(Object[] args, String[] paramNames, Sharding sharding, String fieldName) {
    if (args == null || args.length == 0) {
      return null;
    }

    // ── 1. Named-field extraction ─────────────────────────────────────────
    if (fieldName != null && !fieldName.isEmpty()) {
      for (Object arg : args) {
        if (arg == null) {
          continue;
        }
        if (arg instanceof Iterable<?> iterable) {
          // Look at the first element only
          for (Object item : iterable) {
            if (item == null) {
              continue;
            }
            Long val = readFieldReflectively(item, fieldName);
            if (val != null) {
              return val;
            }
            break; // only check first element
          }
        } else {
          Long val = readFieldReflectively(arg, fieldName);
          if (val != null) {
            return val;
          }
        }
      }

      // ── 2. Parameter name matching (needs -parameters javac flag) ────────
      if (paramNames != null) {
        for (int i = 0; i < paramNames.length && i < args.length; i++) {
          if (fieldName.equals(paramNames[i]) && args[i] instanceof Number n) {
            return n.longValue();
          }
        }
      }
    }

    // ── 3. First Long argument fallback ───────────────────────────────────
    for (Object arg : args) {
      if (arg instanceof Long l) {
        return l;
      }
    }

    return null;
  }

  @Override
  public int getOrder() {
    return 100;
  }

  // ── Private helpers ──────────────────────────────────────────────────────

  /**
   * Reads a field by name from {@code obj} by walking the class hierarchy. Returns {@code null}
   * when the field doesn't exist or is not numeric.
   */
  private Long readFieldReflectively(Object obj, String fieldName) {
    Class<?> clazz = obj.getClass();
    while (clazz != null && clazz != Object.class) {
      try {
        java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        Object val = f.get(obj);
        if (val instanceof Number n) {
          return n.longValue();
        }
        return null;
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } catch (Exception e) {
        log.trace("Failed to read field '{}' from {}: {}", fieldName,
            obj.getClass().getSimpleName(), e.getMessage());
        return null;
      }
    }
    return null;
  }
}
