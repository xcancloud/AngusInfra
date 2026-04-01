package cloud.xcan.angus.core.tester.infra.metricsds;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceConfiguration.MASTER_DATASOURCE_KEY;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsDataSourceContextHolder {

  private static final ThreadLocal<Shard> HOLDER = new ThreadLocal<>();

  public static void setShard(Shard shard) {
    if (log.isDebugEnabled()) {
      log.debug("Switch to slave datasource: {}", shard);
    }
    HOLDER.set(shard);
  }

  public static Shard getShard() {
    return HOLDER.get();
  }

  /**
   * Master dataSource {@see HibernateJpaAutoConfigurer}
   */
  public static String getDataSource() {
    Shard lookUpKey = HOLDER.get();
    return lookUpKey == null || lookUpKey.dataSource() == null
        ? MASTER_DATASOURCE_KEY : lookUpKey.dataSource();
  }

  public static void clear() {
    HOLDER.remove();
  }

  public static boolean checkDataSourceShard() {
    String datasource = getDataSource();
    return datasource != null && !datasource.equalsIgnoreCase(MASTER_DATASOURCE_KEY);
  }

  @Setter
  @Getter
  @Accessors(chain = true, fluent = true)
  public static class Shard {

    private long tenantId;
    private String dataSource;
    /**
     * If the value is -1, it will not participate in sharding
     */
    private long tableSecondIndex;

    @Override
    public String toString() {
      return "Shard{" +
          "tenantId=" + tenantId +
          ", dataSource='" + dataSource + '\'' +
          ", tableIndex=" + tableSecondIndex +
          '}';
    }
  }
}
