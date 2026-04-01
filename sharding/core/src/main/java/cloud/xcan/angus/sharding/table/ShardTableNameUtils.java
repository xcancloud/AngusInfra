package cloud.xcan.angus.sharding.table;

/**
 * Utility class for building sharded table names.
 */
public final class ShardTableNameUtils {

  public static final String SEPARATOR = "-";

  private ShardTableNameUtils() {
  }

  /**
   * Build a sharded table name from the template table name and shard key.
   *
   * @param templateName base table name (e.g. "exec_sample")
   * @param shardKey     primary shard key value (e.g. tenant ID)
   * @return e.g. "exec_sample-100"
   */
  public static String buildName(String templateName, long shardKey) {
    return templateName + SEPARATOR + shardKey;
  }

  /**
   * Build a sharded table name with secondary table index.
   *
   * @param templateName base table name
   * @param shardKey     primary shard key value
   * @param tableIndex   secondary table index
   * @return e.g. "exec_sample-100-3"
   */
  public static String buildName(String templateName, long shardKey, long tableIndex) {
    return templateName + SEPARATOR + shardKey + SEPARATOR + tableIndex;
  }

  /**
   * Parse the template table name from a sharded table name.
   *
   * @param shardedName e.g. "exec_sample-100-3"
   * @return the template name, e.g. "exec_sample"
   */
  public static String parseTemplateName(String shardedName) {
    int idx = shardedName.indexOf(SEPARATOR);
    return idx < 0 ? shardedName : shardedName.substring(0, idx);
  }
}
