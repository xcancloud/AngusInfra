package cloud.xcan.angus.sharding.autoconfigure.jpa;

import cloud.xcan.angus.sharding.autoconfigure.ShardingAutoConfiguration;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Optional auto-configuration that registers a {@link JpaShardTableRegistry} when a
 * {@link ShardTableJpaRepository} bean is present on the application context.
 *
 * <p>This auto-configuration is activated automatically – no explicit import is required.  To
 * use it:
 * <ol>
 *   <li>Add {@link ShardTableEntity} to your primary {@code @EntityScan} package list.</li>
 *   <li>Add {@link ShardTableJpaRepository} to your primary {@code @EnableJpaRepositories}
 *       package list.</li>
 * </ol>
 * Once a {@link ShardTableJpaRepository} bean exists this auto-configuration replaces the default
 * {@link cloud.xcan.angus.sharding.autoconfigure.registry.InMemoryShardTableRegistry} by running
 * <strong>before</strong> {@link ShardingAutoConfiguration}, so the JPA-backed implementation
 * wins the {@code @ConditionalOnMissingBean(ShardTableRegistry.class)} check.
 */
@Slf4j
@AutoConfiguration(before = ShardingAutoConfiguration.class)
@ConditionalOnBean(ShardTableJpaRepository.class)
public class ShardingJpaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(ShardTableRegistry.class)
  public ShardTableRegistry jpaShardTableRegistry(ShardTableJpaRepository repository) {
    log.info("Activating JPA-backed shard table registry.");
    return new JpaShardTableRegistry(repository);
  }
}
