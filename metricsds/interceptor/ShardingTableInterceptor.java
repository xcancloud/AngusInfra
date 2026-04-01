package cloud.xcan.angus.core.tester.infra.metricsds.interceptor;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder.checkDataSourceShard;
import static cloud.xcan.angus.core.tester.infra.metricsds.TableSchemaManager.SHARD_TABLE_NAME_SPLIT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.jpa.interceptor.TenantInterceptor;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder.Shard;
import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.core.tester.infra.metricsds.TableSchemaManager;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ShardingTableInterceptor extends TenantInterceptor {

  public static Set<String> shardingTables = new CopyOnWriteArraySet<>();

  public static final Pattern selectPattern = Pattern
      .compile("select\\s.+?from\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
  public static final Pattern insertPattern = Pattern
      .compile("insert\\s+into\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
  public static final Pattern updatePattern = Pattern
      .compile("update\\s+(\\S+)\\s+set\\s", Pattern.CASE_INSENSITIVE);
  public static final Pattern deletePattern = Pattern
      .compile("delete\\s+from\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
  private TableSchemaManager schemaManager;

  public ShardingTableInterceptor() {
    if (isEmpty(shardingTables)) {
      shardingTables = loadAnnotationTable(
          "cloud.xcan.angus.core.tester.infra.metricsds.domain", ShardingTable.class);
    }
  }

  @Override
  public String inspect(String sql) {
    if (!checkDataSourceShard()) {
      return super.inspect(sql);
    }

    String rawTableToken = matchTableName(sql);
    if (rawTableToken == null) {
      return super.inspect(sql);
    }

    String tableName = stripQuotes(rawTableToken);
    if (!shardingTables.contains(tableName)) {
      return super.inspect(sql);
    }

    String realTableName = getRealTableName(tableName);
    if (!getSchemaManager().isCreatedShardTable(realTableName)) {
      getSchemaManager().checkAndCreate();
    }

    String quoteChar = detectQuoteChar(rawTableToken);
    String quoted = quoteChar + realTableName + quoteChar;
    return super.inspect(sql.replaceFirst(Pattern.quote(rawTableToken), quoted));
  }

  @NotNull
  private String getRealTableName(String tableName) {
    Shard shard = MetricsDataSourceContextHolder.getShard();
    return shard.tableSecondIndex() >= 0
        ? tableName + SHARD_TABLE_NAME_SPLIT + shard.tenantId()
        + SHARD_TABLE_NAME_SPLIT + shard.tableSecondIndex()
        : tableName + SHARD_TABLE_NAME_SPLIT + shard.tenantId();
  }

  public static String matchTableName(String sql) {
    String trimmed = sql.trim();
    String upper = trimmed.substring(0, Math.min(trimmed.length(), 7)).toUpperCase();
    Matcher matcher;
    if (upper.startsWith("SELECT")) {
      matcher = selectPattern.matcher(trimmed);
    } else if (upper.startsWith("INSERT")) {
      matcher = insertPattern.matcher(trimmed);
    } else if (upper.startsWith("UPDATE")) {
      matcher = updatePattern.matcher(trimmed);
    } else if (upper.startsWith("DELETE")) {
      matcher = deletePattern.matcher(trimmed);
    } else {
      return null;
    }
    return matcher.find() ? matcher.group(1) : null;
  }

  static String stripQuotes(String name) {
    if (name == null || name.length() < 2) {
      return name;
    }
    char first = name.charAt(0);
    if ((first == '`' || first == '"') && name.charAt(name.length() - 1) == first) {
      return name.substring(1, name.length() - 1);
    }
    return name;
  }

  static String detectQuoteChar(String rawToken) {
    if (rawToken != null && !rawToken.isEmpty()) {
      char first = rawToken.charAt(0);
      if (first == '`' || first == '"') {
        return String.valueOf(first);
      }
    }
    return "`";
  }

  public synchronized TableSchemaManager getSchemaManager() {
    if (schemaManager == null) {
      schemaManager = SpringContextHolder.getBean(TableSchemaManager.class);
    }
    return schemaManager;
  }

}
