package cloud.xcan.angus.sharding.context;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local holder for the current shard context.
 *
 * <p>Set before jpa access and cleared after each operation to prevent context leaking.
 */
@Slf4j
public final class ShardContext {

  public static final String MASTER_DATASOURCE_KEY = "dataSource";

  private static final ThreadLocal<ShardInfo> HOLDER = new ThreadLocal<>();

  private ShardContext() {
  }

  public static void set(ShardInfo shard) {
    if (log.isDebugEnabled()) {
      log.debug("Setting shard context: {}", shard);
    }
    HOLDER.set(shard);
  }

  public static ShardInfo get() {
    return HOLDER.get();
  }

  /**
   * Returns the current data source key, falling back to the master data source.
   */
  public static String getDataSourceKey() {
    ShardInfo info = HOLDER.get();
    return info == null || info.getDataSourceKey() == null
        ? MASTER_DATASOURCE_KEY : info.getDataSourceKey();
  }

  public static void clear() {
    HOLDER.remove();
  }

  /**
   * @return true if the current context is routed to a shard (non-master) data source
   */
  public static boolean isSharded() {
    String key = getDataSourceKey();
    return !MASTER_DATASOURCE_KEY.equals(key);
  }
}
