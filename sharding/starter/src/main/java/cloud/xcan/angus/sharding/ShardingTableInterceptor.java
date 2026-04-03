package cloud.xcan.angus.sharding;

import cloud.xcan.angus.sharding.annotation.ShardedTable;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.table.ShardTableManager;
import cloud.xcan.angus.sharding.table.ShardTableNameUtils;
import cloud.xcan.angus.sharding.table.SqlTableMatcher;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Hibernate {@link StatementInspector} that rewrites table names in SQL statements for sharded
 * tables. Detects entities annotated with {@link ShardedTable} and replaces their table names with
 * the tenant-specific sharded table name.
 */
@Slf4j
public class ShardingTableInterceptor implements StatementInspector {

  private final Set<String> shardedTables;
  private final ShardingProperties properties;
  private ShardTableManager tableManager;

  public ShardingTableInterceptor(ShardingProperties properties) {
    this.properties = properties;
    this.shardedTables = new CopyOnWriteArraySet<>();
  }

  /**
   * Scan packages for {@link ShardedTable}-annotated entity classes and register their table
   * names.
   */
  public void scanAndRegister(String... packages) {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(ShardedTable.class));
    for (String pkg : packages) {
      for (BeanDefinition bd : scanner.findCandidateComponents(pkg)) {
        try {
          Class<?> clazz = Class.forName(bd.getBeanClassName());
          jakarta.persistence.Table tableAnn = clazz.getAnnotation(jakarta.persistence.Table.class);
          if (tableAnn != null && !tableAnn.name().isEmpty()) {
            shardedTables.add(tableAnn.name());
          } else {
            // Convert class name to snake_case as table name
            shardedTables.add(toSnakeCase(clazz.getSimpleName()));
          }
        } catch (ClassNotFoundException e) {
          log.warn("Could not load @ShardedTable class: {}", bd.getBeanClassName());
        }
      }
    }
    log.info("Registered {} sharded table names: {}", shardedTables.size(), shardedTables);
  }

  /**
   * Register a table name as sharded (for programmatic registration).
   */
  public void registerTable(String tableName) {
    shardedTables.add(tableName);
  }

  public Set<String> getShardedTables() {
    return Set.copyOf(shardedTables);
  }

  @Override
  public String inspect(String sql) {
    if (!ShardContext.isSharded()) {
      return sql;
    }

    String rawToken = SqlTableMatcher.matchTableToken(sql);
    if (rawToken == null) {
      return sql;
    }

    String tableName = SqlTableMatcher.stripQuotes(rawToken);
    if (!shardedTables.contains(tableName)) {
      return sql;
    }

    String realTableName = buildShardedTableName(tableName);

    // Trigger table creation if needed
    if (tableManager != null && !tableManager.isCreated(realTableName)) {
      ShardInfo info = ShardContext.get();
      if (info != null) {
        tableManager.ensureTablesExist(
            info.getShardKey(),
            properties.getShardTableCount(),
            properties.isEnableTableSecondaryIndex());
      }
    }

    String quoteChar = SqlTableMatcher.detectQuoteChar(rawToken);
    String quoted = quoteChar + realTableName + quoteChar;
    return sql.replaceFirst(java.util.regex.Pattern.quote(rawToken), quoted);
  }

  String buildShardedTableName(String tableName) {
    ShardInfo shard = ShardContext.get();
    if (shard == null) {
      return tableName;
    }
    return shard.hasTableIndex()
        ? ShardTableNameUtils.buildName(tableName, shard.getShardKey(), shard.getTableIndex())
        : ShardTableNameUtils.buildName(tableName, shard.getShardKey());
  }

  public void setTableManager(ShardTableManager tableManager) {
    this.tableManager = tableManager;
  }

  static String toSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
