package cloud.xcan.angus.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity as a sharded table. Entity table names annotated with this annotation will be
 * rewritten at SQL level to include the tenant and optional table index suffix.
 *
 * <p>For example, a table named {@code exec_sample} for tenant 100 becomes {@code exec_sample-100}
 * or {@code exec_sample-100-3} when secondary table indexing is enabled.
 *
 * <p>When {@link #shardKey()} is specified together with a repository annotated with
 * {@link ShardedRepository}, the {@code ShardedRepositoryAspect} will automatically extract the
 * shard key value from method arguments (or the saved entity) and set the {@code ShardContext}
 * for that invocation – removing the need for callers to manage the context manually.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardedTable {

  /**
   * Name of the field on the entity used as the shard key (e.g. {@code "taskId"} or
   * {@code "nodeId"}). Required when paired with {@link ShardedRepository}; ignored otherwise.
   */
  String shardKey() default "";

  /**
   * Number of physical table shards for this entity. {@code 0} means inherit from the runtime
   * configuration ({@code angus.sharding.shard-table-count}).
   */
  int tableCount() default 0;
}
