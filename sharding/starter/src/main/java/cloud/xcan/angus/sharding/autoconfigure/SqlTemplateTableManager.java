package cloud.xcan.angus.sharding.autoconfigure;

import cloud.xcan.angus.sharding.table.ShardTableManager;
import cloud.xcan.angus.sharding.table.ShardTableNameUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * SQL-template-based implementation of {@link ShardTableManager}. Loads DDL templates from the
 * classpath, applies table name substitution, and executes the DDL to create shard tables.
 */
@Slf4j
public class SqlTemplateTableManager implements ShardTableManager {

  private final DataSource dataSource;
  private final String schemaPath;
  private final List<String> templateTableNames;
  private final ConcurrentMap<String, Boolean> createdTables = new ConcurrentHashMap<>();

  public SqlTemplateTableManager(DataSource dataSource, String schemaPath,
      List<String> templateTableNames) {
    this.dataSource = dataSource;
    this.schemaPath = schemaPath != null ? schemaPath : "sharding/schema";
    this.templateTableNames = templateTableNames;
  }

  @Override
  public boolean isCreated(String tableName) {
    return createdTables.containsKey(tableName);
  }

  @Override
  public void ensureTablesExist(long shardKey, int shardTableCount,
      boolean enableSecondaryIndex) {
    if (templateTableNames == null || templateTableNames.isEmpty()) {
      return;
    }
    for (String template : templateTableNames) {
      for (int i = 0; i < shardTableCount; i++) {
        String targetTable = ShardTableNameUtils.buildName(template, shardKey, i);
        if (createdTables.containsKey(targetTable)) {
          continue;
        }
        try {
          createTable(template, targetTable, enableSecondaryIndex);
          createdTables.put(targetTable, Boolean.TRUE);
        } catch (Exception e) {
          log.warn("Failed to create shard table '{}': {}", targetTable, e.getMessage());
        }
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
    String[] statements = ddl.split(";");
    for (String stmt : statements) {
      String trimmed = stmt.trim();
      if (!trimmed.isEmpty()) {
        try {
          jdbc.execute(trimmed);
        } catch (Exception e) {
          // Table might already exist; log a debug-level message
          log.debug("DDL execution note for '{}': {}", targetTable, e.getMessage());
        }
      }
    }
    log.debug("Shard table '{}' created from template '{}'", targetTable, templateTable);
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
   * Remove CREATE INDEX statements (secondary indexes) from the DDL.
   */
  static String removeSecondaryIndexes(String ddl) {
    return ddl.replaceAll("(?i)CREATE\\s+INDEX[^;]*;?", "");
  }
}
