package cloud.xcan.angus.idgen.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for ID generation system.
 * <p>
 * This configuration class manages all tunable parameters for UID and BID generation, allowing
 * zero-downtime configuration adjustments via Spring properties.
 * <p>
 * Usage in application.yml:
 * <pre>
 * xcan:
 *   idgen:
 *     enabled: true
 *     uid:
 *       timeBits: 32
 *       workerBits: 13
 *       seqBits: 18
 *       epochStr: "2021-01-01"
 *       retriesNum: 3
 *     cached:
 *       boostPower: 2
 *       paddingFactor: 50
 *       scheduleInterval: 300
 *       rejectionPolicy: BLOCK
 *     bid:
 *       maxStep: 1000000
 *       maxBatchNum: 10000
 *       maxSeqLength: 40
 *       initialMapCapacity: 512
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "angus.idgen")
@Getter
@Setter
public class IdGenProperties {

  /**
   * Enable/disable the idgen module
   */
  private boolean enabled = true;

  /**
   * UID Generator configuration
   */
  private UidGeneatorConfig uid = new UidGeneatorConfig();

  /**
   * Cached UID Generator configuration
   */
  private CachedUidConfig cached = new CachedUidConfig();

  /**
   * BID Generator configuration
   */
  private BidGeneratorConfig bid = new BidGeneratorConfig();

  /**
   * UID Generator specific configuration
   */
  @Getter
  @Setter
  public static class UidGeneatorConfig {

    /**
     * Number of bits for timestamp (default 32, supports ~136 years until 2157)
     */
    private int timeBits = 32;

    /**
     * Number of bits for worker/instance id (default 13, supports ~8192 instances)
     */
    private int workerBits = 13;

    /**
     * Number of bits for sequence (default 18, supports ~262144 IDs/sec)
     */
    private int seqBits = 18;

    /**
     * Epoch date for timestamp calculation (format: yyyy-MM-dd)
     */
    private String epochStr = "2021-01-01";

    /**
     * Number of retries when obtaining instance ID fails
     */
    private int retriesNum = 3;
  }

  /**
   * Cached UID Generator (RingBuffer) specific configuration
   */
  @Getter
  @Setter
  public static class CachedUidConfig {

    /**
     * Ring buffer size boost power. Final buffer size = (maxSequence + 1) << boostPower Default 2
     * means 1M size with 48MB memory cost
     */
    private int boostPower = 2;

    /**
     * Padding factor for ring buffer (0-100). When remaining IDs < (buffer_size * paddingFactor /
     * 100), trigger padding Default 50 means trigger at 50% remaining
     */
    private int paddingFactor = 50;

    /**
     * Scheduled padding interval in seconds
     */
    private long scheduleInterval = 300; // 5 minutes

    /**
     * Rejected put buffer handler policy: DISCARD, EXCEPTION, BLOCK Default BLOCK ensures no UID
     * loss but may increase latency
     */
    private String rejectionPolicy = "BLOCK";
  }

  /**
   * BID Generator specific configuration
   */
  @Getter
  @Setter
  public static class BidGeneratorConfig {

    /**
     * Maximum step for ID allocation
     */
    private long maxStep = 1000000L;

    /**
     * Maximum batch number for ID generation
     */
    private int maxBatchNum = 10000;

    /**
     * Maximum sequence length
     */
    private int maxSeqLength = 40;

    /**
     * Initial capacity for ID config and atomic maps
     */
    private int initialMapCapacity = 512;
  }
}
