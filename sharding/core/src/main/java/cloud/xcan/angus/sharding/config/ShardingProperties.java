package cloud.xcan.angus.sharding.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the sharding framework.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "angus.sharding")
public class ShardingProperties {

  /**
   * Whether the sharding framework is enabled.
   */
  private boolean enabled = false;

  /**
   * Number of database shards. Value range: 1-10.
   */
  private int shardDbCount = 1;

  /**
   * Number of table shards per database. Value range: 1-50.
   */
  private int shardTableCount = 1;

  /**
   * Whether to enable secondary table-level indexing. When false, tables are only split by the
   * primary shard key (e.g. tenant ID).
   */
  private boolean enableTableSecondaryIndex = false;

  /**
   * Database type: mysql or postgres.
   */
  private String dbType = "mysql";

  /**
   * Database username shared across all shards.
   */
  private String username;

  /**
   * Database password shared across all shards.
   */
  private String password;

  /**
   * JPA entity packages to scan for the sharding EntityManagerFactory.
   */
  private String[] entityPackages;

  /**
   * Pointcut expression for the AOP sharding aspect. Typically points to the jpa package.
   */
  private String aspectPointcut;

  /**
   * SQL schema path prefix for template table DDL. Defaults to classpath-relative.
   */
  private String schemaPath;

  /**
   * Template table names whose DDL scripts are loaded for dynamic table creation.
   */
  private String[] templateTableNames;

  /**
   * Whether to persist shard table records to a durable store on creation. When {@code false}
   * (default), an in-memory registry is used and the registry is lost on restart (safe when DDL
   * uses {@code CREATE TABLE IF NOT EXISTS}). When {@code true}, a JDBC-backed registry writes each
   * new shard table record to the {@link #tableRegistryTable} table in the primary datasource,
   * enabling cross-restart tracking.
   */
  private boolean tableRegistryEnabled = false;

  /**
   * Name of the SQL table used by the JDBC shard table registry. Only relevant when
   * {@link #tableRegistryEnabled} is {@code true}.
   */
  private String tableRegistryTable = "angus_shard_table";

  private Mysql mysql = new Mysql();
  private Postgresql postgresql = new Postgresql();

  @Getter
  @Setter
  public static class Mysql {

    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String[] urls;
  }

  @Getter
  @Setter
  public static class Postgresql {

    private String driverClassName = "org.postgresql.Driver";
    private String[] urls;
  }

  public static final int MAX_SHARD_DB_COUNT = 10;
  public static final int MAX_SHARD_TABLE_COUNT = 50;
}
