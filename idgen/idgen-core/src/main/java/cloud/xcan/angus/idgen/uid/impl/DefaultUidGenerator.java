package cloud.xcan.angus.idgen.uid.impl;

import cloud.xcan.angus.api.pojo.instance.InstanceInfo;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
import cloud.xcan.angus.idgen.uid.BitsAllocator;
import cloud.xcan.angus.idgen.uid.InstanceIdAssigner;
import cloud.xcan.angus.spec.utils.DateUtils;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an implementation of {@link UidGenerator}
 * <p>
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <li>sign: The highest bit is 0
 * <li>delta seconds: The next 28 bits, represents delta seconds since a customer epoch(2016-05-20
 * 00:00:00.000). Supports about 8.7 years until to 2024-11-20 21:24:16
 * <li>worker id: The next 22 bits, represents the worker's id which assigns based on database, max
 * id is about 420W
 * <li>sequence: The next 13 bits, represents a sequence within the same second, max for
 * 8192/s<br><br>
 * <p>
 * The {@link DefaultUidGenerator#parseUID(long)} is a tool method to parse the bits
 *
 * <pre>{@code
 * +------+----------------------+----------------+-----------+
 * | sign |     delta seconds    | worker node id | sequence  |
 * +------+----------------------+----------------+-----------+
 *   1bit          28bits              22bits         13bits
 * }</pre>
 * <p>
 * You can also specified the bits by Spring property setting.
 * <li>timeBits: default as 28
 * <li>workerBits: default as 22
 * <li>seqBits: default as 13
 * <li>epochStr: Epoch date string format 'yyyy-MM-dd'. Default as '2016-05-20'<p>
 *
 * <b>Note that:</b> The total bits must be 64 -1
 */
public class DefaultUidGenerator implements UidGenerator/*, InitializingBean*/ {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUidGenerator.class);

  /**
   * Maximum 128 year
   */
  protected int timeBits = 32;
  /**
   * Maximum start 8192 times
   * <p>
   * TODO Consider reuse strategies
   */
  protected int workerBits = 13;
  /**
   * Maximum 262144 UID/s
   */
  protected int seqBits = 18;

  /**
   * Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000)
   */
  protected String epochStr = "2021-01-01";
  protected long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1609430400000L);

  /**
   * Stable fields after spring bean initializing
   */
  protected BitsAllocator bitsAllocator;
  protected long instanceId;

  /**
   * Volatile fields caused by nextId()
   */
  protected long sequence = 0L;
  protected long lastSecond = -1L;

  /**
   * The number of retries when obtaining the instance id fails
   */
  protected int retriesNum = 3;
  /**
   * Spring property
   */
  protected InstanceInfo instanceInfo;
  /**
   * Spring property
   */
  private InstanceIdAssigner instanceIdAssigner;

  public void afterPropertiesSet() throws Exception {
    // initialize bits allocator
    bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);

    // initialize instance id
    Long id = null;
    while (retriesNum > 0) {
      id = getInstanceId();
      if (Objects.nonNull(id)) {
        break;
      }
      --retriesNum;
    }
    if (Objects.isNull(id)) {
      throw new RuntimeException("Failed to obtain instance id");
    }

    if (instanceId > bitsAllocator.getMaxWorkerId()) {
      throw new RuntimeException(
          "Instance id " + instanceId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
    }

    LOGGER
        .info("Initialized bits(1, {}, {}, {}) for instance id :{}", timeBits, workerBits, seqBits,
            instanceId);
  }

  private Long getInstanceId() {
    if (retriesNum <= 0) {
      return null;
    }
    Long id = null;
    if (Objects.nonNull(instanceInfo)) {
      return instanceIdAssigner
          .assignInstanceIdByParam(instanceInfo.getHost(), instanceInfo.getPort(),
              instanceInfo.getInstanceType());

    } else {
      return instanceIdAssigner.assignInstanceIdByEnv();
    }
  }

  @Override
  public long getUID() throws IdGenerateException {
    try {
      return nextId();
    } catch (Exception e) {
      LOGGER.error("Generate unique id exception. ", e);
      throw new IdGenerateException(e);
    }
  }

  @Override
  public String parseUID(long uid) {
    long totalBits = BitsAllocator.TOTAL_BITS;
    long signBits = bitsAllocator.getSignBits();
    long timestampBits = bitsAllocator.getTimestampBits();
    long workerIdBits = bitsAllocator.getWorkerIdBits();
    long sequenceBits = bitsAllocator.getSequenceBits();

    // parse UID
    long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
    long workerId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
    long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

    Date thatTime = new Date(TimeUnit.SECONDS.toMillis(epochSeconds + deltaSeconds));
    String thatTimeStr = DateUtils.formatByDateTimePattern(thatTime);

    // format as string
    return String
        .format("{\"UID\":\"%d\",\"datetime\":\"%s\",\"instanceId\":\"%d\",\"sequence\":\"%d\"}",
            uid, thatTimeStr, workerId, sequence);
  }

  /**
   * Get UID
   *
   * @return UID
   * @throws IdGenerateException in the case: Clock moved backwards; Exceeds the max datetime
   */
  protected synchronized long nextId() {
    long currentSecond = getCurrentSecond();

    // Clock moved backwards, refuse to generate baidu.uid
    if (currentSecond < lastSecond) {
      long refusedSeconds = lastSecond - currentSecond;
      throw new IdGenerateException("Clock moved backwards. Refusing for %d seconds",
          refusedSeconds);
    }

    // At the same second, increase sequence
    if (currentSecond == lastSecond) {
      sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
      // Exceed the max sequence, we wait the next second to generate baidu.uid
      if (sequence == 0) {
        currentSecond = getNextSecond(lastSecond);
      }

      // At the different second, sequence restart from zero
    } else {
      sequence = 0L;
    }

    lastSecond = currentSecond;

    // Allocate bits for UID
    return bitsAllocator.allocate(currentSecond - epochSeconds, instanceId, sequence);
  }

  /**
   * Get next millisecond
   */
  private long getNextSecond(long lastTimestamp) {
    long timestamp = getCurrentSecond();
    while (timestamp <= lastTimestamp) {
      timestamp = getCurrentSecond();
    }

    return timestamp;
  }

  /**
   * Get current second
   */
  private long getCurrentSecond() {
    long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
      throw new IdGenerateException(
          "Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
    }
    return currentSecond;
  }

  /**
   * Setters for spring property
   */
  public void setInstanceIdAssigner(InstanceIdAssigner instanceIdAssigner) {
    this.instanceIdAssigner = instanceIdAssigner;
  }

  /**
   * Setters for spring property
   */
  public void setInstanceInfo(InstanceInfo instanceInfo) {
    this.instanceInfo = instanceInfo;
  }

  public void setTimeBits(int timeBits) {
    if (timeBits > 0) {
      this.timeBits = timeBits;
    }
  }

  public void setWorkerBits(int workerBits) {
    if (workerBits > 0) {
      this.workerBits = workerBits;
    }
  }

  public void setSeqBits(int seqBits) {
    if (seqBits > 0) {
      this.seqBits = seqBits;
    }
  }

}
