package cloud.xcan.angus.sharding.autoconfigure.registry;

import cloud.xcan.angus.sharding.table.ShardTableRecord;
import cloud.xcan.angus.sharding.table.ShardTableRegistry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * JDBC-backed {@link ShardTableRegistry} that persists records to a {@code shard_table} table in
 * the <em>primary</em> datasource.
 *
 * <p>The table is created automatically on startup using {@code CREATE TABLE IF NOT EXISTS}
 * semantics, so no manual schema migration is required.
 *
 * <p>This implementation is registered when {@code angus.sharding.table-registry-enabled=true}.
 * It can be replaced by providing a custom {@link ShardTableRegistry} bean.
 */
@Slf4j
public class JdbcShardTableRegistry implements ShardTableRegistry {

  static final String COL_TABLE_NAME = "table_name";
  static final String COL_SHARD_KEY = "shard_key";
  static final String COL_DB_INDEX = "db_index";
  static final String COL_TABLE_INDEX = "table_index";

  private final JdbcTemplate jdbc;
  private final String tableName;

  public JdbcShardTableRegistry(DataSource dataSource, String registryTable) {
    this.jdbc = new JdbcTemplate(dataSource);
    this.tableName = registryTable;
    initSchema();
  }

  private void initSchema() {
    String ddl = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
        + "table_name  VARCHAR(255) NOT NULL, "
        + "shard_key   BIGINT       NOT NULL, "
        + "db_index    INT          NOT NULL, "
        + "table_index BIGINT       NOT NULL DEFAULT -1, "
        + "CONSTRAINT pk_" + tableName + " PRIMARY KEY (table_name)"
        + ")";
    try {
      jdbc.execute(ddl);
      log.info("Shard table registry '{}' initialized.", tableName);
    } catch (Exception e) {
      log.warn("Could not initialize shard table registry '{}': {}", tableName, e.getMessage());
    }
  }

  @Override
  public List<ShardTableRecord> findAll() {
    try {
      return jdbc.query("SELECT * FROM " + tableName, new RecordMapper());
    } catch (Exception e) {
      log.error("Failed to load shard table records: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public List<ShardTableRecord> findByShardKey(long shardKey) {
    try {
      return jdbc.query("SELECT * FROM " + tableName + " WHERE shard_key = ?",
          new RecordMapper(), shardKey);
    } catch (Exception e) {
      log.error("Failed to query shard table records by shardKey={}: {}", shardKey, e.getMessage());
      return new ArrayList<>();
    }
  }

  @Override
  public boolean exists(String name) {
    if (name == null) {
      return false;
    }
    try {
      Integer count = jdbc.queryForObject(
          "SELECT COUNT(*) FROM " + tableName + " WHERE table_name = ?", Integer.class, name);
      return count != null && count > 0;
    } catch (Exception e) {
      log.error("Failed to check existence of shard table '{}': {}", name, e.getMessage());
      return false;
    }
  }

  @Override
  public void save(ShardTableRecord record) {
    if (record == null || record.getTableName() == null) {
      return;
    }
    try {
      jdbc.update("INSERT INTO " + tableName
              + " (table_name, shard_key, db_index, table_index) VALUES (?, ?, ?, ?)",
          record.getTableName(), record.getShardKey(), record.getDbIndex(),
          record.getTableIndex());
    } catch (DuplicateKeyException e) {
      // Record already exists — silently ignore (ON CONFLICT DO NOTHING semantics)
      log.debug("Shard table record '{}' already exists, skipping.", record.getTableName());
    } catch (Exception e) {
      log.warn("Failed to persist shard table record '{}': {}", record.getTableName(),
          e.getMessage());
    }
  }

  @Override
  public void saveAll(List<ShardTableRecord> records) {
    if (records == null || records.isEmpty()) {
      return;
    }
    records.forEach(this::save);
  }

  private static class RecordMapper implements RowMapper<ShardTableRecord> {

    @Override
    public ShardTableRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ShardTableRecord(
          rs.getString(COL_TABLE_NAME),
          rs.getLong(COL_SHARD_KEY),
          rs.getInt(COL_DB_INDEX),
          rs.getLong(COL_TABLE_INDEX)
      );
    }
  }
}
