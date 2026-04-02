package cloud.xcan.angus.sharding.autoconfigure;

import cloud.xcan.angus.sharding.table.ShardTableManager;
import cloud.xcan.angus.sharding.table.ShardTableNameUtils;
import cloud.xcan.angus.sharding.table.ShardTableRecord;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * SQL-template-based implementation of {@link ShardTableManager}.
 *
 * <p>Loads DDL template scripts from the classpath, applies table-name substitution, and
 * executes the resulting DDL against the shard datasource. An in-memory map tracks which tables
 * have already been created during the current JVM lifetime.
 *
 * <p>When a {@link ShardTableRegistry} is supplied the manager:
 * <ul>
 *   <li>Loads previously-created tables from the registry at construction time, so that shard
 *       tables persisted before the current JVM restart are not unnecessarily re-attempted.</li>
 *   <li>Persists newly-created table records back to the registry for cross-restart durability.</li>
 * </ul>
 *
 * <p>The {@link ShardTableRegistry} is optional; when omitted the manager behaves exactly as
 * before (in-memory only, fully idempotent when DDL uses {@code CREATE TABLE IF NOT EXISTS}).
 */
@Slf4j
public class SqlTemplateTableManager implements ShardTableManager {

  private final DataSource dataSource;
  private final String schemaPath;
  private final List<String> templateTableNames;
  /**
   * Fast in-process lookup; always populated from registry at startup (if registry provided).
   */
  private final ConcurrentMap<String, Boolean> localCache = new ConcurrentHashMap<>();
  private final ShardTableRegistry registry;

  /**
   * Constructor without registry – matches the existing API.
   */
  public SqlTemplateTableManager(DataSource dataSource, String schemaPath,
      List<String> templateTableNames) {
    this(dataSource, schemaPath, templateTableNames, null);
  }

  /**
   * Constructor with an optional {@link ShardTableRegistry} for durable table tracking.
   */
  public SqlTemplateTableManager(DataSource dataSource, String schemaPath,
      List<String> templateTableNames, ShardTableRegistry registry) {
    this.dataSource = dataSource;
    this.schemaPath = schemaPath != null ? schemaPath : "sharding/schema";
    this.templateTableNames = templateTableNames;
    this.registry = registry;
    loadFromRegistry();
  }

  /**
   * Pre-warm the local cache with records persisted in the registry.
   */
  private void loadFromRegistry() {
    if (registry == null) {
      return;
    }
    try {
      List<ShardTableRecord> records = registry.findAll();
      for (ShardTableRecord r : records) {
        localCache.put(r.getTableName(), Boolean.TRUE);
      }
      log.info("Loaded {} shard table records from registry.", records.size());
    } catch (Exception e) {
      log.warn("Failed to pre-load shard table records from registry: {}", e.getMessage());
    }
  }

  @Override
  public boolean isCreated(String tableName) {
    return localCache.containsKey(tableName);
  }

  @Override
  public void ensureTablesExist(long shardKey, int shardTableCount, boolean enableSecondaryIndex) {
    if (templateTableNames == null || templateTableNames.isEmpty()) {
      return;
    }
    List<ShardTableRecord> newlyCreated = new ArrayList<>();

    for (String template : templateTableNames) {
      for (int i = 0; i < shardTableCount; i++) {
        String targetTable = enableSecondaryIndex
            ? ShardTableNameUtils.buildName(template, shardKey, i)
            : ShardTableNameUtils.buildName(template, shardKey);

        if (localCache.containsKey(targetTable)) {
          continue;
        }
        try {
          createTable(template, targetTable, enableSecondaryIndex);
          localCache.put(targetTable, Boolean.TRUE);
          newlyCreated.add(new ShardTableRecord(targetTable, shardKey,
              computeDbIndex(shardKey, shardTableCount), i));
        } catch (Exception e) {
          log.warn("Failed to create shard table '{}': {}", targetTable, e.getMessage());
        }
      }
    }

    if (!newlyCreated.isEmpty() && registry != null) {
      try {
        registry.saveAll(newlyCreated);
      } catch (Exception e) {
        log.warn("Failed to persist {} shard table records to registry: {}",
            newlyCreated.size(), e.getMessage());
      }
    }
  }

  void createTable(String templateTable, String targetTable, boolean enableSecondaryIndex) {
    String ddl = loadTemplate(templateTable);
    if (ddl == null) {
      log.warn("No DDL template found for table '{}'", templateTable);
      return;
    }
    ddl = ddl.replace(templateTable, targetTable);
    if (!enableSecondaryIndex) {
      ddl = removeSecondaryIndexes(ddl);
    }

    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    for (String stmt : ddl.split(";")) {
      String trimmed = stmt.trim();
      if (!trimmed.isEmpty()) {
        try {
          jdbc.execute(trimmed);
        } catch (Exception e) {
          log.debug("DDL note for '{}': {}", targetTable, e.getMessage());
        }
      }
    }
    log.info("Shard table '{}' created from template '{}'.", targetTable, templateTable);
  }

  String loadTemplate(String templateTable) {
    String path = schemaPath + "/" + templateTable + ".sql";
    ClassPathResource resource = new ClassPathResource(path);
    if (!resource.exists()) {
      return null;
    }
    try (InputStream is = resource.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Failed to load DDL template from '{}'", path, e);
      return null;
    }
  }

  /**
   * Remove {@code CREATE INDEX} statements from the DDL (secondary-index suppression).
   */
  static String removeSecondaryIndexes(String ddl) {
    return ddl.replaceAll("(?i)CREATE\\s+INDEX[^;]*;?", "");
  }

  /**
   * Simple modulo helper for persisting the db index in the registry record.
   */
  private static int computeDbIndex(long shardKey, int shardTableCount) {
    return shardTableCount > 0 ? (int) (Math.abs(shardKey) % shardTableCount) : 0;
  }
}
