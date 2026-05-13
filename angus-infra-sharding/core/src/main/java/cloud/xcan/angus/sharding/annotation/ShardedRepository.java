package cloud.xcan.angus.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring Data JPA repository whose target entity is a {@link ShardedTable}. The
 * {@code ShardedRepositoryAspect} intercepts every invocation on the annotated repository and
 * automatically establishes the {@code ShardContext} for that invocation, so business code never
 * has to call {@code ShardContext.set/clear} directly.
 *
 * <p>The shard key value is resolved as follows (first match wins):
 * <ol>
 *   <li>For {@code save} / {@code saveAndFlush} / {@code saveAll} / {@code saveAllAndFlush}:
 *       read the field named by {@link ShardedTable#shardKey()} from the entity (or first
 *       element of the iterable).</li>
 *   <li>For other methods: the parameter whose name matches {@link ShardedTable#shardKey()};
 *       requires compilation with {@code javac -parameters} (Spring Boot default).</li>
 *   <li>If neither path resolves the key but {@code ShardContext} has already been set by an
 *       outer caller, the existing context is used unchanged.</li>
 * </ol>
 *
 * <p>If none of the above succeed and {@link #failOnUnresolved()} is {@code true}, an
 * {@link IllegalStateException} is thrown to prevent silently writing to the base table.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardedRepository {

  /**
   * Logical data-source key used to populate {@link cloud.xcan.angus.sharding.context.ShardInfo}.
   * In <em>embedded mode</em> (table-only sharding on the primary datasource) this can be any
   * non-master string – it just has to differ from
   * {@link cloud.xcan.angus.sharding.context.ShardContext#MASTER_DATASOURCE_KEY} so that the SQL
   * interceptor treats the context as sharded.
   */
  String dataSourceKey() default "shard";

  /**
   * When {@code true} (default), the aspect throws {@link IllegalStateException} if it cannot
   * resolve a shard key and no outer context is present. Set to {@code false} to allow the call to
   * proceed against the base table (only safe when the call is intentionally cross-shard).
   */
  boolean failOnUnresolved() default true;
}
